package com.equipment.management.common.util;

import com.equipment.management.common.constant.ErrorCode;
import com.equipment.management.common.exception.BusinessException;
import com.equipment.management.config.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MinioUtils {

    private final MinioProperties properties;
    private MinioClient minioClient;

    @jakarta.annotation.PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    public String upload(MultipartFile file, String directory) {
        String objectName = buildObjectName(file.getOriginalFilename(), directory);
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
        String endpoint = properties.getEndpoint();
        String normalizedEndpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return normalizedEndpoint + "/" + properties.getBucket() + "/" + objectName;
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
