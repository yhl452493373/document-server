package com.optima.document.test.config;

import com.optima.document.api.DocumentService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "document")
public class DocumentServiceConfig {
    private String server;
    private String sourceFile;
    private String targetFile;

    @SuppressWarnings("deprecation")
    @Bean
    public DocumentService documentService() {
        // 创建客户端代理
        HttpInvokerProxyFactoryBean factoryBean = new HttpInvokerProxyFactoryBean();

        String serviceUrl;
        if (server.endsWith("/")) {
            serviceUrl = server + "document-service";
        } else {
            serviceUrl = server + "/document-service";
        }
        factoryBean.setServiceUrl(serviceUrl);
        factoryBean.setServiceInterface(DocumentService.class);
        factoryBean.afterPropertiesSet();

        return (DocumentService) factoryBean.getObject();
    }
}
