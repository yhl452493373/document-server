package com.optima.document.tl.server.api;

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
import com.optima.document.tl.server.config.DocumentConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 服务接口实现
 * @author Elias
 * @date 2021-09-28 16:18
 */
@Slf4j
@Component
public class DocumentServiceImpl implements DocumentService {

    /**
     * word模版引擎配置
     */
    private static final Configure wtlConfig = Configure.builder().buildGramer("${", "}")
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

    @Resource
    private DocumentConfig documentConfig;

    @Override
    public byte[] generateWord(byte[] templateData, Map<String, Object> dataModel) {
        long start = System.currentTimeMillis();
        XWPFTemplate template = XWPFTemplate.compile(new ByteArrayInputStream(templateData), wtlConfig).render(dataModel);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()){
            template.write(bos);
            log.info("word generate========consuming：{} milliseconds", System.currentTimeMillis() - start);
            return bos.toByteArray();
        } catch (Exception e) {
            log.error("word generate error", e);
            return null;
        }
    }

    @Override
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
                    log.info("清空占位符=======耗时：{} 毫秒", System.currentTimeMillis() - start);
                } catch (Exception e) {
                    log.error("清空标签失败：", e);
                    return null;
                }
            }
            Path tempFilePath = Files.createTempFile(UUID.randomUUID().toString(), ".docx");
            Files.write(tempFilePath, source);

            Path pdfPath = Files.createTempFile(UUID.randomUUID().toString(), ".pdf");

            try {
                long begin = System.currentTimeMillis();
                String command = "%s -f %s -O %s -T wdFormatPDF";
                command = String.format(command, documentConfig.getDocToProgram(), tempFilePath, pdfPath);
                Process p = Runtime.getRuntime().exec(command);
                p.waitFor();
                long end = System.currentTimeMillis();
                log.info("docto take time in millis:" + (end - begin));
                return Files.readAllBytes(pdfPath);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                Files.delete(tempFilePath);
                Files.delete(pdfPath);
            }
        } catch (Exception e) {
            log.error("word to pdf error", e);
            return null;
        }
    }

    @Override
    public byte[] wordToImage(byte[] source, String targetFormat) {
        try {
            byte[] pdfBytes = wordToPdf(source, true);
            long start = System.currentTimeMillis();
            PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(
                    0, 300, ImageType.RGB);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ImageIOUtil.writeImage(bim, targetFormat, bos, 300);
            document.close();
            log.info("word to image=======consuming：{} milliseconds", System.currentTimeMillis() - start);

            return bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
