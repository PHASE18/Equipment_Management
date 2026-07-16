import { expect, test, type Page } from '@playwright/test'
import { loginAsAdmin, ok } from './helpers/auth'

interface MockProject {
  id: number
  projectName: string
  projectCode: string
  departmentId?: number
  remark?: string
  createTime?: string
}

async function mockProjectModule(page: Page, store: { projects: MockProject[] }) {
  await page.route('**/api/department/tree', async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(
        ok([
          {
            id: 1,
            departmentName: '总部',
            parentId: 0,
            children: []
          }
        ])
      )
    })
  })

  await page.route(/\/api\/project(\/.*)?$/, async route => {
    const request = route.request()
    const method = request.method()
    const url = new URL(request.url())
    const path = url.pathname.replace(/^\/api/, '')

    if (method === 'GET' && path === '/project/list') {
      const keyword = url.searchParams.get('keyword') || ''
      const records = store.projects.filter(
        item =>
          !keyword ||
          item.projectName.includes(keyword) ||
          item.projectCode.includes(keyword)
      )
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
      return
    }

    if (method === 'POST' && path === '/project') {
      const body = request.postDataJSON() as MockProject
      const created: MockProject = {
        id: store.projects.length + 1,
        projectName: body.projectName,
        projectCode: body.projectCode,
        departmentId: body.departmentId,
        remark: body.remark,
        createTime: '2026-07-16 12:00:00'
      }
      store.projects.push(created)
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(ok(null))
      })
      return
    }

    if (method === 'PUT' && path === '/project') {
      const body = request.postDataJSON() as MockProject
      const target = store.projects.find(item => item.id === body.id)
      if (target) {
        Object.assign(target, body)
      }
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(ok(null))
      })
      return
    }

    const deleteMatch = path.match(/^\/project\/(\d+)$/)
    if (method === 'DELETE' && deleteMatch) {
      const id = Number(deleteMatch[1])
      store.projects = store.projects.filter(item => item.id !== id)
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(ok(null))
      })
      return
    }

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok(null))
    })
  })
}

test.describe('Project module E2E', () => {
  test('list and keyword search', async ({ page }) => {
    const store = {
      projects: [
        {
          id: 1,
          projectName: '智慧机房改造',
          projectCode: 'PRJ-A001',
          departmentId: 1,
          remark: '一期',
          createTime: '2026-07-16 10:00:00'
        },
        {
          id: 2,
          projectName: '网络升级',
          projectCode: 'PRJ-B002',
          departmentId: 1,
          createTime: '2026-07-16 11:00:00'
        }
      ] as MockProject[]
    }

    await mockProjectModule(page, store)
    await loginAsAdmin(page)

    await expect(page.getByTestId('project-page')).toBeVisible()
    await expect(page.getByTestId('project-table')).toContainText('智慧机房改造')
    await expect(page.getByTestId('project-table')).toContainText('网络升级')

    await page.getByTestId('project-keyword-input').fill('PRJ-A001')
    await page.getByTestId('project-search-btn').click()
    await expect(page.getByTestId('project-table')).toContainText('智慧机房改造')
    await expect(page.getByTestId('project-table')).not.toContainText('网络升级')
  })

  test('create edit delete flow', async ({ page }) => {
    const store = { projects: [] as MockProject[] }
    await mockProjectModule(page, store)
    await loginAsAdmin(page)

    await page.getByTestId('project-create-btn').click()
    await expect(page.getByTestId('project-dialog')).toBeVisible()
    await page.getByTestId('project-code-input').fill('PRJ-E2E-001')
    await page.getByTestId('project-name-input').fill('E2E测试项目')
    await page.getByTestId('project-remark-input').fill('自动化创建')
    await page.getByTestId('project-save-btn').click()

    await expect(page.getByTestId('project-table')).toContainText('PRJ-E2E-001')
    await expect(page.getByTestId('project-table')).toContainText('E2E测试项目')

    await page.getByTestId('project-edit-1').click()
    await page.getByTestId('project-name-input').fill('E2E测试项目-已更新')
    await page.getByTestId('project-save-btn').click()
    await expect(page.getByTestId('project-table')).toContainText('E2E测试项目-已更新')

    await page.getByTestId('project-delete-1').click()
    await page.getByRole('button', { name: '确定' }).click()
    await expect(page.getByTestId('project-table')).not.toContainText('PRJ-E2E-001')
  })
})
