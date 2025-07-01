package com.optima.document.server.config;

import com.optima.document.api.DocumentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

/**
 * 服务端配置
 *
 * @author Elias
 * @since 2021-09-28 16:12
 */
@Configuration
public class DocumentConfig {
    /**
     * 文档接口
     *
     * @return httpinvoker
     */
    @SuppressWarnings("deprecation")
    @Bean(name = "/document-service")
    HttpInvokerServiceExporter wordService(DocumentService documentService) {
        HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
        exporter.setService(documentService);
        exporter.setServiceInterface(DocumentService.class);
        return exporter;
    }

}
