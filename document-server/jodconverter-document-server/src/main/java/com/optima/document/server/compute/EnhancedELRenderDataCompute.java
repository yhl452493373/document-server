package com.optima.document.server.compute;

import com.deepoove.poi.exception.ExpressionEvalException;
import com.deepoove.poi.expression.DefaultEL;
import com.deepoove.poi.render.compute.DefaultELRenderDataCompute;
import com.deepoove.poi.render.compute.EnvModel;
import com.deepoove.poi.render.compute.RenderDataCompute;

import java.util.Map;

/**
 * 增强版表达式变量解析器，参考 {@link DefaultELRenderDataCompute} 做了如下修改：
 * <ul>
 *     <li>当表达式引擎解析失败（如 {@code CaseInfo.aaa} 这类带点号的 Map key
 *         无法被识别）时，回退为从根数据 Map 中按完整 key 直接查找，
 *         以支持数据源为 Map 的场景。</li>
 *     <li>保留了默认实现中环境变量（如 {@code _index}、{@code _size}、
 *         {@code _hasNext}）的优先查找逻辑。</li>
 * </ul>
 * <p>
 * <b>扩展说明：</b>如需支持更多数据类型的回退查找（如 Gson 的 JsonObject、
 * Jackson 的 ObjectNode 等），在 {@link #compute(String)} 方法的 catch 块中
 * 增加对应的 instanceof 分支即可。示例：
 * <pre>
 *     // Gson JsonObject
 *     if (root instanceof com.google.gson.JsonObject) {
 *         com.google.gson.JsonElement element =
 *                 ((com.google.gson.JsonObject) root).get(el);
 *         if (element != null) {
 *             return element;
 *         }
 *     }
 *     // Jackson ObjectNode
 *     if (root instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
 *         com.fasterxml.jackson.databind.JsonNode node =
 *                 ((com.fasterxml.jackson.databind.node.ObjectNode) root).get(el);
 *         if (node != null) {
 *             return node;
 *         }
 *     }
 * </pre>
 *
 * @see DefaultELRenderDataCompute
 * @see RenderDataCompute
 */
public class EnhancedELRenderDataCompute implements RenderDataCompute {

    /**
     * 根数据对象，用于表达式引擎解析失败时的 Map 回退查找
     */
    private final Object root;

    /**
     * 元素数据表达式引擎，负责从数据对象中取值（如字段访问、#this 等）
     */
    private final DefaultEL elObject;

    /**
     * 环境变量表达式引擎，负责从环境变量中取值（如 _index、_size、_hasNext）
     */
    private final DefaultEL envObject;

    /**
     * 是否严格模式，为 true 时表达式解析失败直接抛异常，为 false 时返回 null
     */
    private final boolean isStrict;

    /**
     * 构造方法，根据数据对象和环境变量初始化表达式引擎
     *
     * @param model    数据模型，包含根数据对象和环境变量
     * @param isStrict 是否严格模式
     */
    public EnhancedELRenderDataCompute(EnvModel model, boolean isStrict) {
        this.root = model.getRoot();
        this.elObject = DefaultEL.create(model.getRoot());
        if (null != model.getEnv() && !model.getEnv().isEmpty()) {
            this.envObject = DefaultEL.create(model.getEnv());
        } else {
            this.envObject = null;
        }
        this.isStrict = isStrict;
    }

    /**
     * 根据表达式获取对应的值，按以下优先级查找：
     * <ol>
     *     <li>环境变量（_index、_size、_hasNext 等内置变量）</li>
     *     <li>表达式引擎从数据对象中取值</li>
     *     <li>回退为从根数据 Map 中按完整 key 直接查找</li>
     * </ol>
     *
     * @param el 表达式字符串
     * @return 表达式对应的值，找不到时根据严格模式返回 null 或抛异常
     */
    @Override
    public Object compute(String el) {
        try {
            // 1. 优先从环境变量中取值（_index、_size、_hasNext 等内置变量）
            if (null != envObject && !el.contains("#this")) {
                try {
                    Object val = envObject.eval(el);
                    if (null != val) {
                        return val;
                    }
                } catch (Exception ignored) {
                }
            }
            // 2. 通过表达式引擎从数据对象中取值
            return elObject.eval(el);
        } catch (ExpressionEvalException e) {
            // 3. 表达式引擎解析失败时，回退为从数据源中按完整 key 直接查找
            //    当前支持 Map 类型，如需扩展其他类型（Gson JsonObject、Jackson ObjectNode 等）
            //    在此处增加对应的 instanceof 分支即可，参见类注释中的扩展说明
            if (root instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) root;
                if (map.containsKey(el)) {
                    return map.get(el);
                }
            }

            if (isStrict) {
                throw e;
            }
            return null;
        }
    }
}