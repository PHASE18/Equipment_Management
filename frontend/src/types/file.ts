/** 文件业务分类，用于选择上传校验规则和展示方式。 */
export type FileCategory = 'image' | 'document' | 'excel' | 'contract'

export interface FileMeta {
  fileId: number
  deviceId: number
  maintenanceId?: number
  fileName: string
  fileTypeCode: string
  category?: FileCategory
  fileSize: number
  filePath: string
  url: string
  uploadTime?: string
}

export const FILE_CATEGORY_OPTIONS: Array<{ label: string; value: FileCategory; accept: string; tip: string }> = [
  {
    label: '图片',
    value: 'image',
    accept: '.jpg,.jpeg,.png,.gif,.webp,.bmp',
    tip: '支持 JPG、PNG、GIF、WEBP、BMP，最大 50MB'
  },
  {
    label: '文档',
    value: 'document',
    accept: '.pdf,.doc,.docx,.txt',
    tip: '支持 PDF、Word、TXT，最大 50MB'
  },
  {
    label: 'Excel',
    value: 'excel',
    accept: '.xls,.xlsx',
    tip: '支持 XLS、XLSX，最大 50MB'
  },
  {
    label: '合同',
    value: 'contract',
    accept: '.pdf,.doc,.docx',
    tip: '支持 PDF、Word 合同文件，最大 50MB'
  }
]

export function getCategoryConfig(category: FileCategory) {
  return FILE_CATEGORY_OPTIONS.find(item => item.value === category) ?? FILE_CATEGORY_OPTIONS[1]
}
