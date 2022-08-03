package com.optima.document.server.api;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;
import com.optima.document.api.DocumentService;
import com.optima.document.server.utils.DocToPdfUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * 服务接口实现
 * @author Elias
 * @date 2021-09-28 16:18
 */
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    public byte[] fieldToWord(byte[] source, Map<String, Object> infoMap) {
        return DocToPdfUtil.fieldToWord(source, infoMap);
    }

    public byte[] insertJpeg(byte[] source, String toFindText, byte[] imgSource, int width, int height) {
        return DocToPdfUtil.insertJpeg(source, toFindText, imgSource, width, height);
    }

    public byte[] insertJpeg(byte[] source, String toFindText, List<byte[]> imgSource, int width, int height) {
        return DocToPdfUtil.insertJpeg(source, toFindText, imgSource, width, height);
    }

    public byte[] wordToPdf(byte[] source, String sourceFormat, boolean clear) {
        try {
            if (clear) {
                source = DocToPdfUtil.clearPlaceholder(source);
            }
            long t1 = System.currentTimeMillis();
            String command = "D:\\OpenOffice4\\program\\soffice.exe -headless -accept=\"socket,host=127.0.0.1,port=8100;urp;\" -nofirststartwizard";
            Process p = Runtime.getRuntime().exec(command);
            OpenOfficeConnection connection = new SocketOpenOfficeConnection();
            connection.connect();
            DocumentConverter converter = new OpenOfficeDocumentConverter(connection);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DefaultDocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();
            converter.convert(new ByteArrayInputStream(source), formatRegistry.getFormatByFileExtension(sourceFormat), bos, formatRegistry.getFormatByFileExtension("pdf"));
            connection.disconnect();
            p.destroy();
            log.info("word to pdf=======consuming：{} milliseconds", System.currentTimeMillis() - t1);
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] wordToImage(byte[] source, String sourceFormat, String targetFormat) {
        try {
            byte[] pdfBytes = wordToPdf(source, sourceFormat, true);
            PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(
                    0, 300, ImageType.RGB);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIOUtil.writeImage(bim, targetFormat, bos, 300);
            document.close();
            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
