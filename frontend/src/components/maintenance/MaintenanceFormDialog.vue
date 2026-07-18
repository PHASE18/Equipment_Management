<script setup lang="ts">
// 维修表单对话框：处理维修记录新增、编辑、设备选择和附件上传。
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import UploadPanel from '@/components/common/UploadPanel.vue'
import { listMaintenanceFilesApi } from '@/api/file'
import { createMaintenanceApi, listFaultTypesApi, updateMaintenanceApi } from '@/api/maintenance'
import { pageDevicesApi } from '@/api/device'
import type { Device } from '@/types/device'
import type { MaintenanceRecord } from '@/types/maintenance'
import type { FileMeta } from '@/types/file'

const visible = defineModel<boolean>({ required: true })

const props = defineProps<{
  record?: MaintenanceRecord | null
}>()

const emit = defineEmits<{
  success: []
}>()

const formRef = ref<FormInstance>()
const saving = ref(false)
const devices = ref<Device[]>([])
const faultTypes = ref<Array<{ dictCode: string; dictName: string }>>([])
const reportFiles = ref<FileMeta[]>([])
const currentMaintenanceId = ref<number>()

const isEdit = computed(() => !!currentMaintenanceId.value)

const form = reactive<MaintenanceRecord>({
  deviceId: undefined,
  maintenanceDate: '',
  maintenancePerson: '',
  maintenanceCompany: '',
  faultTypeCode: '',
  faultReason: '',
  faultDescription: '',
  replaceParts: '',
  maintenanceCost: undefined,
  recoverDate: '',
  isResolved: 0,
  remark: ''
})

const formRules: FormRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  maintenanceDate: [{ required: true, message: '请选择维修日期', trigger: 'change' }],
  faultTypeCode: [{ required: true, message: '请选择故障类型', trigger: 'change' }],
  faultDescription: [{ required: true, message: '请填写故障现象', trigger: 'blur' }]
}

async function loadOptions() {
  const [devicePage, types] = await Promise.all([
    pageDevicesApi({ pageNum: 1, pageSize: 500 }),
    listFaultTypesApi()
  ])
  devices.value = devicePage.records
  faultTypes.value = types
}

function resetForm() {
  Object.assign(form, {
    deviceId: undefined,
    maintenanceDate: '',
    maintenancePerson: '',
    maintenanceCompany: '',
    faultTypeCode: '',
    faultReason: '',
    faultDescription: '',
    replaceParts: '',
    maintenanceCost: undefined,
    recoverDate: '',
    isResolved: 0,
    remark: ''
  })
  reportFiles.value = []
}

watch(
  () => [visible.value, props.record] as const,
  async ([open, record]) => {
    if (!open) {
      currentMaintenanceId.value = undefined
      resetForm()
      return
    }
    await loadOptions()
    if (record?.id) {
      currentMaintenanceId.value = record.id
      Object.assign(form, record)
      reportFiles.value = await listMaintenanceFilesApi(record.id)
    } else {
      currentMaintenanceId.value = undefined
      resetForm()
    }
  }
)

async function handleSubmit() {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await updateMaintenanceApi({ ...form, id: currentMaintenanceId.value })
      ElMessage.success('维修记录已更新')
      visible.value = false
    } else {
      const id = await createMaintenanceApi(form)
      currentMaintenanceId.value = id
      ElMessage.success('维修工单已创建，可上传维修报告')
    }
    emit('success')
  } finally {
    saving.value = false
  }
}

function handleFinish() {
  visible.value = false
}
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑维修工单' : '新增维修工单'"
    width="760px"
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="96px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="绑定设备" prop="deviceId">
            <el-select
              v-model="form.deviceId"
              filterable
              placeholder="选择设备"
              :disabled="isEdit"
              style="width: 100%"
            >
              <el-option
                v-for="item in devices"
                :key="item.id"
                :label="`${item.deviceName}（${item.deviceNo}）`"
                :value="item.id!"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="维修日期" prop="maintenanceDate">
            <el-date-picker
              v-model="form.maintenanceDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="维修日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="维修人员">
            <el-input v-model="form.maintenancePerson" placeholder="维修人员" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="维修单位">
            <el-input v-model="form.maintenanceCompany" placeholder="维修单位" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="故障类型" prop="faultTypeCode">
            <el-select v-model="form.faultTypeCode" clearable placeholder="故障类型" style="width: 100%">
              <el-option
                v-for="item in faultTypes"
                :key="item.dictCode"
                :label="item.dictName"
                :value="item.dictCode"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="维修费用">
            <el-input-number v-model="form.maintenanceCost" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="故障现象" prop="faultDescription">
            <el-input v-model="form.faultDescription" type="textarea" :rows="2" placeholder="故障现象描述" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="故障原因">
            <el-input v-model="form.faultReason" type="textarea" :rows="2" placeholder="故障原因" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="更换部件">
            <el-input v-model="form.replaceParts" placeholder="更换部件" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="恢复日期">
            <el-date-picker
              v-model="form.recoverDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="恢复日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="是否解决">
            <el-radio-group v-model="form.isResolved">
              <el-radio :value="0">未解决</el-radio>
              <el-radio :value="1">已解决</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="维修报告">
            <UploadPanel
              v-if="currentMaintenanceId && form.deviceId"
              v-model="reportFiles"
              :device-id="form.deviceId"
              :maintenance-id="currentMaintenanceId"
              category="document"
              file-type-code="MAINTENANCE_REPORT"
            />
            <el-alert
              v-else
              title="保存工单后可上传维修报告附件"
              type="info"
              :closable="false"
              show-icon
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-alert
        title="新增维修工单时，若设备状态为「在用」，系统将自动流转为「维修中」；标记已解决后将自动恢复为「在用」。"
        type="info"
        :closable="false"
        show-icon
      />
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-if="!isEdit && currentMaintenanceId" type="primary" @click="handleFinish">完成</el-button>
      <el-button v-else type="primary" :loading="saving" @click="handleSubmit">
        {{ isEdit ? '保存' : '创建工单' }}
      </el-button>
    </template>
  </el-dialog>
</template>
