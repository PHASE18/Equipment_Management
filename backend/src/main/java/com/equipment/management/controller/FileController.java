package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.annotation.RequirePermission;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.response.FileUploadResponse;
import com.equipment.management.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequireAuth
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 统一文件上传：支持 image / document / excel / contract
     */
    @RequirePermission("attachment:upload")
    @PostMapping("/upload")
    public Result<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("deviceId") Long deviceId,
            @RequestParam(value = "category", defaultValue = "document") String category,
            @RequestParam(value = "fileTypeCode", required = false) String fileTypeCode) {
        return Result.success(fileService.upload(file, deviceId, category, fileTypeCode));
    }

    @RequirePermission("attachment:download")
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        fileService.download(id, response);
    }

    @RequirePermission("attachment:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success();
    }

    @RequirePermission("attachment:download")
    @GetMapping("/list/{deviceId}")
    public Result<List<FileUploadResponse>> list(@PathVariable Long deviceId) {
        return Result.success(fileService.listByDeviceId(deviceId));
    }
}
