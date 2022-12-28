package com.optima.document.api;

/**
 * @author yanghuanglin
 * @since 2022/12/28
 */
public interface BaseDocumentService {
    /**
     * 格式转换，server和tl-server下均实现
     *
     * @param sourceData      源文件流，tl-server下仅支持docx格式
     * @param sourceExtension 源文件后缀名，不包含"."
     * @param targetExtension 目标文件后缀名，不包含"."
     * @param targetFormat    目标文件格式
     * @return 转换后的文件流
     */
    default byte[] convert(byte[] sourceData, String sourceExtension, String targetExtension, String targetFormat) {
        throw new UnsupportedOperationException();
    }

    /**
     * 通word转pdf，server和tl-server下均实现
     *
     * @param source       源文件流，tl-server下仅支持docx格式
     * @param sourceFormat 源文件后缀名，不包含"."，tl-server下可忽略
     * @param clear        是否清除占位符
     * @return pdf文档流
     */
    default byte[] wordToPdf(byte[] source, String sourceFormat, boolean clear) {
        throw new UnsupportedOperationException();
    }

    /**
     * doc转为docx，server和tl-server下均实现
     *
     * @param docData doc文档流
     * @return docx文档流
     */
    default byte[] docToDocx(byte[] docData) {
        throw new UnsupportedOperationException();
    }

    /**
     * xls转为xlsx，server和tl-server下均实现
     *
     * @param xlsData xls文档流
     * @return xlsx文档流
     */
    default byte[] xlsToXlsx(byte[] xlsData) {
        throw new UnsupportedOperationException();
    }
}
