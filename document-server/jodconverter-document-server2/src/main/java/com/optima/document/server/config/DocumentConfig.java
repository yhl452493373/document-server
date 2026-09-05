package com.optima.document.server.config;

import com.optima.document.api.DocumentService;
import info.hncy.word.generator.WordGenerator;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "document")
public class DocumentConfig {

    @Bean
    public WordGenerator wordGenerator(){
        return new WordGenerator();
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
