<script setup lang="ts">
import { reactive, watch } from 'vue'
import { ElMessage, type FormRules } from 'element-plus'
import StatusSelect from '@/components/common/StatusSelect.vue'
import type { Device, LifecycleTransitionRequest } from '@/types/device'

const visible = defineModel<boolean>({ required: true })

const props = defineProps<{
  device?: Device
  submitHandler: (data: LifecycleTransitionRequest) => Promise<unknown>
}>()

const form = reactive({
  toStatus: '',
  reason: '',
  remark: ''
})

const rules: FormRules = {
  toStatus: [{ required: true, message: '请选择目标状态', trigger: 'change' }],
  reason: [{ required: true, message: '请输入流转原因', trigger: 'blur' }]
}

watch(
  () => props.device,
  device => {
    form.toStatus = device?.statusCode || ''
    form.reason = ''
    form.remark = ''
  }
)

async function submit() {
  if (!props.device) return
  await props.submitHandler({
    deviceId: props.device.id,
    fromStatus: props.device.statusCode,
    toStatus: form.toStatus,
    reason: form.reason,
    remark: form.remark
  })
  ElMessage.success('状态流转成功')
  visible.value = false
}
</script>

<template>
  <el-dialog v-model="visible" title="设备生命周期流转" width="520px">
    <el-form :model="form" :rules="rules" label-width="96px">
      <el-form-item label="设备">
        <span>{{ device?.deviceName }} / {{ device?.deviceNo }}</span>
      </el-form-item>
      <el-form-item label="当前状态">
        <StatusSelect :model-value="device?.statusCode" disabled />
      </el-form-item>
      <el-form-item label="目标状态" prop="toStatus">
        <StatusSelect v-model="form.toStatus" />
      </el-form-item>
      <el-form-item label="流转原因" prop="reason">
        <el-input v-model="form.reason" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="submit">确认流转</el-button>
    </template>
  </el-dialog>
</template>
