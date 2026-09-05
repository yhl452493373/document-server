package com.optima.document.server;

import com.optima.document.api.DocumentService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Disabled
public class JodConverterDocumentServer2Test {

    @Test
    public void test() throws IOException {
        DocumentService documentService = buildDocumentService();

        File sourceDocx = new File("/Users/yanghuanglin/Downloads/test.docx");
        File targetPdf = new File("/Users/yanghuanglin/Downloads/test.pdf");

        Map<String, Object> params = new HashMap<>();
        params.put("p",true);
        params.put("CaseInfo.name","zhangsan");

        byte[] sourceBytes = documentService.generateWord(Files.readAllBytes(sourceDocx.toPath()), params);

        byte[] bytes = documentService.wordToPdf(sourceBytes, true);
        Files.write(Paths.get(targetPdf.getPath()),bytes);
    }

    private static DocumentService buildDocumentService() {
        // 创建客户端代理
        HttpInvokerProxyFactoryBean factoryBean = new HttpInvokerProxyFactoryBean();
        factoryBean.setServiceUrl("http://127.0.0.1:9004/document-service");
        factoryBean.setServiceInterface(DocumentService.class);
        factoryBean.afterPropertiesSet();

        return (DocumentService) factoryBean.getObject();
    }
}
