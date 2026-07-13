package com.equipment.management.common.enums;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum FileCategory {

    IMAGE("image", "图片", "DEVICE_PHOTO", "jpg,jpeg,png,gif,webp,bmp"),
    DOCUMENT("document", "文档", "OTHER_DOC", "pdf,doc,docx,txt"),
    EXCEL("excel", "Excel", "EXCEL_DATA", "xls,xlsx"),
    CONTRACT("contract", "合同", "PURCHASE_CONTRACT", "pdf,doc,docx");

    private final String code;
    private final String label;
    private final String defaultFileTypeCode;
    private final String defaultExtensions;

    FileCategory(String code, String label, String defaultFileTypeCode, String defaultExtensions) {
        this.code = code;
        this.label = label;
        this.defaultFileTypeCode = defaultFileTypeCode;
        this.defaultExtensions = defaultExtensions;
    }

    public static FileCategory fromCode(String code) {
        if (code == null || code.isBlank()) {
            return DOCUMENT;
        }
        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code.trim()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.BAD_REQUEST, "不支持的文件分类: " + code));
    }

    public boolean isAllowedExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return false;
        }
        String normalized = extension.toLowerCase(Locale.ROOT);
        return Arrays.asList(defaultExtensions.split(",")).contains(normalized);
    }
}
