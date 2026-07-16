-- 维修附件关联 + 故障类型字典
USE `equipment_manager`;

ALTER TABLE `device_attachment`
    ADD COLUMN `maintenance_id` BIGINT DEFAULT NULL COMMENT '关联维修工单ID' AFTER `device_id`,
    ADD KEY `idx_maintenance_id` (`maintenance_id`);

INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_name`, `sort`, `status`) VALUES
('fault_type', 'HARDWARE', '硬件故障', 1, 1),
('fault_type', 'SOFTWARE', '软件故障', 2, 1),
('fault_type', 'NETWORK', '网络故障', 3, 1),
('fault_type', 'POWER', '电源故障', 4, 1),
('fault_type', 'OTHER', '其他故障', 5, 1)
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);
