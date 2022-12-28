package com.optima.document.server.config;

import com.optima.document.api.LegacyDocumentService;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

/**
 * 服务端配置
 *
 * @author Elias
 * @since 2021-09-28 16:12
 */
@SuppressWarnings("VulnerableCodeUsages")
@Configuration
@ConfigurationProperties(prefix = "document")
@Data
public class DocumentConfig {

    private String tempDir;

    private String openOfficeHome;

    /**
     * 文档接口
     *
     * @return httpinvoker
     */
    @Bean(name = "/document-service")
    HttpInvokerServiceExporter wordService(LegacyDocumentService legacyDocumentService) {
        HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
        exporter.setService(legacyDocumentService);
        exporter.setServiceInterface(LegacyDocumentService.class);
        return exporter;
    }

}
