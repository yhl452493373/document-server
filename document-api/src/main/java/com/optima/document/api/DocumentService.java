package com.optima.document.api;

import java.util.Map;

/**
 * 文档操作接口
 *
 * @author yanghuanglin
 * @since 2022/12/28
 */
public interface DocumentService {

    /**
     * 格式转换
     *
     * @param source          源文件流，仅支持docx格式
     * @param sourceExtension 源文件后缀名，不包含"."
     * @param targetExtension 目标文件后缀名，不包含"."
     * @param targetFormat    目标文件格式，需与目标文件后缀名匹配。
     *                        jodconverter-document-server 下此参数无效。
     *                        docto-document-server 下参考:
     *                        <a href="https://docs.microsoft.com/en-us/dotnet/api/microsoft.office.interop.word.wdsaveformat">word格式</a>
     *                        或 <a href="https://docs.microsoft.com/en-us/dotnet/api/microsoft.office.interop.excel.xlfileformat">excel格式</a>
     *                        或 <a href="https://docs.microsoft.com/en-us/office/vba/api/powerpoint.presentation.saveas">powerpoint格式</a>
     * @return 转换后的文件流
     */
    default byte[] convert(byte[] source, String sourceExtension, String targetExtension, String targetFormat) {
        throw new UnsupportedOperationException();
    }

    /**
     * 通过调用poi生成word
     *
     * @param sourceTemplate word模版流，仅支持docx格式
     * @param dataModel      数据模型
     * @return word文档流
     */
    default byte[] generateWord(byte[] sourceTemplate, Map<String, Object> dataModel) {
        throw new UnsupportedOperationException();
    }

    /**
     * 通过调用poi将word转pdf，如果clear为true，则仅支持docx格式
     *
     * @param source word模版流，仅支持docx格式
     * @param clear  是否清除占位符
     * @return pdf文档流
     */
    default byte[] wordToPdf(byte[] source, boolean clear) {
        throw new UnsupportedOperationException();
    }

    /**
     * 通过pdfbox将word转图片，仅支持docx格式
     *
     * @param source          word文件流，仅支持docx格式
     * @param targetExtension 目标格式 支持jpeg, jpg, gif, tiff or png
     * @return 图片流
     */
    default byte[] wordToImage(byte[] source, String targetExtension) {
        throw new UnsupportedOperationException();
    }

    /**
     * doc转为docx
     *
     * @param source doc文档流
     * @return docx文档流
     */
    default byte[] docToDocx(byte[] source) {
        throw new UnsupportedOperationException();
    }

    /**
     * xls转为xlsx
     *
     * @param source xls文档流
     * @return xlsx文档流
     */
    default byte[] xlsToXlsx(byte[] source) {
        throw new UnsupportedOperationException();
    }

    /**
     * 获取模板语法配置
     * <ul>
     * <li>
     * 模板语法参考：<a href="https://deepoove.com/poi-tl/">POI-TL语法</a>
     * </li>
     * <li>
     * 这里的配置会将原来的 {{ 替换为{@link Gramer#getPrefix()}， }} 替换为{@link Gramer#getSuffix()}
     * </li>
     * <li>
     * 增加以{@link Gramer#getCustomizeList()}开头的列表对象处理插件，其字符串类型列表分隔符为{@link Gramer#getCustomizeListStringDelimiting()}
     * </li>
     * </ul>
     *
     * @return 模板语法配置
     */
    default Gramer gramer() {
        return new Gramer();
    }
}
