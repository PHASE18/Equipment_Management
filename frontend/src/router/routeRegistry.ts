import type { Component } from 'vue'

/** 权限编码到前端页面组件的安全映射项。 */
export interface RouteRegistryItem {
  name: string
  path: string
  title: string
  component: Component
}

export const routeRegistry: Record<string, RouteRegistryItem> = {
  'login:view': {
    name: 'Login',
    path: '/login',
    title: '登录',
    component: () => import('@/views/login/LoginView.vue')
  },
  'dashboard:view': {
    name: 'Dashboard',
    path: '/dashboard',
    title: '首页统计',
    component: () => import('@/views/dashboard/DashboardView.vue')
  },
  'statistics:view': {
    name: 'Statistics',
    path: '/statistics',
    title: '统计分析',
    component: () => import('@/views/dashboard/DashboardView.vue')
  },
  'system:user': {
    name: 'SystemUser',
    path: '/system/users',
    title: '用户管理',
    component: () => import('@/views/system/UserListView.vue')
  },
  'system:role': {
    name: 'SystemRole',
    path: '/system/roles',
    title: '角色管理',
    component: () => import('@/views/system/RoleListView.vue')
  },
  'system:dept': {
    name: 'SystemDepartment',
    path: '/system/departments',
    title: '部门管理',
    component: () => import('@/views/system/DepartmentListView.vue')
  },
  'system:brand': {
    name: 'SystemBrand',
    path: '/system/brands',
    title: '设备品牌',
    component: () => import('@/views/system/BrandListView.vue')
  },
  'system:device-type': {
    name: 'SystemDeviceType',
    path: '/system/device-types',
    title: '设备类型',
    component: () => import('@/views/system/DeviceTypeListView.vue')
  },
  'device:list': {
    name: 'DeviceList',
    path: '/devices',
    title: '设备档案',
    component: () => import('@/views/device/DeviceListView.vue')
  },
  'maintenance:list': {
    name: 'MaintenanceList',
    path: '/maintenance',
    title: '维修管理',
    component: () => import('@/views/maintenance/MaintenanceListView.vue')
  },
  'project:list': {
    name: 'ProjectList',
    path: '/projects',
    title: '项目管理',
    component: () => import('@/views/project/ProjectListView.vue')
  },
  'log:list': {
    name: 'AuditLogList',
    path: '/audit-logs',
    title: '日志审计',
    component: () => import('@/views/audit/AuditLogListView.vue')
  },
  'attachment:list': {
    name: 'AttachmentList',
    path: '/attachments',
    title: '附件管理',
    component: () => import('@/views/attachment/AttachmentListView.vue')
  }
}
