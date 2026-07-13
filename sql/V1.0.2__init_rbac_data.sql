-- ============================================================================
-- RBAC 权限初始化：三角色 + 按钮权限 + 测试账号
-- ============================================================================

USE `equipment_manager`;

-- 角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `remark`) VALUES
(2, '部门管理员', 'DEPT_ADMIN', '管理本部门设备和用户'),
(3, '普通用户', 'DEVICE_OWNER', '仅管理本人负责设备')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

-- 按钮/接口权限（permission_type: 1-菜单 2-按钮 3-接口）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `icon`, `sort`, `status`) VALUES
(101, 2, '查看设备', 'device:view', 2, NULL, NULL, 1, 1),
(102, 2, '新增设备', 'device:add', 2, NULL, NULL, 2, 1),
(103, 2, '修改设备', 'device:edit', 2, NULL, NULL, 3, 1),
(104, 2, '删除设备', 'device:delete', 2, NULL, NULL, 4, 1),
(105, 2, '导入设备', 'device:import', 2, NULL, NULL, 5, 1),
(106, 2, '导出设备', 'device:export', 2, NULL, NULL, 6, 1),
(201, 3, '查看维修', 'maintenance:view', 2, NULL, NULL, 1, 1),
(202, 3, '新增维修', 'maintenance:add', 2, NULL, NULL, 2, 1),
(203, 3, '修改维修', 'maintenance:edit', 2, NULL, NULL, 3, 1),
(204, 3, '删除维修', 'maintenance:delete', 2, NULL, NULL, 4, 1),
(301, 5, '上传附件', 'attachment:upload', 2, NULL, NULL, 1, 1),
(302, 5, '下载附件', 'attachment:download', 2, NULL, NULL, 2, 1),
(303, 5, '删除附件', 'attachment:delete', 2, NULL, NULL, 3, 1),
(401, 7, '用户管理', 'system:user', 2, NULL, NULL, 1, 1),
(402, 7, '角色管理', 'system:role', 2, NULL, NULL, 2, 1),
(403, 7, '部门管理', 'system:dept', 2, NULL, NULL, 3, 1),
(404, 7, '权限管理', 'system:permission', 2, NULL, NULL, 4, 1),
(501, 8, '查看日志', 'log:view', 2, NULL, NULL, 1, 1)
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 部门管理员：业务菜单 + 按钮（不含系统管理）
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, `id` FROM `sys_permission`
WHERE `permission_code` IN (
    'dashboard:view', 'device:list', 'device:view', 'device:add', 'device:edit', 'device:delete',
    'device:import', 'device:export', 'maintenance:list', 'maintenance:view', 'maintenance:add',
    'maintenance:edit', 'maintenance:delete', 'project:list', 'attachment:list', 'attachment:upload',
    'attachment:download', 'attachment:delete', 'statistics:view', 'log:list', 'log:view'
)
ON DUPLICATE KEY UPDATE `permission_id` = VALUES(`permission_id`);

-- 普通用户：本人设备相关
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, `id` FROM `sys_permission`
WHERE `permission_code` IN (
    'dashboard:view', 'device:list', 'device:view', 'device:edit', 'device:export',
    'maintenance:list', 'maintenance:view', 'maintenance:add', 'maintenance:edit',
    'attachment:list', 'attachment:upload', 'attachment:download', 'statistics:view'
)
ON DUPLICATE KEY UPDATE `permission_id` = VALUES(`permission_id`);

-- 测试部门
INSERT INTO `sys_department` (`id`, `department_name`, `parent_id`, `leader`, `remark`)
VALUES (2, '运维保障部', 0, '部门管理员', '测试部门数据隔离')
ON DUPLICATE KEY UPDATE `department_name` = VALUES(`department_name`);

-- 测试账号（密码均为 123456）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `department_id`, `status`) VALUES
(2, 'dept_admin', '$2b$10$7LNK6iMycXEntxc7mJa7HORC/tltzTGvHjvwy3JVoyQQQ9iuwSyoW', '部门管理员', 2, 1),
(3, 'user01', '$2b$10$7LNK6iMycXEntxc7mJa7HORC/tltzTGvHjvwy3JVoyQQQ9iuwSyoW', '设备责任人', 2, 1)
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(2, 2),
(3, 3)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);
