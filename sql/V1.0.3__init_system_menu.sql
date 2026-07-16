-- ============================================================================
-- 系统管理子菜单与 CRUD 按钮权限
-- ============================================================================

USE `equipment_manager`;

-- 系统管理子菜单（挂在 system:manage 下，id=7）
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `icon`, `sort`, `status`) VALUES
(711, 7, '用户管理', 'system:user', 1, '/system/users', 'User', 1, 1),
(712, 7, '角色管理', 'system:role', 1, '/system/roles', 'Avatar', 2, 1),
(713, 7, '部门管理', 'system:dept', 1, '/system/departments', 'OfficeBuilding', 3, 1),
(714, 7, '设备品牌', 'system:brand', 1, '/system/brands', 'PriceTag', 4, 1),
(715, 7, '设备类型', 'system:device-type', 1, '/system/device-types', 'Collection', 5, 1)
ON DUPLICATE KEY UPDATE
    `permission_name` = VALUES(`permission_name`),
    `path` = VALUES(`path`),
    `icon` = VALUES(`icon`),
    `sort` = VALUES(`sort`);

-- 将原按钮权限编码调整为 CRUD 后缀约定
UPDATE `sys_permission` SET `permission_code` = 'system:user:list' WHERE `id` = 401;
UPDATE `sys_permission` SET `permission_code` = 'system:role:list' WHERE `id` = 402;
UPDATE `sys_permission` SET `permission_code` = 'system:dept:list' WHERE `id` = 403;

-- 用户 / 角色 / 部门 增删改按钮
INSERT INTO `sys_permission` (`id`, `parent_id`, `permission_name`, `permission_code`, `permission_type`, `path`, `icon`, `sort`, `status`) VALUES
(411, 7, '新增用户', 'system:user:add', 2, NULL, NULL, 11, 1),
(412, 7, '编辑用户', 'system:user:edit', 2, NULL, NULL, 12, 1),
(413, 7, '删除用户', 'system:user:delete', 2, NULL, NULL, 13, 1),
(421, 7, '新增角色', 'system:role:add', 2, NULL, NULL, 21, 1),
(422, 7, '编辑角色', 'system:role:edit', 2, NULL, NULL, 22, 1),
(423, 7, '删除角色', 'system:role:delete', 2, NULL, NULL, 23, 1),
(431, 7, '新增部门', 'system:dept:add', 2, NULL, NULL, 31, 1),
(432, 7, '编辑部门', 'system:dept:edit', 2, NULL, NULL, 32, 1),
(433, 7, '删除部门', 'system:dept:delete', 2, NULL, NULL, 33, 1),
(441, 7, '品牌列表', 'system:brand:list', 2, NULL, NULL, 41, 1),
(442, 7, '新增品牌', 'system:brand:add', 2, NULL, NULL, 42, 1),
(443, 7, '编辑品牌', 'system:brand:edit', 2, NULL, NULL, 43, 1),
(444, 7, '删除品牌', 'system:brand:delete', 2, NULL, NULL, 44, 1),
(451, 7, '类型列表', 'system:device-type:list', 2, NULL, NULL, 51, 1),
(452, 7, '新增类型', 'system:device-type:add', 2, NULL, NULL, 52, 1),
(453, 7, '编辑类型', 'system:device-type:edit', 2, NULL, NULL, 53, 1),
(454, 7, '删除类型', 'system:device-type:delete', 2, NULL, NULL, 54, 1)
ON DUPLICATE KEY UPDATE `permission_name` = VALUES(`permission_name`);

-- 管理员角色拥有全部权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `sys_permission`
WHERE `id` IN (711, 712, 713, 714, 715, 411, 412, 413, 421, 422, 423, 431, 432, 433, 441, 442, 443, 444, 451, 452, 453, 454)
ON DUPLICATE KEY UPDATE `permission_id` = VALUES(`permission_id`);

-- 初始化设备品牌、设备类型示例数据
INSERT INTO `sys_dict` (`dict_type`, `dict_code`, `dict_name`, `sort`, `status`) VALUES
('device_brand', 'DELL', '戴尔', 1, 1),
('device_brand', 'HUAWEI', '华为', 2, 1),
('device_brand', 'LENOVO', '联想', 3, 1),
('device_type', 'SERVER', '服务器', 1, 1),
('device_type', 'NETWORK', '网络设备', 2, 1),
('device_type', 'STORAGE', '存储设备', 3, 1)
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);
