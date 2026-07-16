package com.equipment.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 附件存储配置：开发可用 local，生产必须 minio。
 */
@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

    /**
     * local | minio
     */
    private String storage = "local";

    /**
     * 本地存储根目录（仅 storage=local 时生效）
     */
    private String localBaseDir = "uploads";
}
