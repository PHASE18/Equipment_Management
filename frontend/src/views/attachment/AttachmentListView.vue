<script setup lang="ts">
// 附件列表页：按设备查询附件并提供上传、下载和删除操作。
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppPagination from '@/components/common/AppPagination.vue'
import UploadPanel from '@/components/common/UploadPanel.vue'
import { pageDevicesApi } from '@/api/device'
import { deleteFileApi, downloadFileApi, listDeviceFilesApi } from '@/api/file'
import type { Device } from '@/types/device'
import type { FileMeta } from '@/types/file'
import { FILE_CATEGORY_OPTIONS, type FileCategory } from '@/types/file'

const loading = ref(false)
const devices = ref<Device[]>([])
const files = ref<FileMeta[]>([])
const selectedDeviceId = ref<number>()
const category = ref<FileCategory>('document')
const keyword = ref('')

const query = reactive({
  pageNum: 1,
  pageSize: 20,
  keyword: ''
})
const deviceTotal = ref(0)

async function loadDevices() {
  const result = await pageDevicesApi({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    keyword: query.keyword || undefined
  })
  devices.value = result.records
  deviceTotal.value = result.total
  if (!selectedDeviceId.value && result.records.length > 0) {
    selectedDeviceId.value = result.records[0].id
    await loadFiles()
  }
}

async function loadFiles() {
  if (!selectedDeviceId.value) {
    files.value = []
    return
  }
  loading.value = true
  try {
    const list = await listDeviceFilesApi(selectedDeviceId.value)
    const kw = keyword.value.trim().toLowerCase()
    files.value = kw
      ? list.filter(item => item.fileName.toLowerCase().includes(kw) || item.fileTypeCode.toLowerCase().includes(kw))
      : list
  } finally {
    loading.value = false
  }
}

async function handleSelectDevice(deviceId: number) {
  selectedDeviceId.value = deviceId
  await loadFiles()
}

async function handleDownload(row: FileMeta) {
  try {
    await downloadFileApi(row.fileId, row.fileName)
    ElMessage.success('开始下载')
  } catch (error) {
    const message = error instanceof Error ? error.message : '下载失败'
    ElMessage.error(message)
  }
}

async function handleDelete(row: FileMeta) {
  await ElMessageBox.confirm(`确定删除附件「${row.fileName}」吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
  await deleteFileApi(row.fileId)
  ElMessage.success('删除成功')
  await loadFiles()
}

function handleSearchDevices() {
  query.pageNum = 1
  loadDevices()
}

onMounted(loadDevices)
</script>

<template>
  <div class="attachment-page" data-testid="attachment-page">
    <el-row :gutter="16">
      <el-col :span="8">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">选择设备</div>
          </template>
          <el-form :inline="true" class="search-form" @submit.prevent="handleSearchDevices">
            <el-form-item>
              <el-input
                v-model="query.keyword"
                data-testid="attachment-device-keyword"
                placeholder="设备编号/名称"
                clearable
                @keyup.enter="handleSearchDevices"
              />
            </el-form-item>
            <el-form-item>
              <el-button data-testid="attachment-device-search" type="primary" @click="handleSearchDevices">
                查询
              </el-button>
            </el-form-item>
          </el-form>
          <el-table
            :data="devices"
            highlight-current-row
            data-testid="attachment-device-table"
            @current-change="(row: Device | undefined) => row?.id && handleSelectDevice(row.id)"
          >
            <el-table-column prop="deviceNo" label="设备编号" min-width="120" />
            <el-table-column prop="deviceName" label="设备名称" min-width="140" />
          </el-table>
          <AppPagination
            v-model:page-num="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="deviceTotal"
            @change="loadDevices"
          />
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" class="panel-card">
          <template #header>
            <div class="card-header">
              <span>附件管理</span>
              <el-tag v-if="selectedDeviceId" type="info">设备ID: {{ selectedDeviceId }}</el-tag>
            </div>
          </template>

          <div v-if="selectedDeviceId" class="upload-block" data-testid="attachment-upload-block">
            <el-form :inline="true" class="search-form">
              <el-form-item label="附件分类">
                <el-select v-model="category" data-testid="attachment-category-select" style="width: 140px">
                  <el-option
                    v-for="item in FILE_CATEGORY_OPTIONS"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="关键词">
                <el-input
                  v-model="keyword"
                  data-testid="attachment-file-keyword"
                  placeholder="文件名/类型"
                  clearable
                  @keyup.enter="loadFiles"
                />
              </el-form-item>
              <el-form-item>
                <el-button data-testid="attachment-file-search" @click="loadFiles">筛选</el-button>
              </el-form-item>
            </el-form>

            <UploadPanel
              :key="`${selectedDeviceId}-${category}`"
              :device-id="selectedDeviceId"
              :category="category"
              data-testid="attachment-upload-panel"
              @success="loadFiles"
              @remove="loadFiles"
            />
          </div>
          <el-empty v-else description="请先选择左侧设备" data-testid="attachment-empty" />

          <el-table
            v-loading="loading"
            :data="files"
            stripe
            border
            class="file-table"
            data-testid="attachment-file-table"
          >
            <el-table-column prop="fileName" label="文件名" min-width="180" show-overflow-tooltip />
            <el-table-column prop="fileTypeCode" label="类型编码" width="140" />
            <el-table-column prop="fileSize" label="大小(字节)" width="120" />
            <el-table-column prop="uploadTime" label="上传时间" min-width="170" />
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }">
                <el-button
                  :data-testid="`attachment-download-${row.fileId}`"
                  link
                  type="primary"
                  @click="handleDownload(row)"
                >
                  下载
                </el-button>
                <el-button
                  :data-testid="`attachment-delete-${row.fileId}`"
                  link
                  type="danger"
                  @click="handleDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.attachment-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-card {
  border-radius: 12px;
  min-height: 640px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.search-form {
  margin-bottom: 12px;
}

.upload-block {
  margin-bottom: 16px;
}

.file-table {
  margin-top: 12px;
}
</style>
