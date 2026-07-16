<script setup lang="ts">
import { ref, watch } from 'vue'
import LifecycleHistoryTimeline from '@/components/device/LifecycleHistoryTimeline.vue'
import { listDeviceStatusHistoryApi } from '@/api/device'
import type { Device, DeviceStatusLogItem } from '@/types/device'

const visible = defineModel<boolean>({ required: true })

const props = defineProps<{
  device?: Device | null
}>()

const loading = ref(false)
const history = ref<DeviceStatusLogItem[]>([])

watch(
  () => [visible.value, props.device?.id] as const,
  async ([open, deviceId]) => {
    if (!open || !deviceId) {
      history.value = []
      return
    }
    loading.value = true
    try {
      history.value = await listDeviceStatusHistoryApi(deviceId)
    } finally {
      loading.value = false
    }
  }
)
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="`生命周期历史 - ${device?.deviceName || ''}`"
    width="680px"
    destroy-on-close
  >
    <div v-loading="loading">
      <LifecycleHistoryTimeline :history="history" />
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>
