package com.optima.document.api;

/**
 * 文档接口，此类中用的poi2生成word文档，使用docto进行格式转换
 *
 * @author yanghuanglin
 * @since 2022/12/28
 */
public interface DocumentService extends BaseDocumentService {
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
}
