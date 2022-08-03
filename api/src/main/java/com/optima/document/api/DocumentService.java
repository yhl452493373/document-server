package com.optima.document.api;

import java.util.List;
import java.util.Map;

/**
 * 文档接口
 * @author Elias
 * @date 2021-09-28 16:00
 */
public interface DocumentService {
    /**
     * generate word
     * @param templateData word模版流
     * @param dataModel 数据模型
     * @return 修改后的文档流
     */
    default byte[] generateWord(byte[] templateData, Map<String, Object> dataModel) {
        throw new UnsupportedOperationException();
    }

    /**
     * word to pdf
     * @param templateData word模版流
     * @param clear 是否清除占位符
     * @return
     */
    default byte[] wordToPdf(byte[] templateData, boolean clear) {
        throw new UnsupportedOperationException();
    }

    /**
     * word to image
     * @param templateData word模版流
     * @param targetFormat 目标格式 支持jpeg, jpg, gif, tiff or png
     * @return
     */
    default byte[] wordToImage(byte[] templateData, String targetFormat) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param source 文档
     * @param toFindText 需要替换的文本
     * @param imgSource 图片
     * @param width 宽度
     * @param height 高度
     * @return 修改后的文档
     */
    default byte[] insertJpeg(byte[] source, String toFindText, byte[] imgSource, int width, int height){
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param source
     * @param toFindText
     * @param imgSource
     * @param width
     * @param height
     * @return
     */
    default byte[] insertJpeg(byte[] source, String toFindText, List<byte[]> imgSource, int width, int height) {
        throw new UnsupportedOperationException();
    }

    default byte[] fieldToWord(byte[] source, Map<String, Object> infoMap) {
        throw new UnsupportedOperationException();
    }
}
