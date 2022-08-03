package com.optima.document.tl.server.config;

import com.optima.document.api.DocumentService;
import com.optima.document.tl.server.api.DocumentServiceImpl;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 服务端配置
 * @author Elias
 * @date 2021-09-28 16:12
 */
@Configuration
@ConfigurationProperties(prefix = "document")
@Data
public class DocumentConfig {

    private String docToProgram;

    /**
     * 文档接口
     * @return httpinvoker
     */
    @Bean(name = "/document-service")
    HttpInvokerServiceExporter wordService(DocumentService documentService) {
        HttpInvokerServiceExporter exporter = new HttpInvokerServiceExporter();
        exporter.setService( documentService );
        exporter.setServiceInterface(DocumentService.class);
        return exporter;
    }

}
