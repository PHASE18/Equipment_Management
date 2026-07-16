package com.equipment.management.common.util;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.enums.FileCategory;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.config.FileUploadProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FileUploadValidatorTest {

    private FileUploadValidator validator;

    @BeforeEach
    void setUp() {
        FileUploadProperties properties = new FileUploadProperties();
        properties.setMaxSize(1024L);
        validator = new FileUploadValidator(properties);
    }

    @Test
    @DisplayName("空文件应校验失败")
    void validate_emptyFile_shouldFail() {
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[0]);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(file, FileCategory.DOCUMENT));
        assertEquals(ErrorCode.BAD_REQUEST.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("不能为空"));
    }

    @Test
    @DisplayName("超大文件应校验失败")
    void validate_oversized_shouldFail() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "big.pdf", "application/pdf", new byte[2048]);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(file, FileCategory.DOCUMENT));
        assertTrue(ex.getMessage().contains("超出限制"));
    }

    @Test
    @DisplayName("可执行文件扩展名应被拒绝")
    void validate_exe_shouldFail() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "malware.exe", "application/octet-stream", "x".getBytes());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(file, FileCategory.DOCUMENT));
        assertTrue(ex.getMessage().contains("不允许的文件类型"));
    }

    @Test
    @DisplayName("合法 PDF 文档应通过校验")
    void validate_pdf_shouldPass() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "contract.pdf", "application/pdf", "pdf".getBytes());
        validator.validate(file, FileCategory.DOCUMENT);
        assertEquals("pdf", validator.resolveExtension("contract.pdf"));
    }

    @Test
    @DisplayName("图片分类上传 PDF 应失败")
    void validate_pdfAsImage_shouldFail() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.pdf", "application/pdf", "pdf".getBytes());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> validator.validate(file, FileCategory.IMAGE));
        assertTrue(ex.getMessage().contains("不允许的文件类型"));
    }
}
