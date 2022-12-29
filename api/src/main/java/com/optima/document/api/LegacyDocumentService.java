package com.optima.document.api;

import java.util.List;
import java.util.Map;

/**
 * 文档接口，此类中用的jacob生成word文档，插入图片等，使用openoffice进行格式转换
 *
 * @author Elias
 * @since 2021-09-28 16:00
 */
public interface LegacyDocumentService extends BaseDocumentService {
    /**
     * 通过jacob向文档中插入图片，仅server模块下实现
     *
     * @param source     文档流
     * @param toFindText 需要替换的文本
     * @param imgSource  图片流
     * @param width      宽度
     * @param height     高度
     * @return word文档流
     */
    default byte[] insertJpeg(byte[] source, String toFindText, byte[] imgSource, int width, int height) {
        throw new UnsupportedOperationException();
    }

    /**
     * 通过jacob向文档中插入多张图片，仅server模块下实现
     *
     * @param source     文档流
     * @param toFindText 需要替换的文本
     * @param imgSource  图片流列表
     * @param width      宽度
     * @param height     高度
     * @return word文档流
     */
    default byte[] insertJpeg(byte[] source, String toFindText, List<byte[]> imgSource, int width, int height) {
        throw new UnsupportedOperationException();
    }

    /**
     * 通过jacob向word填充属性字段，仅server模块下实现
     *
     * @param source  文档流
     * @param infoMap 字段集合，${key}为占位字符，value为对应替换内容
     * @return word文档流
     */
    default byte[] fieldToWord(byte[] source, Map<String, Object> infoMap) {
        throw new UnsupportedOperationException();
    }
}
