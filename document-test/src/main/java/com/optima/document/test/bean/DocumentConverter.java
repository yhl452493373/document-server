package com.optima.document.test.bean;

import com.optima.document.api.DocumentService;
import com.optima.document.test.config.DocumentServiceConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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

        Map<String, Object> params = new HashMap<>();
        params.put("callerName", "张三");

        // 生成word，并转为pdf
        byte[] generatedWord = documentService.generateWord(Files.readAllBytes(sourceFile.toPath()), params);
        byte[] wordedToPdf = documentService.wordToPdf(generatedWord, true);
        Path pdfPath = targetFile.toPath();
        Files.write(pdfPath, wordedToPdf);

        // 生成的word转为图片
        byte[] wordedToImage = documentService.wordToImage(generatedWord, "jpg");
        File imageFile = new File(targetFile.getAbsolutePath().replace(".pdf", ".jpg"));
        if (imageFile.exists()) {
            imageFile.delete();
        }
        Path imagePath = imageFile.toPath();
        Files.write(imagePath, wordedToImage);
    }
}
