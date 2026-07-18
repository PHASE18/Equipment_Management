import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { PageQuery, PageResult } from '@/types/api'

export interface CrudPageOptions<T> {
  /** 分页查询数据源。 */
  fetchPage: (query: PageQuery & Record<string, unknown>) => Promise<PageResult<T>>
  /** 新增记录；缺省时表示页面只读。 */
  createItem?: (data: T) => Promise<void>
  /** 更新记录；缺省时表示页面不支持编辑。 */
  updateItem?: (data: T) => Promise<void>
  /** 删除记录；缺省时表示页面不支持删除。 */
  deleteItem?: (id: number) => Promise<void>
  /** 返回新增表单的初始值。 */
  defaultForm: () => T
  /** 删除确认提示中显示的业务名称。 */
  entityName?: string
}

/** 封装后台列表页通用的分页、表单、保存和删除状态。 */
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

  /** 按当前查询条件加载分页数据，并同步加载状态。 */
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

  /** 搜索时回到第一页，避免当前页超出新结果范围。 */
  function handleSearch() {
    query.pageNum = 1
    loadData()
  }

  /** 清空关键词并重新加载第一页。 */
  function resetQuery() {
    query.keyword = ''
    query.pageNum = 1
    loadData()
  }

  /** 打开新增对话框并重置表单。 */
  function openCreate() {
    isEdit.value = false
    Object.assign(form, options.defaultForm())
    dialogVisible.value = true
  }

  /** 将选中行复制到表单并打开编辑对话框。 */
  function openEdit(row: T) {
    isEdit.value = true
    Object.assign(form, options.defaultForm(), row)
    dialogVisible.value = true
  }

  /** 根据当前模式调用新增或更新接口，成功后刷新列表。 */
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

  /** 二次确认后删除记录，并刷新列表数据。 */
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
