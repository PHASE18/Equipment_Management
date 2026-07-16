<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import StatusSelect from '@/components/common/StatusSelect.vue'
import LifecycleHistoryTimeline from '@/components/device/LifecycleHistoryTimeline.vue'
import { changeDeviceStatusApi, listAllowedStatusTransitionsApi } from '@/api/device'
import type { Device, DeviceStatusChangeResult } from '@/types/device'
import { getDeviceStatusLabel } from '@/types/device'

const visible = defineModel<boolean>({ required: true })

const props = defineProps<{
  device?: Device
}>()

const emit = defineEmits<{
  success: [result: DeviceStatusChangeResult]
}>()

const formRef = ref<FormInstance>()
const step = ref<'form' | 'result'>('form')
const loading = ref(false)
const allowedStatuses = ref<string[]>([])
const changeResult = ref<DeviceStatusChangeResult | null>(null)

const form = reactive({
  toStatus: '',
  reason: '',
  remark: ''
})

const rules: FormRules = {
  toStatus: [{ required: true, message: '请选择目标状态', trigger: 'change' }],
  reason: [{ required: true, message: '请输入流转原因', trigger: 'blur' }]
}

async function loadAllowedStatuses(deviceId: number) {
  allowedStatuses.value = await listAllowedStatusTransitionsApi(deviceId)
}

watch(
  () => [visible.value, props.device?.id] as const,
  async ([open, deviceId]) => {
    if (!open) {
      step.value = 'form'
      changeResult.value = null
      form.toStatus = ''
      form.reason = ''
      form.remark = ''
      allowedStatuses.value = []
      return
    }
    if (deviceId) {
      await loadAllowedStatuses(deviceId)
    }
    form.toStatus = ''
    form.reason = ''
    form.remark = ''
  }
)

async function submit() {
  if (!props.device?.id || !formRef.value) {
    return
  }
  await formRef.value.validate()
  loading.value = true
  try {
    const result = await changeDeviceStatusApi({
      deviceId: props.device.id,
      fromStatus: props.device.statusCode,
      toStatus: form.toStatus,
      reason: form.reason,
      remark: form.remark || undefined
    })
    changeResult.value = result
    step.value = 'result'
    emit('success', result)
    ElMessage.success('状态流转成功')
  } finally {
    loading.value = false
  }
}

function handleClose() {
  visible.value = false
}
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="step === 'form' ? '设备生命周期流转' : '流转成功'"
    width="640px"
    destroy-on-close
  >
    <template v-if="step === 'form'">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="设备">
          <span>{{ device?.deviceName }} / {{ device?.deviceNo }}</span>
        </el-form-item>
        <el-form-item label="当前状态">
          <StatusSelect :model-value="device?.statusCode" disabled />
        </el-form-item>
        <el-form-item label="目标状态" prop="toStatus">
          <StatusSelect
            v-model="form.toStatus"
            :allowed-values="allowedStatuses"
            placeholder="请选择合法目标状态"
            style="width: 100%"
          />
        </el-form-item>
        <el-alert
          v-if="allowedStatuses.length === 0"
          title="当前状态已无法继续流转（如已报废）"
          type="warning"
          :closable="false"
          show-icon
          style="margin-bottom: 16px"
        />
        <el-form-item label="流转原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请填写变更原因" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
    </template>

    <template v-else-if="changeResult">
      <el-result icon="success" title="状态流转完成">
        <template #sub-title>
          {{ changeResult.oldStatusName || getDeviceStatusLabel(changeResult.oldStatusCode) }}
          →
          {{ changeResult.newStatusName || getDeviceStatusLabel(changeResult.newStatusCode) }}
        </template>
      </el-result>
      <div class="history-title">完整流转历史</div>
      <LifecycleHistoryTimeline :history="changeResult.history" />
    </template>

    <template #footer>
      <el-button @click="handleClose">{{ step === 'result' ? '关闭' : '取消' }}</el-button>
      <el-button
        v-if="step === 'form'"
        type="primary"
        :loading="loading"
        :disabled="allowedStatuses.length === 0"
        @click="submit"
      >
        确认流转
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.history-title {
  margin: 8px 0 12px;
  font-size: 14px;
  font-weight: 600;
}
</style>
