package com.equipment.management.common.util;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.config.FileStorageProperties;
import com.equipment.management.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
/** MinIO 对象存储适配器，封装上传、下载和删除等存储操作。 */
public class MinioUtils {

    private final MinioProperties properties;
    private final FileStorageProperties storageProperties;
    private MinioClient minioClient;
    private Path localRoot;

    @PostConstruct
    public void init() {
        if (isLocalMode()) {
            localRoot = Paths.get(storageProperties.getLocalBaseDir()).toAbsolutePath().normalize();
            try {
                Files.createDirectories(localRoot);
                log.info("附件存储使用本地目录: {}", localRoot);
            } catch (IOException e) {
                throw new IllegalStateException("无法创建本地附件目录: " + localRoot, e);
            }
            return;
        }
        minioClient = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
        log.info("附件存储使用 MinIO: {}", properties.getEndpoint());
    }

    public String upload(MultipartFile file, String directory) {
        String objectName = buildObjectName(file.getOriginalFilename(), directory);
        if (isLocalMode()) {
            return uploadLocal(file, objectName);
        }
        try (InputStream inputStream = file.getInputStream()) {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return objectName;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to upload file");
        }
    }

    public InputStream download(String objectName) {
        if (isLocalMode()) {
            try {
                Path file = resolveLocalPath(objectName);
                if (!Files.exists(file)) {
                    throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
                }
                return Files.newInputStream(file);
            } catch (BusinessException ex) {
                throw ex;
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
            }
        }
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }
    }

    public void delete(String objectName) {
        if (isLocalMode()) {
            try {
                Files.deleteIfExists(resolveLocalPath(objectName));
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to delete file");
            }
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to delete file");
        }
    }

    public StatObjectResponse stat(String objectName) {
        if (isLocalMode()) {
            throw new UnsupportedOperationException("local storage does not support MinIO stat");
        }
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.ATTACHMENT_NOT_FOUND);
        }
    }

    public String getObjectUrl(String objectName) {
        if (isLocalMode()) {
            return "/api/file/local/" + objectName.replace("\\", "/");
        }
        String endpoint = properties.getEndpoint();
        String normalizedEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return normalizedEndpoint + "/" + properties.getBucket() + "/" + objectName;
    }

    private String uploadLocal(MultipartFile file, String objectName) {
        try {
            Path target = resolveLocalPath(objectName);
            Files.createDirectories(target.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return objectName.replace("\\", "/");
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to upload file");
        }
    }

    private Path resolveLocalPath(String objectName) {
        Path resolved = localRoot.resolve(objectName).normalize();
        if (!resolved.startsWith(localRoot)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法文件路径");
        }
        return resolved;
    }

    private boolean isLocalMode() {
        return !"minio".equalsIgnoreCase(storageProperties.getStorage());
    }

    private void ensureBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucket())
                        .build());
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to initialize MinIO bucket");
        }
    }

    private String buildObjectName(String originalFilename, String directory) {
        String filename = StringUtils.hasText(originalFilename) ? originalFilename : "file";
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.')) : "";
        String baseDir = StringUtils.hasText(directory) ? trimSlash(directory) : "attachments";
        LocalDate now = LocalDate.now();
        return baseDir + "/" + now.getYear() + "/" + now.getMonthValue() + "/" + UUID.randomUUID() + ext;
    }

    private String trimSlash(String value) {
        String result = value;
        while (result.startsWith("/")) {
            result = result.substring(1);
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }
}
