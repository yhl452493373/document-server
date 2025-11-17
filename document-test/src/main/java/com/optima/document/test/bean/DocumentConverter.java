package com.optima.document.test.bean;

import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.Pictures;
import com.optima.document.api.DocumentService;
import com.optima.document.test.config.DocumentServiceConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        List<String> departmentList = new ArrayList<>();
        departmentList.add("处置部门1");
        departmentList.add("处置部门2");
        params.put("departmentList", departmentList);

        byte[] bytes = Files.readAllBytes(Paths.get("/Users/yanghuanglin/Downloads/02.png"));

        params.put("img", Pictures.ofBytes(bytes).size(48, 27).create());

        List<PictureRenderData> imgList = new ArrayList<>();
        imgList.add(Pictures.ofBytes(bytes).size(48, 27).create());
        imgList.add(Pictures.ofBytes(bytes).size(48, 27).create());
        params.put("imgList", imgList);

        params.put("urlImg",Pictures.ofUrl("https://xct.cdhncy.cn/file/city/2025/10/28/fa03c40bdfc64a4f9e1241ebda6b3fda_1761641676690.jpeg").create());

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
