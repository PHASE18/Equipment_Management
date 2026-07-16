<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import type { UploadFile, UploadProps, UploadRequestOptions, UploadUserFile } from 'element-plus'
import { ElMessage } from 'element-plus'
import { deleteFileApi, previewFileApi, uploadFileApi } from '@/api/file'
import type { FileCategory, FileMeta } from '@/types/file'
import { getCategoryConfig } from '@/types/file'

const files = defineModel<FileMeta[]>({ default: () => [] })

const props = withDefaults(
  defineProps<{
    deviceId: number
    maintenanceId?: number
    category?: FileCategory
    fileTypeCode?: string
    limit?: number
    disabled?: boolean
    showFileList?: boolean
    listType?: 'text' | 'picture' | 'picture-card'
  }>(),
  {
    category: 'document',
    limit: 5,
    disabled: false,
    showFileList: true,
    listType: 'text'
  }
)

const emit = defineEmits<{
  success: [file: FileMeta]
  remove: [file: FileMeta]
}>()

const uploading = ref(false)
const uploadFileList = ref<UploadUserFile[]>([])

const categoryConfig = computed(() => getCategoryConfig(props.category))

watch(
  () => files.value,
  value => {
    uploadFileList.value = value.map(item => ({
      name: item.fileName,
      url: item.url,
      status: 'success',
      uid: item.fileId
    }))
  },
  { immediate: true, deep: true }
)

const beforeUpload: UploadProps['beforeUpload'] = file => {
  const maxSize = 50 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

async function customUpload(options: UploadRequestOptions) {
  if (!props.deviceId) {
    ElMessage.error('请先选择设备后再上传附件')
    options.onError?.(new Error('deviceId is required') as never)
    return
  }
  if (props.category === 'document' && props.fileTypeCode === 'MAINTENANCE_REPORT' && !props.maintenanceId) {
    ElMessage.error('请先保存维修工单后再上传报告')
    options.onError?.(new Error('maintenanceId is required') as never)
    return
  }

  uploading.value = true
  try {
    const result = await uploadFileApi(options.file as File, {
      deviceId: props.deviceId,
      maintenanceId: props.maintenanceId,
      category: props.category,
      fileTypeCode: props.fileTypeCode
    })
    files.value = [...files.value, result]
    emit('success', result)
    options.onSuccess?.(result)
    ElMessage.success('上传成功')
  } catch (error) {
    options.onError?.(error as never)
  } finally {
    uploading.value = false
  }
}

async function handleRemove(uploadFile: UploadFile) {
  const target = files.value.find(item => item.fileId === uploadFile.uid || item.url === uploadFile.url)
  if (!target) {
    return true
  }
  try {
    await deleteFileApi(target.fileId)
    files.value = files.value.filter(item => item.fileId !== target.fileId)
    emit('remove', target)
    ElMessage.success('删除成功')
    return true
  } catch {
    return false
  }
}

async function handlePreview(uploadFile: UploadFile) {
  const target = files.value.find(item => item.fileId === uploadFile.uid || item.url === uploadFile.url)
  if (!target) {
    return
  }
  try {
    const blobUrl = await previewFileApi(target.fileId)
    window.open(blobUrl, '_blank')
  } catch {
    ElMessage.error('文件预览失败')
  }
}
</script>

<template>
  <el-upload
    v-model:file-list="uploadFileList"
    class="upload-panel"
    :accept="categoryConfig.accept"
    :limit="limit"
    :disabled="disabled || uploading || !deviceId"
    :show-file-list="showFileList"
    :list-type="listType"
    :before-upload="beforeUpload"
    :http-request="customUpload"
    :on-preview="handlePreview"
    :on-remove="handleRemove"
    multiple
  >
    <el-button type="primary" :icon="Plus" :loading="uploading">
      上传{{ categoryConfig.label }}
    </el-button>
    <template #tip>
      <div class="upload-tip">{{ categoryConfig.tip }}</div>
    </template>
  </el-upload>
</template>

<style scoped>
.upload-panel {
  width: 100%;
}

.upload-tip {
  margin-top: 6px;
  color: #6b7280;
  font-size: 12px;
}
</style>
