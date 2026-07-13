-- ============================================================================
-- 设备管理系统 - 初始化数据（管理员账号、角色、菜单权限）
-- ============================================================================

USE `equipment_manager`;

-- 默认部门
INSERT INTO `sys_department` (`id`, `department_name`, `parent_id`, `leader`, `remark`)
VALUES (1, '信息科技处', 0, '系统管理员', '默认部门')
ON DUPLICATE KEY UPDATE `department_name` = VALUES(`department_name`);

-- 管理员角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `remark`)
VALUES (1, '系统管理员', 'ADMIN', '拥有全部菜单权限')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

-- 管理员账号（用户名: admin，密码: 123456）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `department_id`, `status`)
VALUES (1, 'admin', '$2b$10$7LNK6iMycXEntxc7mJa7HORC/tltzTGvHjvwy3JVoyQQQ9iuwSyoW', '系统管理员', 1, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`);

INSERT INTO `sys_user_role` (`user_id`, `role_id`)
VALUES (1, 1)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- 菜单权限
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `icon`, `sort`, `status`) VALUES
(1, 0, '首页统计', 'dashboard:view', 1, '/dashboard', 'DataAnalysis', 1, 1),
(2, 0, '设备档案', 'device:list', 1, '/devices', 'Monitor', 2, 1),
(3, 0, '维修管理', 'maintenance:list', 1, '/maintenance', 'Tools', 3, 1),
(4, 0, '项目管理', 'project:list', 1, '/projects', 'FolderOpened', 4, 1),
(5, 0, '附件管理', 'attachment:list', 1, '/attachments', 'Paperclip', 5, 1),
(6, 0, '统计分析', 'statistics:view', 1, '/statistics', 'PieChart', 6, 1),
(7, 0, '系统管理', 'system:manage', 1, '/system', 'Setting', 7, 1),
(8, 0, '日志审计', 'log:list', 1, '/audit-logs', 'DocumentChecked', 8, 1)
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `sys_permission`
ON DUPLICATE KEY UPDATE `permission_id` = VALUES(`permission_id`);

-- 登录安全配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `remark`)
VALUES
('auth.login.max-failures', '5', '登录失败最大次数'),
('auth.login.lock-duration-minutes', '30', '登录锁定时长（分钟）')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);
