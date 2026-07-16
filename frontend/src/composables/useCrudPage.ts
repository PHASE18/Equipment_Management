import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { PageQuery, PageResult } from '@/types/api'

export interface CrudPageOptions<T> {
  fetchPage: (query: PageQuery & Record<string, unknown>) => Promise<PageResult<T>>
  createItem?: (data: T) => Promise<void>
  updateItem?: (data: T) => Promise<void>
  deleteItem?: (id: number) => Promise<void>
  defaultForm: () => T
  entityName?: string
}

export function useCrudPage<T extends { id?: number }>(options: CrudPageOptions<T>) {
  const loading = ref(false)
  const saving = ref(false)
  const dialogVisible = ref(false)
  const isEdit = ref(false)
  const tableData = ref<T[]>([])
  const total = ref(0)
  const form = reactive(options.defaultForm()) as T

  const query = reactive({
    pageNum: 1,
    pageSize: 20,
    keyword: ''
  })

  async function loadData() {
    loading.value = true
    try {
      const result = await options.fetchPage({ ...query })
      tableData.value = result.records
      total.value = result.total
    } finally {
      loading.value = false
    }
  }

  function handleSearch() {
    query.pageNum = 1
    loadData()
  }

  function resetQuery() {
    query.keyword = ''
    query.pageNum = 1
    loadData()
  }

  function openCreate() {
    isEdit.value = false
    Object.assign(form, options.defaultForm())
    dialogVisible.value = true
  }

  function openEdit(row: T) {
    isEdit.value = true
    Object.assign(form, options.defaultForm(), row)
    dialogVisible.value = true
  }

  async function submitForm() {
    saving.value = true
    try {
      if (isEdit.value) {
        await options.updateItem?.(form)
        ElMessage.success('更新成功')
      } else {
        await options.createItem?.(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      await loadData()
    } finally {
      saving.value = false
    }
  }

  async function handleDelete(row: T) {
    if (!row.id) {
      return
    }
    await ElMessageBox.confirm(`确定删除该${options.entityName || '记录'}吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await options.deleteItem?.(row.id)
    ElMessage.success('删除成功')
    await loadData()
  }

  return {
    loading,
    saving,
    dialogVisible,
    isEdit,
    tableData,
    total,
    form,
    query,
    loadData,
    handleSearch,
    resetQuery,
    openCreate,
    openEdit,
    submitForm,
    handleDelete
  }
}
