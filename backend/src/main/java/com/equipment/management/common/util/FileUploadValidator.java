package com.equipment.management.common.util;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.enums.FileCategory;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.config.FileUploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Locale;

@Component
@RequiredArgsConstructor
/** 根据文件类别、扩展名和大小限制校验上传文件。 */
public class FileUploadValidator {

    private final FileUploadProperties fileUploadProperties;

    public void validate(MultipartFile file, FileCategory category) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "上传文件不能为空");
        }
        if (file.getSize() > fileUploadProperties.getMaxSize()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件大小超出限制");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        if (!isAllowedExtension(extension, category)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "不允许的文件类型: " + extension + "，当前分类: " + category.getLabel());
        }
    }

    public String resolveExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件格式不支持");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedExtension(String extension, FileCategory category) {
        String configured = fileUploadProperties.getCategories().get(category.getCode());
        if (StringUtils.hasText(configured)) {
            return Arrays.asList(configured.split(",")).contains(extension);
        }
        return category.isAllowedExtension(extension);
    }
}
