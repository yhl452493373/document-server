package com.optima.document.server;

import com.optima.document.api.DocumentService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Disabled
public class JodConverterDocumentServerTest {

    @Test
    public void test() throws IOException {
        DocumentService documentService = buildDocumentService();

        File sourceDocx = new File("/Users/yanghuanglin/Downloads/test.docx");
        File targetPdf = new File("/Users/yanghuanglin/Downloads/test.pdf");

        Map<String, Object> params = new HashMap<>();
        params.put("callerName", "张三");
        byte[] sourceBytes = documentService.generateWord(Files.readAllBytes(sourceDocx.toPath()), params);

        byte[] bytes = documentService.wordToPdf(sourceBytes, false);
        FileUtils.writeByteArrayToFile(targetPdf, bytes);
    }

    private static DocumentService buildDocumentService() {
        // 创建客户端代理
        HttpInvokerProxyFactoryBean factoryBean = new HttpInvokerProxyFactoryBean();
        factoryBean.setServiceUrl("http://127.0.0.1:9005/document-service");
        factoryBean.setServiceInterface(DocumentService.class);
        factoryBean.afterPropertiesSet();

        return (DocumentService) factoryBean.getObject();
    }
}
