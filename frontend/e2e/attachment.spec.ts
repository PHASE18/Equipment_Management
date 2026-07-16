import { expect, test } from '@playwright/test'
import { loginAsAdmin, ok } from './helpers/auth'

const devicesPage = {
  records: [
    { id: 1, deviceNo: 'DEV-001', deviceName: '核心交换机' },
    { id: 2, deviceNo: 'DEV-002', deviceName: '机房服务器' }
  ],
  total: 2,
  pageNum: 1,
  pageSize: 20,
  pages: 1
}

async function mockAttachmentApis(page: import('@playwright/test').Page) {
  let files = [
    {
      fileId: 11,
      deviceId: 1,
      fileName: '合同A.pdf',
      fileTypeCode: 'PURCHASE_CONTRACT',
      fileSize: 1024,
      filePath: 'contract/device/1/a.pdf',
      url: 'http://mock/a.pdf',
      uploadTime: '2026-07-16 12:00:00'
    },
    {
      fileId: 12,
      deviceId: 1,
      fileName: '机柜照片.png',
      fileTypeCode: 'DEVICE_PHOTO',
      fileSize: 2048,
      filePath: 'image/device/1/b.png',
      url: 'http://mock/b.png',
      uploadTime: '2026-07-16 12:10:00'
    }
  ]

  await page.route(/\/api\/device\/list(\?.*)?$/, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok(devicesPage))
    })
  })

  await page.route(/\/api\/file\/list\/\d+$/, async route => {
    const deviceId = Number(route.request().url().split('/').pop())
    const records = files.filter(item => item.deviceId === deviceId)
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok(records))
    })
  })

  await page.route('**/api/file/upload', async route => {
    const created = {
      fileId: 99,
      deviceId: 1,
      fileName: '新上传.txt',
      fileTypeCode: 'OTHER_DOC',
      category: 'document',
      fileSize: 12,
      filePath: 'document/device/1/new.txt',
      url: 'http://mock/new.txt',
      uploadTime: '2026-07-16 13:00:00'
    }
    files = [...files, created]
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok(created))
    })
  })

  await page.route(/\/api\/file\/\d+$/, async route => {
    if (route.request().method() !== 'DELETE') {
      await route.fallback()
      return
    }
    const id = Number(route.request().url().split('/').pop())
    files = files.filter(item => item.fileId !== id)
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify(ok(null))
    })
  })

  await page.route(/\/api\/file\/download\/\d+$/, async route => {
    await route.fulfill({
      status: 200,
      contentType: 'application/pdf',
      body: 'mock-file-content'
    })
  })
}

test.describe('Attachment module E2E', () => {
  test('list devices and attachments', async ({ page }) => {
    await mockAttachmentApis(page)
    await loginAsAdmin(page, { path: '/attachments', testId: 'attachment-page' })

    await expect(page.getByTestId('attachment-page')).toBeVisible()
    await expect(page.getByTestId('attachment-device-table')).toContainText('DEV-001')
    await expect(page.getByTestId('attachment-file-table')).toContainText('合同A.pdf')
    await expect(page.getByTestId('attachment-file-table')).toContainText('机柜照片.png')
  })

  test('filter attachments by keyword', async ({ page }) => {
    await mockAttachmentApis(page)
    await loginAsAdmin(page, { path: '/attachments', testId: 'attachment-page' })

    await page.getByTestId('attachment-file-keyword').fill('合同')
    await page.getByTestId('attachment-file-search').click()
    await expect(page.getByTestId('attachment-file-table')).toContainText('合同A.pdf')
    await expect(page.getByTestId('attachment-file-table')).not.toContainText('机柜照片.png')
  })

  test('delete attachment with confirm', async ({ page }) => {
    await mockAttachmentApis(page)
    await loginAsAdmin(page, { path: '/attachments', testId: 'attachment-page' })

    await page.getByTestId('attachment-delete-11').click()
    await page.getByRole('button', { name: '确定' }).click()
    await expect(page.getByTestId('attachment-file-table')).not.toContainText('合同A.pdf')
  })
})
