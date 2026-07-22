-- 设备档案字段增强：基本信息 / 配置信息 / 网络信息
USE `equipment_manager`;

-- device：新增资产与使用信息；注释对齐业务命名（不改列名）
ALTER TABLE `device`
    ADD COLUMN `is_fixed_asset`    TINYINT        NOT NULL DEFAULT 0    COMMENT '是否固定资产：0-否 1-是' AFTER `asset_no`,
    ADD COLUMN `use_department_id` BIGINT                  DEFAULT NULL COMMENT '使用部门ID' AFTER `department_id`,
    ADD COLUMN `use_user_name`     VARCHAR(100)            DEFAULT NULL COMMENT '使用人' AFTER `manager_user_id`,
    ADD COLUMN `original_value`    DECIMAL(14, 2)          DEFAULT NULL COMMENT '设备原值' AFTER `use_user_name`,
    ADD COLUMN `approval_no`       VARCHAR(100)            DEFAULT NULL COMMENT '批准文号' AFTER `original_value`,
    ADD COLUMN `manufacture_date`  DATE                    DEFAULT NULL COMMENT '出厂日期' AFTER `purchase_date`,
    ADD COLUMN `online_date`       DATE                    DEFAULT NULL COMMENT '上架日期' AFTER `manufacture_date`,
    ADD COLUMN `scrap_date`        DATE                    DEFAULT NULL COMMENT '报废日期' AFTER `warranty_end`,
    ADD KEY `idx_use_department_id` (`use_department_id`);

ALTER TABLE `device`
    MODIFY COLUMN `department_id`   BIGINT       DEFAULT NULL COMMENT '管理部门ID',
    MODIFY COLUMN `manager_user_id` BIGINT       DEFAULT NULL COMMENT '责任人用户ID',
    MODIFY COLUMN `warranty_end`    DATE         DEFAULT NULL COMMENT '到保日期',
    MODIFY COLUMN `cabinet`         VARCHAR(100) DEFAULT NULL COMMENT '机柜U位',
    MODIFY COLUMN `location`        VARCHAR(200) DEFAULT NULL COMMENT '所在机房';

-- device_config：硬件与软件配置扩展
ALTER TABLE `device_config`
    ADD COLUMN `gpu`          VARCHAR(200) DEFAULT NULL COMMENT 'GPU' AFTER `raid`,
    ADD COLUMN `fiber_card`   VARCHAR(200) DEFAULT NULL COMMENT '光纤卡' AFTER `gpu`,
    ADD COLUMN `nic`          VARCHAR(200) DEFAULT NULL COMMENT '网卡' AFTER `fiber_card`,
    ADD COLUMN `power_supply` VARCHAR(200) DEFAULT NULL COMMENT '电源' AFTER `nic`,
    ADD COLUMN `db_version`   VARCHAR(200) DEFAULT NULL COMMENT '数据库版本' AFTER `os`;

-- device_ip：网络业务信息
ALTER TABLE `device_ip`
    ADD COLUMN `mounted_business`  VARCHAR(200) DEFAULT NULL COMMENT '挂载业务' AFTER `gateway`,
    ADD COLUMN `network_zone`      VARCHAR(100) DEFAULT NULL COMMENT '所属网络' AFTER `mounted_business`,
    ADD COLUMN `mgmt_login_method` VARCHAR(100) DEFAULT NULL COMMENT '管理地址登录方式' AFTER `network_zone`;
