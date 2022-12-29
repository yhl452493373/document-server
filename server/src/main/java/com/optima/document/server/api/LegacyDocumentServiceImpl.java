package com.optima.document.server.api;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.optima.document.api.LegacyDocumentService;
import com.optima.document.server.config.DocumentConfig;
import com.optima.document.server.utils.DocToPdfUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * 服务接口实现
 *
 * @author Elias
 * @since 2021-09-28 16:18
 */
@Slf4j
@Service
public class LegacyDocumentServiceImpl implements LegacyDocumentService {
    @Resource
    private DocumentConfig documentConfig;

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
            sourceExtension = sourceExtension.replace(".","");
            targetExtension = targetExtension.replace(".","");
            long start = System.currentTimeMillis();
            String command = "%s -headless -accept=\"socket,host=127.0.0.1,port=8100;urp;\" -nofirststartwizard";
            Process p = Runtime.getRuntime().exec(String.format(command, documentConfig.getOpenOfficeHome()));
            OpenOfficeConnection connection = new SocketOpenOfficeConnection();
            connection.connect();
            DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DefaultDocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();
            converter.convert(new ByteArrayInputStream(source), formatRegistry.getFormatByFileExtension(sourceExtension), bos, formatRegistry.getFormatByFileExtension(targetExtension));
            connection.disconnect();
            p.destroy();
            log.info("openoffice convert {} to {} take time in millis:{}", sourceExtension, targetExtension, System.currentTimeMillis() - start);
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] fieldToWord(byte[] source, Map<String, Object> infoMap) {
        return DocToPdfUtil.fieldToWord(source, infoMap);
    }

    public byte[] insertJpeg(byte[] source, String toFindText, byte[] imgSource, int width, int height) {
        return DocToPdfUtil.insertJpeg(source, toFindText, imgSource, width, height);
    }

    public byte[] insertJpeg(byte[] source, String toFindText, List<byte[]> imgSource, int width, int height) {
        return DocToPdfUtil.insertJpeg(source, toFindText, imgSource, width, height);
    }

    public byte[] wordToPdf(byte[] source, String sourceExtension, boolean clear) {
        try {
            if (clear) {
                source = DocToPdfUtil.clearPlaceholder(source);
            }
            return convert(source, sourceExtension, "pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] convert(byte[] source, String sourceExtension, String targetExtension, String targetFormat) {
        return convert(source, sourceExtension, targetExtension);
    }

    public byte[] wordToImage(byte[] source, String sourceExtension, String targetExtension) {
        try {
            byte[] pdfBytes = wordToPdf(source, sourceExtension, true);
            PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(
                    0, 300, ImageType.RGB);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIOUtil.writeImage(bim, targetExtension, bos, 300);
            document.close();
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
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
