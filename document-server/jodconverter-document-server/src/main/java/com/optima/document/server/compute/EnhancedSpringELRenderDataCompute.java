package com.optima.document.server.compute;

import com.deepoove.poi.render.compute.EnvModel;
import com.deepoove.poi.render.compute.ReadMapAccessor;
import com.deepoove.poi.render.compute.RenderDataCompute;
import com.deepoove.poi.render.compute.SpELRenderDataCompute;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 增强版Spring表达式语言变量解析器，参考 {@link SpELRenderDataCompute} 做了如下修改：
 * <ul>
 *     <li>当Spring表达式引擎解析失败时，优先按完整 key 直接查找 Map；
 *         若仍失败，则尝试从表达式中提取 Map key 引用并替换为实际值后重新解析，
 *         以支持 {@code CaseInfo.litigantType == 'UNIT'? 'aaa':'bbb'} 等复杂表达式。</li>
 *     <li>保留了默认实现中环境变量（如 {@code _index}、{@code _size}、
 *         {@code _hasNext}）的优先查找逻辑。</li>
 * </ul>
 * <p>
 * <b>扩展说明：</b>如需支持更多数据类型的回退查找（如 Gson 的 JsonObject、
 * Jackson 的 ObjectNode 等），需要同时修改以下两处：
 * <ol>
 *     <li>构造方法中：增加对应类型的 key 提取逻辑，填充 {@link #mapKeys}</li>
 *     <li>{@link #compute(String)} 方法的 catch 块中：增加对应的 instanceof 分支</li>
 * </ol>
 *
 * @see SpELRenderDataCompute
 * @see RenderDataCompute
 */
public class EnhancedSpringELRenderDataCompute implements RenderDataCompute {

    /**
     * 根数据对象，用于表达式引擎解析失败时的 Map 回退查找
     */
    private final Object root;

    /**
     * Spring表达式解析器
     */
    private final ExpressionParser parser;

    /**
     * 元素数据的Spring表达式求值上下文，支持字段访问和自定义函数
     */
    private final EvaluationContext context;

    /**
     * 环境变量的Spring表达式求值上下文，负责解析 _index、_size、_hasNext 等内置变量
     */
    private EvaluationContext envContext;

    /**
     * 是否严格模式，为 true 时表达式解析失败直接抛异常，为 false 时返回 null
     */
    private final boolean isStrict;

    /**
     * 根数据中的所有 Map key（按长度降序），用于表达式中的变量替换
     */
    private final List<String> mapKeys;

    /**
     * 用于匹配表达式中变量引用的正则：匹配字母/数字/下划线/中文字符组成的点分路径
     */
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\w\\u4e00-\\u9fa5]+(\\.[\\w\\u4e00-\\u9fa5]+)+");

    public EnhancedSpringELRenderDataCompute(EnvModel model) {
        this(model, true);
    }

    public EnhancedSpringELRenderDataCompute(EnvModel model, boolean isStrict) {
        this(model, isStrict, Collections.emptyMap());
    }

    /**
     * 构造方法，根据数据对象和环境变量初始化Spring表达式引擎。
     * <p>
     * <b>注意：</b>当前仅支持根数据为 {@link Map} 类型的 key 提取。
     * 如需扩展其他类型（如 Gson JsonObject、Jackson ObjectNode 等），
     * 请在本构造方法中增加对应类型的 key 提取逻辑。
     *
     * @param model        数据模型，包含根数据对象和环境变量
     * @param isStrict     是否严格模式
     * @param spELFunction 自定义SpEL函数映射，key为函数名，value为对应的Method
     */

    public EnhancedSpringELRenderDataCompute(EnvModel model, boolean isStrict, Map<String, Method> spELFunction) {
        this.root = model.getRoot();
        this.isStrict = isStrict;
        this.parser = new SpelExpressionParser();
        if (null != model.getEnv() && !model.getEnv().isEmpty()) {
            this.envContext = new StandardEvaluationContext(model.getEnv());
            ((StandardEvaluationContext) envContext).addPropertyAccessor(new ReadMapAccessor());
        }
        this.context = new StandardEvaluationContext(model.getRoot());
        ((StandardEvaluationContext) context).addPropertyAccessor(new ReadMapAccessor());
        spELFunction.forEach(((StandardEvaluationContext) context)::registerFunction);

        // 预提取 Map 中的所有 key，按长度降序排列（避免短 key 误匹配长 key 的前缀）
        if (root instanceof Map) {
            this.mapKeys = ((Map<?, ?>) root).keySet().stream()
                    .map(Object::toString)
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .collect(Collectors.toList());
        } else {
            this.mapKeys = Collections.emptyList();
            if (root != null) {
                System.err.printf("根数据类型为 %s，非 Map 类型，不支持通过 Map 的 Key 进行回退查找。\n如需支持该类型，请在 compute 方法的 catch 块中增加对应的 instanceof 分支。\n", root.getClass().getName());
            }
        }
    }

    @Override
    public Object compute(String el) {
        try {
            // 1. 优先从环境变量中取值（_index、_size、_hasNext 等内置变量）
            if (null != envContext && !el.contains("#this")) {
                try {
                    Object val = parser.parseExpression(el).getValue(envContext);
                    if (null != val) {
                        return val;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            // 2. 通过Spring表达式引擎从数据对象中取值
            return parser.parseExpression(el).getValue(context);
        } catch (Exception firstEx) {
            // 3. 表达式引擎解析失败，尝试从表达式中提取 Map key 替换为实际值后重新解析
            if (root instanceof Map && !mapKeys.isEmpty()) {
                Map<?, ?> map = (Map<?, ?>) root;
                // 3a. 先尝试完整 key 直接查找（适用于纯变量表达式）
                if (map.containsKey(el)) {
                    return map.get(el);
                }
                // 3b. 尝试从表达式中提取变量引用并替换为实际值后重新解析
                //     适用于 CaseInfo.litigantType == 'UNIT'? 'aaa':'bbb' 等复杂表达式
                try {
                    String resolvedEl = resolveMapKeys(el, map);
                    if (!resolvedEl.equals(el)) {
                        return parser.parseExpression(resolvedEl).getValue(context);
                    }
                } catch (Exception ignored) {
                    // ignore, fall through to strict check
                }
            }

            if (isStrict) throw firstEx;
            return null;
        }
    }

    /**
     * 从表达式中提取 Map key 引用，替换为实际值的字面量。
     * <p>
     * 示例：输入 {@code CaseInfo.litigantType == 'UNIT'? 'aaa':'bbb'}，
     * 当 Map 中 {@code CaseInfo.litigantType} 的值为 {@code "UNIT"} 时，
     * 输出 {@code "UNIT" == 'UNIT'? 'aaa':'bbb'}。
     * <p>
     * 精确性保障：
     * <ul>
     *     <li>Map key 按长度降序处理，避免短 key 优先替换长 key 的前缀</li>
     *     <li>正则前瞻断言排除 \w、中文字符和 {@code .}，
     *         确保匹配完整的变量引用而非子串</li>
     * </ul>
     *
     * @param el  原始表达式
     * @param map 数据 Map
     * @return 替换后的表达式，未发生替换时返回原表达式
     */
    private String resolveMapKeys(String el, Map<?, ?> map) {
        String result = el;
        for (String key : mapKeys) {
            if (!result.contains(key)) {
                continue;
            }
            Matcher matcher = TOKEN_PATTERN.matcher(key);
            if (matcher.matches()) {
                Object value = map.get(key);
                String replacement = escapeToLiteral(value);
                // 前瞻断言：key 后面不能是 \w、中文字符或 .（排除子串和嵌套路径）
                String regex = Pattern.quote(key) + "(?=[^\\w\\u4e00-\\u9fa5.]|$)";
                result = result.replaceAll(regex, replacement);
            }
        }
        return result;
    }

    /**
     * 将值转为表达式字面量字符串。
     * <ul>
     *     <li>String 类型：用双引号包裹，内部双引号和反斜杠转义</li>
     *     <li>数值/布尔类型：直接 toString</li>
     *     <li>其他类型：用双引号包裹 toString 结果</li>
     * </ul>
     *
     * @param value 待转换的值
     * @return 表达式字面量
     */
    private String escapeToLiteral(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        } else {
            String str = value.toString()
                    .replace("\\", "\\\\")   // 先转义反斜杠
                    .replace("\"", "\\\"");  // 再转义双引号
            return "\"" + str + "\"";
        }
    }
}