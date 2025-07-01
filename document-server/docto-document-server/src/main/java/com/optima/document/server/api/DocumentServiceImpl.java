package com.optima.document.server.api;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.deepoove.poi.policy.ListRenderPolicy;
import com.deepoove.poi.policy.PictureRenderPolicy;
import com.deepoove.poi.render.RenderContext;
import com.deepoove.poi.template.run.RunTemplate;
import com.deepoove.poi.xwpf.BodyContainer;
import com.deepoove.poi.xwpf.BodyContainerFactory;
import com.optima.document.api.DocumentService;
import com.optima.document.server.config.DocumentConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 服务接口实现
 *
 * @author Elias
 * @since 2021-09-28 16:18
 */
@SuppressWarnings("rawtypes")
@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {
    @Resource
    private DocumentConfig documentConfig;

    /**
     * word模版引擎配置
     */
    Configure wtlConfig = Configure.builder().buildGramer("${", "}")
            .setValidErrorHandler(new Configure.DiscardHandler())
            .addPlugin('%', new ListRenderPolicy() {
                @Override
                public void doRender(RenderContext<List<Object>> context) throws Exception {
                    XWPFRun run = context.getRun();
                    List<?> dataList = context.getData();
                    Iterator<?> var5 = dataList.iterator();
                    while (var5.hasNext()) {
                        Object data = var5.next();
                        if (data instanceof TextRenderData) {
                            run.setText(((TextRenderData) data).getText());
                            if (var5.hasNext()) {
                                run.setText("，");
                            }
                        } else if (data instanceof PictureRenderData) {
                            PictureRenderPolicy.Helper.renderPicture(run, (PictureRenderData) data);
                        }

                    }
                }

            })
            .setRenderDataComputeFactory(envModel ->
                    el -> {
                        Object data = envModel.getRoot();
                        if ("#this".equals(el)) {
                            return data;
                        } else if (data instanceof Map) {
                            Map dataMap = ((Map) data);
                            if (dataMap.containsKey(el)) {
                                return dataMap.get(el);
                            }
                        }
                        return null;
                    })
            .build();

    public byte[] convert(byte[] source, String sourceExtension, String targetExtension, String targetFormat) {
        try {
            sourceExtension = sourceExtension.replace(".", "");
            targetExtension = targetExtension.replace(".", "");
            Path sourcePath = Files.createTempFile(UUID.randomUUID().toString(), "." + sourceExtension);
            Path targetPath = Files.createTempFile(UUID.randomUUID().toString(), "." + targetExtension);
            Files.write(sourcePath, source);
            try {
                long begin = System.currentTimeMillis();
                String command;
                if (sourceExtension.contains("xls")) {
                    command = "%s -XL -f %s -O %s -T %s";
                } else if (sourceExtension.contains("ppt")) {
                    command = "%s -PP -f %s -O %s -T %s";
                } else {
                    command = "%s -WD -f %s -O %s -T %s";
                }
                command = String.format(command, documentConfig.getDocToProgram(), sourcePath, targetPath, targetFormat);
                Process p = Runtime.getRuntime().exec(command);
                p.waitFor();
                long end = System.currentTimeMillis();
                log.info("convert {} to {} take time in millis:{}", sourceExtension, targetExtension, (end - begin));
                return Files.readAllBytes(targetPath);
            } catch (Exception e) {
                log.error("convert {} to {} error", sourceExtension, targetExtension, e);
                return null;
            } finally {
                Files.delete(sourcePath);
                Files.delete(targetPath);
            }
        } catch (IOException e) {
            log.error("convert error", e);
        }
        return null;
    }

    public byte[] generateWord(byte[] sourceTemplate, Map<String, Object> dataModel) {
        long start = System.currentTimeMillis();
        XWPFTemplate template = XWPFTemplate.compile(new ByteArrayInputStream(sourceTemplate), wtlConfig).render(dataModel);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            template.write(bos);
            log.info("word generate take time in millis:{}", System.currentTimeMillis() - start);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("word generate error", e);
            return null;
        }
    }

    public byte[] wordToPdf(byte[] source, String sourceFormat, boolean clear) {
        return wordToPdf(source, clear);
    }

    public byte[] wordToPdf(byte[] source, boolean clear) {
        try {
            long start = System.currentTimeMillis();
            if (clear) {
                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    XWPFTemplate template = XWPFTemplate.compile(new ByteArrayInputStream(source), wtlConfig);
                    BodyContainer bodyContainer = BodyContainerFactory.getBodyContainer(template.getXWPFDocument());
                    template.getElementTemplates().forEach(it -> {
                        if (it instanceof RunTemplate) {
                            RunTemplate rt = (RunTemplate) it;
                            bodyContainer.clearPlaceholder(rt.getRun());
                        }
                    });
                    template.writeAndClose(bos);
                    source = bos.toByteArray();
                    log.info("clear placeholder take time in millis:{}", System.currentTimeMillis() - start);
                } catch (Exception e) {
                    log.error("clear placeholder error", e);
                    return null;
                }
            }
            return convert(source, "docx", "pdf", "wdFormatPDF");
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

    public byte[] wordToImage(byte[] source, String sourceExtension, String targetExtension) {
        return wordToImage(source, targetExtension);
    }

    public byte[] docToDocx(byte[] source) {
        return convert(source, "doc", "docx", "wdFormatDocumentDefault");
    }

    public byte[] xlsToXlsx(byte[] source) {
        return convert(source, "xls", "xlsx", "xlWorkbookDefault");
    }
}
