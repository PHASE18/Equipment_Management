import { expect, test } from '@playwright/test'
import { loginAsAdmin, ok } from './helpers/auth'

async function mockAuditApis(page: Page) {
  await page.route(/\/api\/log\/login(\?.*)?$/, async route => {
    const url = new URL(route.request().url())
    const username = url.searchParams.get('username') || ''
    const records = [
      {
        id: 1,
        username: 'admin',
        loginIp: '127.0.0.1',
        browser: 'Chrome',
        loginTime: '2026-07-16 11:00:00',
        result: 1
      },
      {
        id: 2,
        username: 'user01',
        loginIp: '10.0.0.2',
        browser: 'Edge',
        loginTime: '2026-07-16 10:00:00',
        result: 0
      }
    ].filter(item => !username || item.username.includes(username))

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(
        ok({
          records,
          total: records.length,
          pageNum: 1,
          pageSize: 20,
          pages: 1
        })
      )
    })
  })

  await page.route(/\/api\/log\/operation(\?.*)?$/, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(
        ok({
          records: [
            {
              id: 11,
              operatorId: 1,
              operationType: 'INSERT',
              tableName: 'project',
              businessId: 100,
              beforeJson: null,
              afterJson: '{"projectCode":"PRJ-1"}',
              ip: '127.0.0.1',
              browser: 'Chrome',
              createTime: '2026-07-16 12:00:00'
            }
          ],
          total: 1,
          pageNum: 1,
          pageSize: 20,
          pages: 1
        })
      )
    })
  })

  await page.route(/\/api\/log\/status(\?.*)?$/, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(
        ok({
          records: [
            {
              id: 21,
              deviceId: 1,
              oldStatusCode: 'IN_USE',
              newStatusCode: 'MAINTAINING',
              changeReason: '送修',
              remark: 'E2E',
              operatorId: 1,
              changeTime: '2026-07-16 13:00:00'
            }
          ],
          total: 1,
          pageNum: 1,
          pageSize: 20,
          pages: 1
        })
      )
    })
  })
}

test.describe('Audit log module E2E', () => {
  test('login log list and filter', async ({ page }) => {
    await mockAuditApis(page)
    await loginAsAdmin(page, { path: '/audit-logs', testId: 'audit-log-page' })

    await expect(page.getByTestId('audit-log-page')).toBeVisible()
    await expect(page.getByTestId('audit-login-table')).toContainText('admin')
    await expect(page.getByTestId('audit-login-table')).toContainText('user01')

    await page.getByTestId('audit-username-input').fill('admin')
    await page.getByTestId('audit-search-btn').click()
    await expect(page.getByTestId('audit-login-table')).toContainText('admin')
    await expect(page.getByTestId('audit-login-table')).not.toContainText('user01')
  })

  test('switch tabs for operation and status logs', async ({ page }) => {
    await mockAuditApis(page)
    await loginAsAdmin(page, { path: '/audit-logs', testId: 'audit-log-page' })

    await page.getByRole('tab', { name: '操作日志' }).click()
    await expect(page.getByTestId('audit-operation-table')).toBeVisible()
    await expect(page.getByTestId('audit-operation-table')).toContainText('INSERT')
    await expect(page.getByTestId('audit-operation-table')).toContainText('project')

    await page.getByRole('tab', { name: '生命周期日志' }).click()
    await expect(page.getByTestId('audit-status-table')).toBeVisible()
    await expect(page.getByTestId('audit-status-table')).toContainText('MAINTAINING')
    await expect(page.getByTestId('audit-status-table')).toContainText('送修')
  })

  test('page has no delete action', async ({ page }) => {
    await mockAuditApis(page)
    await loginAsAdmin(page, { path: '/audit-logs', testId: 'audit-log-page' })

    await expect(page.getByTestId('audit-log-page')).toContainText('禁止删除')
    await expect(page.getByRole('button', { name: '删除' })).toHaveCount(0)
  })
})
