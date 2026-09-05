package com.optima.document.server.api;

import com.optima.document.api.DocumentService;
import info.hncy.word.generator.WordGenerator;
import info.hncy.word.generator.support.PlaceholderCleaner;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

/**
 * 服务接口实现
 *
 * @author Elias
 * @since 2021-09-28 16:18
 */
@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {
    @Resource
    private DocumentConverter documentConverter;
    @Resource
    private WordGenerator wordGenerator;

    /**
     * 文件格式转换
     *
     * @param source          源文件流
     * @param sourceExtension 源文件后缀名，不包含"."
     * @param targetExtension 目标文件后缀名
     * @return 转换后的文件流
     */
    private byte[] convert(byte[] source, String sourceExtension, String targetExtension) {
        try {
            sourceExtension = sourceExtension.replace(".", "");
            targetExtension = targetExtension.replace(".", "");
            long start = System.currentTimeMillis();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DocumentFormat sourceFormat = DefaultDocumentFormatRegistry.getFormatByExtension(sourceExtension);
            DocumentFormat targetFormat = DefaultDocumentFormatRegistry.getFormatByExtension(targetExtension);
            assert sourceFormat != null;
            assert targetFormat != null;
            documentConverter.convert(new ByteArrayInputStream(source))
                    .as(sourceFormat)
                    .to(bos)
                    .as(targetFormat)
                    .execute();
            log.info("convert {} to {} take time in millis:{}", sourceExtension, targetExtension, System.currentTimeMillis() - start);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("convert {} to {} error", sourceExtension, targetExtension, e);
        }
        return null;
    }

    public byte[] convert(byte[] source, String sourceExtension, String targetExtension, String targetFormat) {
        return convert(source, sourceExtension, targetExtension);
    }

    public byte[] generateWord(byte[] sourceTemplate, Map<String, Object> dataModel) {
        long start = System.currentTimeMillis();
        try {
            byte[] generated = wordGenerator.generate(sourceTemplate, dataModel);
            log.info("word generate take time in millis:{}", System.currentTimeMillis() - start);
            return generated;
        } catch (Exception e) {
            log.error("word generate error", e);
            return null;
        }
    }

    public byte[] wordToPdf(byte[] source, boolean clear) {
        try {
            long start = System.currentTimeMillis();
            if (clear) {
                try {
                    source = WordGenerator.clearPlaceholders(source);
                    log.info("clear placeholder take time in millis:{}", System.currentTimeMillis() - start);
                } catch (Exception e) {
                    log.error("clear placeholder error", e);
                    return null;
                }
            }
            return convert(source, "docx", "pdf");
        } catch (Exception e) {
            log.error("word to pdf error", e);
            return null;
        }
    }

    public byte[] wordToImage(byte[] source, String targetExtension) {
        try {
            byte[] pdfBytes = wordToPdf(source, true);
            long start = System.currentTimeMillis();
            PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIOUtil.writeImage(bim, targetExtension, bos, 300);
            document.close();
            log.info("word to image take time in millis:{}", System.currentTimeMillis() - start);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("word to image error", e);
        }
        return null;
    }

    public byte[] docToDocx(byte[] source) {
        return convert(source, "doc", "docx");
    }

    public byte[] xlsToXlsx(byte[] source) {
        return convert(source, "xls", "xlsx");
    }
}
