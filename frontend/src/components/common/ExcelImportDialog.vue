<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, type UploadFile } from 'element-plus'
import * as XLSX from 'xlsx'

const visible = defineModel<boolean>({ required: true })

const props = withDefaults(
  defineProps<{
    title?: string
    templateUrl?: string
    importHandler: (file: File) => Promise<unknown>
  }>(),
  {
    title: 'Excel 导入'
  }
)

const selectedFile = ref<File>()
const previewRows = ref<Record<string, unknown>[]>([])
const loading = ref(false)

async function handleFileChange(uploadFile: UploadFile) {
  selectedFile.value = uploadFile.raw
  previewRows.value = []
  if (!uploadFile.raw) return
  const buffer = await uploadFile.raw.arrayBuffer()
  const workbook = XLSX.read(buffer)
  const sheet = workbook.Sheets[workbook.SheetNames[0]]
  previewRows.value = XLSX.utils.sheet_to_json<Record<string, unknown>>(sheet).slice(0, 5)
}

async function submit() {
  if (!selectedFile.value) {
    ElMessage.warning('请选择 Excel 文件')
    return
  }
  loading.value = true
  try {
    await props.importHandler(selectedFile.value)
    ElMessage.success('导入任务已提交')
    visible.value = false
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <el-dialog v-model="visible" :title="title" width="680px">
    <div class="excel-dialog">
      <div class="dialog-actions">
        <el-upload accept=".xls,.xlsx" :auto-upload="false" :show-file-list="true" :limit="1" @change="handleFileChange">
          <el-button type="primary">选择文件</el-button>
        </el-upload>
        <el-button v-if="templateUrl" tag="a" :href="templateUrl">下载模板</el-button>
      </div>

      <el-table v-if="previewRows.length" :data="previewRows" size="small" border max-height="260">
        <el-table-column
          v-for="column in Object.keys(previewRows[0])"
          :key="column"
          :prop="column"
          :label="column"
          min-width="130"
          show-overflow-tooltip
        />
      </el-table>
      <el-empty v-else description="请选择 Excel 文件预览前 5 行" />
    </div>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="loading" @click="submit">开始导入</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.excel-dialog {
  display: grid;
  gap: 14px;
}

.dialog-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
