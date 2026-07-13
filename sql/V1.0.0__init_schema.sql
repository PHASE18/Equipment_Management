-- ============================================================================
-- 设备管理系统 - 数据库初始化脚本
-- Database: equipment_manager
-- MySQL 8.0+ | InnoDB | utf8mb4
-- 符合第三范式（3NF），共 18 张业务表
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `equipment_manager`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `equipment_manager`;

-- ============================================================================
-- 一、系统管理模块
-- ============================================================================

-- 1. 部门表
CREATE TABLE IF NOT EXISTS `sys_department` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `department_name` VARCHAR(500) NOT NULL                COMMENT '部门名称',
    `parent_id`       BIGINT       NOT NULL DEFAULT 0     COMMENT '父部门ID，0表示顶级',
    `leader`          VARCHAR(50)           DEFAULT NULL  COMMENT '负责人',
    `phone`           VARCHAR(30)           DEFAULT NULL  COMMENT '联系电话',
    `remark`          VARCHAR(200)          DEFAULT NULL  COMMENT '备注',
    `create_by`       BIGINT                DEFAULT NULL  COMMENT '创建人ID',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       BIGINT                DEFAULT NULL  COMMENT '更新人ID',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 2. 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`      VARCHAR(100)  NOT NULL                COMMENT '登录用户名',
    `password`      VARCHAR(255) NOT NULL                COMMENT '密码（BCrypt加密）',
    `real_name`     VARCHAR(300)           DEFAULT NULL  COMMENT '真实姓名',
    `department_id` BIGINT                DEFAULT NULL  COMMENT '所属部门ID',
    `phone`         VARCHAR(30)           DEFAULT NULL  COMMENT '手机号',
    `email`         VARCHAR(100)          DEFAULT NULL  COMMENT '邮箱',
    `status`        TINYINT      NOT NULL DEFAULT 1     COMMENT '状态：0-禁用 1-启用',
    `create_by`     BIGINT                DEFAULT NULL  COMMENT '创建人ID',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     BIGINT                DEFAULT NULL  COMMENT '更新人ID',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 3. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name`   VARCHAR(300)  NOT NULL                COMMENT '角色名称',
    `role_code`   VARCHAR(50)  NOT NULL                COMMENT '角色编码',
    `remark`      VARCHAR(200)          DEFAULT NULL  COMMENT '备注',
    `create_by`   BIGINT                DEFAULT NULL  COMMENT '创建人ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   BIGINT                DEFAULT NULL  COMMENT '更新人ID',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 4. 权限表（菜单/按钮/接口）
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id`       BIGINT       NOT NULL DEFAULT 0     COMMENT '父权限ID，0表示顶级',
    `permission_name` VARCHAR(100) NOT NULL                COMMENT '权限名称',
    `permission_code` VARCHAR(100) NOT NULL                COMMENT '权限编码',
    `permission_type` TINYINT      NOT NULL                COMMENT '权限类型：1-菜单 2-按钮 3-接口',
    `path`            VARCHAR(200)          DEFAULT NULL  COMMENT '路由/接口路径',
    `icon`            VARCHAR(100)          DEFAULT NULL  COMMENT '菜单图标',
    `sort`            INT          NOT NULL DEFAULT 0     COMMENT '排序号',
    `status`          TINYINT      NOT NULL DEFAULT 1     COMMENT '状态：0-禁用 1-启用',
    `create_by`       BIGINT                DEFAULT NULL  COMMENT '创建人ID',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       BIGINT                DEFAULT NULL  COMMENT '更新人ID',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_permission_type` (`permission_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 5. 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT   NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 6. 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `role_id`       BIGINT   NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT   NOT NULL COMMENT '权限ID',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 17. 字典表（品牌、设备类型、故障类型、附件类型、设备状态等）
CREATE TABLE IF NOT EXISTS `sys_dict` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_type`   VARCHAR(50)  NOT NULL                COMMENT '字典类型',
    `dict_code`   VARCHAR(50)  NOT NULL                COMMENT '字典编码',
    `dict_name`   VARCHAR(100) NOT NULL                COMMENT '字典名称',
    `sort`        INT          NOT NULL DEFAULT 0     COMMENT '排序号',
    `status`      TINYINT      NOT NULL DEFAULT 1     COMMENT '状态：0-禁用 1-启用',
    `create_by`   BIGINT                DEFAULT NULL  COMMENT '创建人ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   BIGINT                DEFAULT NULL  COMMENT '更新人ID',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_dict_type_code` (`dict_type`, `dict_code`),
    KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典表';

-- 18. 系统配置表
CREATE TABLE IF NOT EXISTS `sys_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key`   VARCHAR(100) NOT NULL                COMMENT '配置键',
    `config_value` VARCHAR(500) NOT NULL                COMMENT '配置值',
    `remark`       VARCHAR(200)          DEFAULT NULL  COMMENT '备注',
    `create_by`    BIGINT                DEFAULT NULL  COMMENT '创建人ID',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`    BIGINT                DEFAULT NULL  COMMENT '更新人ID',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================================
-- 二、设备管理模块
-- ============================================================================

-- 7. 设备主表
CREATE TABLE IF NOT EXISTS `device` (
    `id`                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_no`            VARCHAR(50)  NOT NULL                COMMENT '设备编号',
    `device_name`          VARCHAR(100) NOT NULL                COMMENT '设备名称',
    `sn`                   VARCHAR(100)          DEFAULT NULL  COMMENT '序列号（SN）',
    `asset_no`             VARCHAR(100)          DEFAULT NULL  COMMENT '资产编号',
    `brand_code`           VARCHAR(50)           DEFAULT NULL  COMMENT '品牌编码，关联 sys_dict.dict_code（dict_type=device_brand）',
    `model`                VARCHAR(100)          DEFAULT NULL  COMMENT '型号',
    `device_type_code`     VARCHAR(50)           DEFAULT NULL  COMMENT '设备类型编码，关联 sys_dict.dict_code（dict_type=device_type）',
    `department_id`        BIGINT                DEFAULT NULL  COMMENT '所属部门ID',
    `manager_user_id`      BIGINT                DEFAULT NULL  COMMENT '设备管理员用户ID',
    `supplier`             VARCHAR(100)          DEFAULT NULL  COMMENT '供应商',
    `maintenance_company`  VARCHAR(100)          DEFAULT NULL  COMMENT '维保单位',
    `purchase_date`        DATE                  DEFAULT NULL  COMMENT '采购日期',
    `warranty_end`         DATE                  DEFAULT NULL  COMMENT '保修截止日期',
    `status_code`          VARCHAR(50)  NOT NULL                COMMENT '设备状态编码，关联 sys_dict.dict_code（dict_type=device_status）',
    `cabinet`              VARCHAR(100)          DEFAULT NULL  COMMENT '机柜位置',
    `location`             VARCHAR(200)          DEFAULT NULL  COMMENT '物理位置',
    `remark`               VARCHAR(500)          DEFAULT NULL  COMMENT '备注',
    `create_by`            BIGINT                DEFAULT NULL  COMMENT '创建人ID',
    `create_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`            BIGINT                DEFAULT NULL  COMMENT '更新人ID',
    `update_time`          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_no` (`device_no`),
    UNIQUE KEY `uk_sn` (`sn`),
    KEY `idx_status_code` (`status_code`),
    KEY `idx_department_id` (`department_id`),
    KEY `idx_manager_user_id` (`manager_user_id`),
    KEY `idx_device_type_code` (`device_type_code`),
    KEY `idx_brand_code` (`brand_code`),
    KEY `idx_purchase_date` (`purchase_date`),
    KEY `idx_warranty_end` (`warranty_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备主表';

-- 8. 设备配置表（与设备 1:1）
CREATE TABLE IF NOT EXISTS `device_config` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id`   BIGINT       NOT NULL                COMMENT '设备ID',
    `cpu`         VARCHAR(200)          DEFAULT NULL  COMMENT 'CPU',
    `memory`      VARCHAR(100)          DEFAULT NULL  COMMENT '内存',
    `disk`        VARCHAR(200)          DEFAULT NULL  COMMENT '磁盘',
    `raid`        VARCHAR(100)          DEFAULT NULL  COMMENT 'RAID配置',
    `os`          VARCHAR(100)          DEFAULT NULL  COMMENT '操作系统',
    `firmware`    VARCHAR(100)          DEFAULT NULL  COMMENT '固件版本',
    `bios`        VARCHAR(100)          DEFAULT NULL  COMMENT 'BIOS版本',
    `remark`      VARCHAR(500)          DEFAULT NULL  COMMENT '备注',
    `create_by`   BIGINT                DEFAULT NULL  COMMENT '创建人ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   BIGINT                DEFAULT NULL  COMMENT '更新人ID',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备配置表';

-- 9. 设备IP地址表（与设备 1:1）
CREATE TABLE IF NOT EXISTS `device_ip` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id`      BIGINT      NOT NULL                COMMENT '设备ID',
    `business_ip`    VARCHAR(45)          DEFAULT NULL  COMMENT '业务IP（支持IPv4/IPv6）',
    `management_ip`  VARCHAR(45)          DEFAULT NULL  COMMENT '管理IP（支持IPv4/IPv6）',
    `mask`           VARCHAR(45)          DEFAULT NULL  COMMENT '子网掩码',
    `gateway`        VARCHAR(45)          DEFAULT NULL  COMMENT '网关',
    `create_by`      BIGINT               DEFAULT NULL  COMMENT '创建人ID',
    `create_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`      BIGINT               DEFAULT NULL  COMMENT '更新人ID',
    `update_time`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT     NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_id` (`device_id`),
    UNIQUE KEY `uk_business_ip` (`business_ip`),
    UNIQUE KEY `uk_management_ip` (`management_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备IP地址表';

-- 10. 项目表
CREATE TABLE IF NOT EXISTS `project` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `project_name`  VARCHAR(100) NOT NULL                COMMENT '项目名称',
    `project_code`  VARCHAR(50)  NOT NULL                COMMENT '项目编码',
    `department_id` BIGINT                DEFAULT NULL  COMMENT '所属部门ID',
    `remark`        VARCHAR(500)          DEFAULT NULL  COMMENT '备注',
    `create_by`     BIGINT                DEFAULT NULL  COMMENT '创建人ID',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`     BIGINT                DEFAULT NULL  COMMENT '更新人ID',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_project_code` (`project_code`),
    KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- 11. 设备项目关联表
CREATE TABLE IF NOT EXISTS `device_project` (
    `device_id`   BIGINT   NOT NULL COMMENT '设备ID',
    `project_id`  BIGINT   NOT NULL COMMENT '项目ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`device_id`, `project_id`),
    KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备项目关联表';

-- ============================================================================
-- 三、维修管理模块
-- ============================================================================

-- 12. 设备维修记录表
CREATE TABLE IF NOT EXISTS `device_maintenance` (
    `id`                   BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id`            BIGINT         NOT NULL                COMMENT '设备ID',
    `maintenance_date`     DATE           NOT NULL                COMMENT '维修日期',
    `maintenance_person`   VARCHAR(50)             DEFAULT NULL  COMMENT '维修人员',
    `maintenance_company`  VARCHAR(100)            DEFAULT NULL  COMMENT '维修单位',
    `fault_type_code`      VARCHAR(50)             DEFAULT NULL  COMMENT '故障类型编码，关联 sys_dict.dict_code（dict_type=fault_type）',
    `fault_reason`         VARCHAR(200)            DEFAULT NULL  COMMENT '故障原因',
    `fault_description`    TEXT                    DEFAULT NULL  COMMENT '故障描述',
    `replace_parts`        VARCHAR(500)            DEFAULT NULL  COMMENT '更换配件',
    `maintenance_cost`     DECIMAL(12, 2)          DEFAULT NULL  COMMENT '维修费用',
    `recover_date`         DATE                    DEFAULT NULL  COMMENT '恢复日期',
    `is_resolved`          TINYINT        NOT NULL DEFAULT 0     COMMENT '是否已解决：0-否 1-是',
    `attachment_path`      VARCHAR(500)            DEFAULT NULL  COMMENT '维修附件路径',
    `remark`               VARCHAR(500)            DEFAULT NULL  COMMENT '备注',
    `create_by`            BIGINT                  DEFAULT NULL  COMMENT '创建人ID',
    `create_time`          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`            BIGINT                  DEFAULT NULL  COMMENT '更新人ID',
    `update_time`          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              TINYINT        NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_fault_type_code` (`fault_type_code`),
    KEY `idx_maintenance_date` (`maintenance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备维修记录表';

-- 13. 设备附件表
CREATE TABLE IF NOT EXISTS `device_attachment` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id`      BIGINT       NOT NULL                COMMENT '设备ID',
    `file_name`      VARCHAR(200) NOT NULL                COMMENT '文件名称',
    `file_type_code` VARCHAR(50)  NOT NULL                COMMENT '附件类型编码，关联 sys_dict.dict_code（dict_type=attachment_type）',
    `file_size`      BIGINT                DEFAULT NULL  COMMENT '文件大小（字节）',
    `file_path`      VARCHAR(500) NOT NULL                COMMENT '文件存储路径',
    `upload_user_id` BIGINT                DEFAULT NULL  COMMENT '上传人用户ID',
    `upload_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `create_by`      BIGINT                DEFAULT NULL  COMMENT '创建人ID',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`      BIGINT                DEFAULT NULL  COMMENT '更新人ID',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_file_type_code` (`file_type_code`),
    KEY `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备附件表';

-- 14. 设备生命周期/状态变更日志表
CREATE TABLE IF NOT EXISTS `device_status_log` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `device_id`     BIGINT      NOT NULL                COMMENT '设备ID',
    `old_status_code` VARCHAR(50)         DEFAULT NULL  COMMENT '原状态编码',
    `new_status_code` VARCHAR(50) NOT NULL              COMMENT '新状态编码',
    `change_reason` VARCHAR(500)          DEFAULT NULL  COMMENT '变更原因',
    `operator_id`   BIGINT                DEFAULT NULL  COMMENT '操作人用户ID',
    `change_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    PRIMARY KEY (`id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_change_time` (`change_time`),
    KEY `idx_operator_id` (`operator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备生命周期状态变更日志表';

-- ============================================================================
-- 四、日志管理模块
-- ============================================================================

-- 15. 操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `operator_id`    BIGINT                DEFAULT NULL  COMMENT '操作人用户ID',
    `operation_type` VARCHAR(50)  NOT NULL                COMMENT '操作类型（INSERT/UPDATE/DELETE等）',
    `table_name`     VARCHAR(100) NOT NULL                COMMENT '操作表名',
    `business_id`    BIGINT                DEFAULT NULL  COMMENT '业务数据ID',
    `before_json`    JSON                  DEFAULT NULL  COMMENT '变更前数据（JSON）',
    `after_json`     JSON                  DEFAULT NULL  COMMENT '变更后数据（JSON）',
    `ip`             VARCHAR(45)           DEFAULT NULL  COMMENT '操作IP',
    `browser`        VARCHAR(200)          DEFAULT NULL  COMMENT '浏览器信息',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_table_name` (`table_name`),
    KEY `idx_business_id` (`business_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';

-- 16. 登录日志表
CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`   VARCHAR(50) NOT NULL                COMMENT '登录用户名',
    `login_ip`   VARCHAR(45)          DEFAULT NULL  COMMENT '登录IP',
    `browser`    VARCHAR(200)         DEFAULT NULL  COMMENT '浏览器信息',
    `login_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `result`     TINYINT     NOT NULL DEFAULT 1     COMMENT '登录结果：0-失败 1-成功',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统登录日志表';

-- ============================================================================
-- 五、外键约束（逻辑关联，生产环境可按需禁用）
-- ============================================================================

ALTER TABLE `sys_user`
    ADD CONSTRAINT `fk_user_department` FOREIGN KEY (`department_id`) REFERENCES `sys_department` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `sys_user_role`
    ADD CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `sys_role_permission`
    ADD CONSTRAINT `fk_role_perm_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_role_perm_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `device`
    ADD CONSTRAINT `fk_device_department` FOREIGN KEY (`department_id`) REFERENCES `sys_department` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_device_manager` FOREIGN KEY (`manager_user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `device_config`
    ADD CONSTRAINT `fk_device_config_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `device_ip`
    ADD CONSTRAINT `fk_device_ip_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `project`
    ADD CONSTRAINT `fk_project_department` FOREIGN KEY (`department_id`) REFERENCES `sys_department` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `device_project`
    ADD CONSTRAINT `fk_device_project_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_device_project_project` FOREIGN KEY (`project_id`) REFERENCES `project` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `device_maintenance`
    ADD CONSTRAINT `fk_maintenance_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE `device_attachment`
    ADD CONSTRAINT `fk_attachment_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_attachment_upload_user` FOREIGN KEY (`upload_user_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `device_status_log`
    ADD CONSTRAINT `fk_status_log_device` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT `fk_status_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE `sys_operation_log`
    ADD CONSTRAINT `fk_operation_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;
