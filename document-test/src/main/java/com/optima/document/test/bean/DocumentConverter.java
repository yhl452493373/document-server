package com.optima.document.test.bean;

import com.optima.document.api.DocumentService;
import com.optima.document.test.config.DocumentServiceConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Component
public class DocumentConverter {
    @Resource
    private DocumentServiceConfig documentServiceConfig;
    @Resource
    private DocumentService documentService;

    @PostConstruct
    public void init() throws IOException {
        String sourceFilePath = documentServiceConfig.getSourceFile();
        String targetFilePath = documentServiceConfig.getTargetFile();

        File sourceFile = new File(sourceFilePath);
        File targetFile = new File(targetFilePath);

        if (targetFile.exists()) {
            targetFile.delete();
        }

        String sourceExtension = sourceFile.getName().substring(sourceFile.getName().lastIndexOf("."));
        String targetExtension = targetFile.getName().substring(targetFile.getName().lastIndexOf("."));

        byte[] converted = documentService.convert(Files.readAllBytes(sourceFile.toPath()), sourceExtension, targetExtension);
        Files.write(targetFile.toPath(), converted);
    }
}
