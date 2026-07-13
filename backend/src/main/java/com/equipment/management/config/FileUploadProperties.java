package com.equipment.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadProperties {

    private long maxSize = 52_428_800L;
    private String allowedExtensions = "jpg,jpeg,png,gif,webp,bmp,pdf,doc,docx,txt,xls,xlsx";
    private Map<String, String> categories = defaultCategories();

    private static Map<String, String> defaultCategories() {
        Map<String, String> map = new HashMap<>();
        map.put("image", "jpg,jpeg,png,gif,webp,bmp");
        map.put("document", "pdf,doc,docx,txt");
        map.put("excel", "xls,xlsx");
        map.put("contract", "pdf,doc,docx");
        return map;
    }
}
