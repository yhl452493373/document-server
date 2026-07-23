package com.optima.document.server.config;

import com.deepoove.poi.config.Configure;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.TextRenderData;
import com.deepoove.poi.policy.ListRenderPolicy;
import com.deepoove.poi.policy.PictureRenderPolicy;
import com.deepoove.poi.render.RenderContext;
import com.optima.document.api.DocumentService;
import com.optima.document.api.Gramer;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 服务端配置
 *
 * @author Elias
 * @since 2021-09-28 16:12
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "document")
public class DocumentConfig {
    @NestedConfigurationProperty
    private Gramer gramer;
    private double minInflateRatio = 0d;

    /**
     * word模版引擎配置
     */
    @Bean(name = "wtlConfig")
    public Configure wtlConfig() {
        ZipSecureFile.setMinInflateRatio(minInflateRatio);
        return Configure.builder().buildGramer(gramer.getPrefix(), gramer.getSuffix())
                .setValidErrorHandler(new Configure.DiscardHandler())
                .addPlugin(gramer.getCustomizeList(), new ListRenderPolicy() {
                    @Override
                    public void doRender(RenderContext<List<Object>> context) throws Exception {
                        XWPFRun run = context.getRun();
                        List<?> dataList = context.getData();
                        Iterator<?> iterator = dataList.iterator();
                        while (iterator.hasNext()) {
                            Object data = iterator.next();
                            if (data instanceof String) {
                                // 纯文本类型
                                run.setText(data.toString());
                                if (iterator.hasNext()) {
                                    run.setText(gramer.getCustomizeListStringDelimiting());
                                }
                            } else if (data instanceof TextRenderData) {
                                // poi的文本类型
                                run.setText(((TextRenderData) data).getText());
                                if (iterator.hasNext()) {
                                    run.setText(gramer.getCustomizeListStringDelimiting());
                                }
                            } else if (data instanceof PictureRenderData) {
                                // poi的图片类型
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
                                @SuppressWarnings("rawtypes") Map dataMap = ((Map) data);
                                if (dataMap.containsKey(el)) {
                                    return dataMap.get(el);
                                }
                            }
                            return null;
                        })
                .build();
    }

    /**
     * 文档接口
     *
     * @return httpinvoker
     */
    @SuppressWarnings("deprecation")
    @Bean(name = "/document-service")
    public HttpInvokerServiceExporter wordService(DocumentService documentService) {
        HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
        exporter.setService(documentService);
        exporter.setServiceInterface(DocumentService.class);
        return exporter;
    }

}
