-- 责任人改为自由文本
USE `equipment_manager`;

ALTER TABLE `device`
    ADD COLUMN `manager_name` VARCHAR(100) DEFAULT NULL COMMENT '责任人' AFTER `manager_user_id`;

-- 尽量用已有用户真实姓名回填历史责任人
UPDATE `device` d
    INNER JOIN `sys_user` u ON u.id = d.manager_user_id AND u.deleted = 0
SET d.manager_name = COALESCE(NULLIF(u.real_name, ''), u.username)
WHERE d.manager_name IS NULL AND d.manager_user_id IS NOT NULL;
