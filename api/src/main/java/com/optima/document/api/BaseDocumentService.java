package com.optima.document.api;

/**
 * @author yanghuanglin
 * @since 2022/12/28
 */
public interface BaseDocumentService {
    /**
     * 格式转换，server和tl-server下均实现
     *
     * @param source          源文件流，tl-server下仅支持docx格式
     * @param sourceExtension 源文件后缀名，不包含"."
     * @param targetExtension 目标文件后缀名，不包含"."
     * @param targetFormat    目标文件格式，需与目标文件后缀名匹配，server下此参数无效。，tl-server下参考: <a href="https://docs.microsoft.com/en-us/dotnet/api/microsoft.office.interop.word.wdsaveformat">word格式</a>
     *                        或 <a href="https://docs.microsoft.com/en-us/dotnet/api/microsoft.office.interop.excel.xlfileformat">excel格式</a>
     *                        或 <a href="https://docs.microsoft.com/en-us/office/vba/api/powerpoint.presentation.saveas">powerpoint格式</a>
     * @return 转换后的文件流
     */
    default byte[] convert(byte[] source, String sourceExtension, String targetExtension, String targetFormat) {
        throw new UnsupportedOperationException();
    }

    /**
     * word转为pdf，server和tl-server下均实现
     *
     * @param source       word文件流，tl-server下仅支持docx格式
     * @param sourceFormat 源文件后缀名，不包含"."，tl-server下此参数无效
     * @param clear        是否清除占位符，如果为true，则在tl-server下源文件只支持docx格式
     * @return pdf文档流
     */
    default byte[] wordToPdf(byte[] source, String sourceFormat, boolean clear) {
        throw new UnsupportedOperationException();
    }

    /**
     * 通过pdfbox将word转图片
     *
     * @param source          word文件流，tl-server下仅支持docx格式
     * @param sourceExtension 源文件后缀名，不包含"."，server下此参数无效
     * @param targetExtension 目标格式 支持jpeg, jpg, gif, tiff or png
     * @return 图片流
     */
    default byte[] wordToImage(byte[] source, String sourceExtension, String targetExtension) {
        throw new UnsupportedOperationException();
    }

    /**
     * doc转为docx，server和tl-server下均实现
     *
     * @param source doc文档流
     * @return docx文档流
     */
    default byte[] docToDocx(byte[] source) {
        throw new UnsupportedOperationException();
    }

    /**
     * xls转为xlsx，server和tl-server下均实现
     *
     * @param source xls文档流
     * @return xlsx文档流
     */
    default byte[] xlsToXlsx(byte[] source) {
        throw new UnsupportedOperationException();
    }
}
