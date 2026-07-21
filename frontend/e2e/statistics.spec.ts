import { expect, test } from '@playwright/test'
import { adminUser, loginAsAdmin, ok } from './helpers/auth'

const mockDashboard = {
  summary: {
    deviceTotal: 12,
    inUseCount: 8,
    maintainingCount: 1,
    stoppedCount: 1,
    scrappedCount: 2,
    warrantyExpiringCount: 3,
    monthNewDeviceCount: 2,
    monthMaintenanceCount: 4
  },
  statusChart: [{ code: 'IN_USE', name: '在用', value: 8 }],
  brandChart: [{ code: 'HUAWEI', name: '华为', value: 5 }],
  typeChart: [{ code: 'SERVER', name: '服务器', value: 4 }],
  departmentChart: [{ code: '1', name: '总部', value: 12 }],
  projectChart: [{ code: '1', name: '智慧机房', value: 3 }],
  faultChart: [{ code: 'HARDWARE', name: '硬件故障', value: 2 }],
  maintenanceTrendChart: [{ code: '2026-07', name: '2026-07', value: 4 }],
  maintenanceCostChart: [{ code: '2026-07', name: '2026-07', value: 4, amount: 1200 }],
  warrantyChart: [{ code: '2026-08', name: '2026-08', value: 2 }],
  supplierChart: [{ code: '华为', name: '华为', value: 5 }],
  maintenanceCompanyChart: [{ code: '维保A', name: '维保A', value: 3 }],
  modelChart: [{ code: 'S5700', name: 'S5700', value: 2 }],
  scrapChart: [{ code: '2026-06', name: '2026-06', value: 1 }]
}

async function mockStatisticsApis(page: import('@playwright/test').Page) {
  const emptyPage = ok({ records: [], total: 0, pageNum: 1, pageSize: 200, pages: 0 })

  await page.route('**/api/options/departments', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok([{ id: 1, departmentName: '总部', parentId: 0 }]))
    })
  })
  await page.route('**/api/options/brands', async route => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(ok([])) })
  })
  await page.route('**/api/options/device-types', async route => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(ok([])) })
  })
  await page.route(/\/api\/project\/list.*/, async route => {
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(emptyPage) })
  })
  await page.route(/\/api\/statistics\/dashboard.*/, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok(mockDashboard))
    })
  })
}

test.describe('Dashboard statistics E2E', () => {
  test('load summary cards and charts', async ({ page }) => {
    await mockStatisticsApis(page)
    await loginAsAdmin(page, {
      path: '/dashboard',
      testId: 'statistics-page',
      user: adminUser
    })

    await expect(page.getByTestId('statistics-page')).toBeVisible()
    await expect(page.getByTestId('statistics-summary')).toContainText('设备总数')
    await expect(page.getByTestId('statistics-summary')).toContainText('12')
    await expect(page.getByTestId('statistics-chart-status')).toBeVisible()
    await expect(page.getByTestId('statistics-chart-supplier')).toBeVisible()
    await expect(page.getByTestId('statistics-chart-scrap')).toBeVisible()
  })

  test('search and reset filters trigger reload', async ({ page }) => {
    let requestCount = 0
    const emptyPage = ok({ records: [], total: 0, pageNum: 1, pageSize: 200, pages: 0 })
    await page.route('**/api/options/departments', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(ok([{ id: 1, departmentName: '总部', parentId: 0 }]))
      })
    })
    await page.route('**/api/options/brands', async route => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(ok([])) })
    })
    await page.route('**/api/options/device-types', async route => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(ok([])) })
    })
    await page.route(/\/api\/project\/list.*/, async route => {
      await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(emptyPage) })
    })
    await page.route(/\/api\/statistics\/dashboard.*/, async route => {
      requestCount += 1
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(ok(mockDashboard))
      })
    })

    await loginAsAdmin(page, {
      path: '/dashboard',
      testId: 'statistics-page',
      user: adminUser
    })

    const before = requestCount
    await page.getByTestId('statistics-search-btn').click()
    await expect.poll(() => requestCount).toBeGreaterThan(before)

    const beforeReset = requestCount
    await page.getByTestId('statistics-reset-btn').click()
    await expect.poll(() => requestCount).toBeGreaterThan(beforeReset)
  })
})
