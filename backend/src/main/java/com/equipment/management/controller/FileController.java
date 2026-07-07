package com.equipment.management.controller;

import com.equipment.management.annotation.RequireAuth;
import com.equipment.management.common.result.Result;
import com.equipment.management.dto.response.FileUploadResponse;
import com.equipment.management.entity.DeviceAttachment;
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

    @PostMapping("/upload")
    public Result<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("deviceId") Long deviceId,
            @RequestParam(value = "fileTypeCode", required = false) String fileTypeCode) {
        return Result.success(fileService.upload(file, deviceId, fileTypeCode));
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) {
        fileService.download(id, response);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success();
    }

    @GetMapping("/list/{deviceId}")
    public Result<List<DeviceAttachment>> list(@PathVariable Long deviceId) {
        return Result.success(fileService.listByDeviceId(deviceId));
    }
}
