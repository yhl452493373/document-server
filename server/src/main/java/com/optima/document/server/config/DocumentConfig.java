package com.optima.document.server.config;

import com.optima.document.api.DocumentService;
import com.optima.document.server.api.DocumentServiceImpl;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

/**
 * 服务端配置
 * @author Elias
 * @date 2021-09-28 16:12
 */
@Configuration
@ConfigurationProperties(prefix = "document")
@Data
public class DocumentConfig {

    private String tempDir;

    private String openOfficeHome;

    /**
     * 文档接口
     * @return httpinvoker
     */
    @Bean(name = "/document-service")
    HttpInvokerServiceExporter wordService() {
        HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
        exporter.setService( new DocumentServiceImpl() );
        exporter.setServiceInterface(DocumentService.class);
        return exporter;
    }

}
