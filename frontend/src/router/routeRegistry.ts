import type { Component } from 'vue'

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
  // 'dashboard:view': {
  //   name: 'Dashboard',
  //   path: '/dashboard',
  //   title: '首页统计',
  //   component: () => import('@/views/dashboard/DashboardView.vue')
  // },
  // 'device:list': {
  //   name: 'DeviceList',
  //   path: '/devices',
  //   title: '设备档案',
  //   component: () => import('@/views/device/DeviceListView.vue')
  // },
  // 'maintenance:list': {
  //   name: 'MaintenanceList',
  //   path: '/maintenance',
  //   title: '维修管理',
  //   component: () => import('@/views/placeholder/ModulePlaceholder.vue')
  // },
  // 'project:list': {
  //   name: 'ProjectList',
  //   path: '/projects',
  //   title: '项目管理',
  //   component: () => import('@/views/placeholder/ModulePlaceholder.vue')
  // },
  // 'attachment:list': {
  //   name: 'AttachmentList',
  //   path: '/attachments',
  //   title: '附件管理',
  //   component: () => import('@/views/placeholder/ModulePlaceholder.vue')
  // },
  // 'statistics:view': {
  //   name: 'Statistics',
  //   path: '/statistics',
  //   title: '统计分析',
  //   component: () => import('@/views/placeholder/ModulePlaceholder.vue')
  // },
  // 'system:manage': {
  //   name: 'SystemManage',
  //   path: '/system',
  //   title: '系统管理',
  //   component: () => import('@/views/placeholder/ModulePlaceholder.vue')
  // },
  // 'log:list': {
  //   name: 'AuditLogs',
  //   path: '/audit-logs',
  //   title: '日志审计',
  //   component: () => import('@/views/placeholder/ModulePlaceholder.vue')
  // }
}
