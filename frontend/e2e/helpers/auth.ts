import type { Page } from '@playwright/test'

export const adminUser = {
  id: 1,
  username: 'admin',
  name: '系统管理员',
  departmentId: 1,
  department: '总部',
  roles: ['ADMIN'] as string[],
  permissions: [
    'project:list',
    'project:add',
    'project:edit',
    'project:delete',
    'dashboard:view',
    'log:list',
    'log:view',
    'attachment:list',
    'attachment:upload',
    'attachment:download',
    'attachment:delete',
    'device:list'
  ],
  dataScope: 'ALL',
  menus: [
    {
      id: 1,
      parentId: 0,
      title: '首页统计',
      path: '/dashboard',
      permissionCode: 'dashboard:view',
      icon: 'DataAnalysis',
      children: [] as []
    },
    {
      id: 4,
      parentId: 0,
      title: '项目管理',
      path: '/projects',
      permissionCode: 'project:list',
      icon: 'FolderOpened',
      children: [] as []
    },
    {
      id: 5,
      parentId: 0,
      title: '附件管理',
      path: '/attachments',
      permissionCode: 'attachment:list',
      icon: 'Paperclip',
      children: [] as []
    },
    {
      id: 8,
      parentId: 0,
      title: '日志审计',
      path: '/audit-logs',
      permissionCode: 'log:list',
      icon: 'DocumentChecked',
      children: [] as []
    }
  ]
}

export type MockUser = typeof adminUser

export function ok<T>(data: T) {
  return {
    success: true,
    code: 200,
    message: '操作成功',
    data
  }
}

export async function mockAuthApis(page: Page, user: MockUser = adminUser) {
  await page.route('**/api/login', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok({ token: 'e2e-mock-token', user }))
    })
  })

  await page.route('**/api/user/info', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok(user))
    })
  })

  await page.route('**/api/logout', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok(null))
    })
  })

  await page.route('**/api/refresh', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok({ token: 'e2e-mock-token', user }))
    })
  })
}

/** 注入 Token 并走 mock 用户信息，避免依赖真实登录接口 */
export async function loginAsAdmin(
  page: Page,
  options: { path?: string; testId?: string; user?: MockUser } = {}
) {
  const path = options.path ?? '/projects'
  const testId = options.testId ?? 'project-page'
  const user = options.user ?? adminUser
  await mockAuthApis(page, user)
  await page.addInitScript(() => {
    localStorage.setItem('equipment_management_token', 'e2e-mock-token')
  })
  await page.goto(path)
  await page.waitForSelector(`[data-testid="${testId}"]`, { timeout: 15_000 })
}
