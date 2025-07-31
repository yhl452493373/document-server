package com.optima.document.api;

import lombok.Getter;
import lombok.Setter;

/**
 * 文书模板中的语法标签
 */
@Getter
@Setter
public class Gramer {
    /// 模板语法开始字符串
    private String prefix = "${";
    /// 模板语法结束字符串
    private String suffix = "}";
    /// 自定义列表对象标签
    private Character customizeList = '%';
    /// 自定义列表对象标签处理字符串列表时的分隔符
    private String customizeListStringDelimiting = "，";
}
