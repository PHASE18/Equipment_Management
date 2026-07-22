-- 移除与首页统计重复的「统计分析」菜单权限
USE `equipment_manager`;

DELETE FROM `sys_role_permission`
WHERE `permission_id` IN (
    SELECT `id` FROM (
        SELECT `id` FROM `sys_permission` WHERE `permission_code` = 'statistics:view'
    ) t
);

DELETE FROM `sys_permission` WHERE `permission_code` = 'statistics:view';
