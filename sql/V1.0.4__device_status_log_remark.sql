-- 生命周期日志增加备注字段
USE `equipment_manager`;

ALTER TABLE `device_status_log`
    ADD COLUMN `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注' AFTER `change_reason`;
