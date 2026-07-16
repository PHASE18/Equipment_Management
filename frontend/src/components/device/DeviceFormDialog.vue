<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import UploadPanel from '@/components/common/UploadPanel.vue'
import StatusSelect from '@/components/common/StatusSelect.vue'
import {
  createDeviceApi,
  getDeviceIpApi,
  listDeviceProjectIdsApi,
  pageDevicesApi,
  saveDeviceIpApi,
  syncDeviceProjectsApi,
  updateDeviceApi
} from '@/api/device'
import { listDeviceFilesApi } from '@/api/file'
import { pageProjectsApi } from '@/api/project'
import { buildDepartmentTreeOptions, departmentApi, deviceBrandApi, deviceTypeApi } from '@/api/system'
import type { SysDepartment, SysDict } from '@/types/system'
import type { Device, DeviceIp, Project } from '@/types/device'
import type { FileMeta } from '@/types/file'

const visible = defineModel<boolean>({ required: true })

const props = defineProps<{
  device?: Device | null
}>()

const emit = defineEmits<{
  success: []
}>()

const formRef = ref<FormInstance>()
const ipFormRef = ref<FormInstance>()
const saving = ref(false)
const activeTab = ref('basic')
const currentDeviceId = ref<number>()
const departments = ref<SysDepartment[]>([])
const brands = ref<SysDict[]>([])
const deviceTypes = ref<SysDict[]>([])
const projects = ref<Project[]>([])
const projectIds = ref<number[]>([])
const contractFiles = ref<FileMeta[]>([])
const photoFiles = ref<FileMeta[]>([])

const isEdit = computed(() => !!currentDeviceId.value)

const departmentOptions = computed(() => buildDepartmentTreeOptions(departments.value))

const form = reactive<Device>({
  deviceNo: '',
  deviceName: '',
  sn: '',
  assetNo: '',
  brandCode: '',
  model: '',
  deviceTypeCode: '',
  departmentId: undefined,
  supplier: '',
  maintenanceCompany: '',
  purchaseDate: '',
  warrantyEnd: '',
  statusCode: 'PURCHASING',
  cabinet: '',
  location: '',
  remark: ''
})

const ipForm = reactive<DeviceIp>({
  deviceId: undefined,
  businessIp: '',
  managementIp: '',
  mask: '',
  gateway: ''
})

const formRules: FormRules = {
  deviceNo: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  statusCode: [{ required: true, message: '请选择设备状态', trigger: 'change' }]
}

const ipRules: FormRules = {
  businessIp: [
    {
      pattern: /^$|^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/,
      message: '业务IP格式不正确',
      trigger: 'blur'
    }
  ],
  managementIp: [
    {
      pattern: /^$|^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/,
      message: '管理IP格式不正确',
      trigger: 'blur'
    }
  ],
  mask: [
    {
      pattern: /^$|^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/,
      message: '子网掩码格式不正确',
      trigger: 'blur'
    }
  ],
  gateway: [
    {
      pattern: /^$|^((25[0-5]|2[0-4]\d|[01]?\d\d?)\.){3}(25[0-5]|2[0-4]\d|[01]?\d\d?)$/,
      message: '网关格式不正确',
      trigger: 'blur'
    }
  ]
}

function resetForm() {
  Object.assign(form, {
    deviceNo: '',
    deviceName: '',
    sn: '',
    assetNo: '',
    brandCode: '',
    model: '',
    deviceTypeCode: '',
    departmentId: undefined,
    supplier: '',
    maintenanceCompany: '',
    purchaseDate: '',
    warrantyEnd: '',
    statusCode: 'PURCHASING',
    cabinet: '',
    location: '',
    remark: ''
  })
  Object.assign(ipForm, {
    deviceId: undefined,
    businessIp: '',
    managementIp: '',
    mask: '',
    gateway: ''
  })
  projectIds.value = []
  contractFiles.value = []
  photoFiles.value = []
  activeTab.value = 'basic'
}

async function loadOptions() {
  const [deptList, brandPage, typePage, projectPage] = await Promise.all([
    departmentApi.tree(),
    deviceBrandApi.page({ pageNum: 1, pageSize: 200 }),
    deviceTypeApi.page({ pageNum: 1, pageSize: 200 }),
    pageProjectsApi({ pageNum: 1, pageSize: 200 })
  ])
  departments.value = deptList
  brands.value = brandPage.records
  deviceTypes.value = typePage.records
  projects.value = projectPage.records
}

async function loadRelatedData(deviceId: number) {
  const [ip, ids, files] = await Promise.all([
    getDeviceIpApi(deviceId).catch(() => null),
    listDeviceProjectIdsApi(deviceId),
    listDeviceFilesApi(deviceId)
  ])
  Object.assign(ipForm, {
    deviceId,
    businessIp: ip?.businessIp || '',
    managementIp: ip?.managementIp || '',
    mask: ip?.mask || '',
    gateway: ip?.gateway || ''
  })
  projectIds.value = ids
  contractFiles.value = files.filter(item => item.fileTypeCode === 'PURCHASE_CONTRACT')
  photoFiles.value = files.filter(item => item.fileTypeCode === 'DEVICE_PHOTO')
}

watch(
  () => [visible.value, props.device] as const,
  async ([open, device]) => {
    if (!open) {
      currentDeviceId.value = undefined
      resetForm()
      return
    }
    await loadOptions()
    if (device?.id) {
      currentDeviceId.value = device.id
      Object.assign(form, device)
      await loadRelatedData(device.id)
    } else {
      currentDeviceId.value = undefined
      resetForm()
    }
  },
  { immediate: true }
)

function hasIpData() {
  return !!(ipForm.businessIp || ipForm.managementIp || ipForm.mask || ipForm.gateway)
}

async function resolveDeviceIdAfterCreate() {
  const result = await pageDevicesApi({ pageNum: 1, pageSize: 1, deviceNo: form.deviceNo })
  return result.records[0]?.id
}

async function saveIp(deviceId: number) {
  if (!hasIpData()) {
    return
  }
  await saveDeviceIpApi({ ...ipForm, deviceId })
}

async function saveProjects(deviceId: number) {
  await syncDeviceProjectsApi(deviceId, projectIds.value)
}

async function handleSubmit() {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate()
  if (hasIpData() && ipFormRef.value) {
    await ipFormRef.value.validate()
  }

  saving.value = true
  try {
    let deviceId = currentDeviceId.value
    if (deviceId) {
      await updateDeviceApi({ ...form, id: deviceId })
    } else {
      await createDeviceApi(form)
      deviceId = await resolveDeviceIdAfterCreate()
      if (!deviceId) {
        throw new Error('创建设备后未能获取设备ID')
      }
      currentDeviceId.value = deviceId
    }

    await saveIp(deviceId)
    await saveProjects(deviceId)
    ElMessage.success(isEdit.value ? '设备更新成功' : '设备创建成功，可继续上传附件')
    emit('success')
    if (props.device) {
      visible.value = false
    } else {
      activeTab.value = 'attachment'
    }
  } finally {
    saving.value = false
  }
}

function handleFinish() {
  visible.value = false
  emit('success')
}
</script>

<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑设备' : '新增设备'"
    width="860px"
    destroy-on-close
    class="device-form-dialog"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-form ref="formRef" :model="form" :rules="formRules" label-width="96px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="设备编号" prop="deviceNo">
                <el-input v-model="form.deviceNo" :disabled="isEdit" placeholder="唯一编号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备名称" prop="deviceName">
                <el-input v-model="form.deviceName" placeholder="设备名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="SN号">
                <el-input v-model="form.sn" placeholder="序列号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="资产编号">
                <el-input v-model="form.assetNo" placeholder="资产编号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="品牌">
                <el-select v-model="form.brandCode" clearable filterable placeholder="选择品牌" style="width: 100%">
                  <el-option
                    v-for="item in brands"
                    :key="item.dictCode"
                    :label="item.dictName"
                    :value="item.dictCode"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备类型">
                <el-select
                  v-model="form.deviceTypeCode"
                  clearable
                  filterable
                  placeholder="选择类型"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in deviceTypes"
                    :key="item.dictCode"
                    :label="item.dictName"
                    :value="item.dictCode"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="型号">
                <el-input v-model="form.model" placeholder="型号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属部门">
                <el-tree-select
                  v-model="form.departmentId"
                  :data="departmentOptions"
                  check-strictly
                  clearable
                  filterable
                  placeholder="选择部门"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备状态" prop="statusCode">
                <StatusSelect v-model="form.statusCode" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="采购日期">
                <el-date-picker
                  v-model="form.purchaseDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="采购日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="保修截止">
                <el-date-picker
                  v-model="form.warrantyEnd"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="保修截止日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="供应商">
                <el-input v-model="form.supplier" placeholder="供应商" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="维保单位">
                <el-input v-model="form.maintenanceCompany" placeholder="维保单位" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="机柜位置">
                <el-input v-model="form.cabinet" placeholder="机柜位置" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="物理位置">
                <el-input v-model="form.location" placeholder="物理位置" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="网络信息" name="network">
        <el-form ref="ipFormRef" :model="ipForm" :rules="ipRules" label-width="96px">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="业务IP" prop="businessIp">
                <el-input v-model="ipForm.businessIp" placeholder="如 192.168.1.10" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="管理IP" prop="managementIp">
                <el-input v-model="ipForm.managementIp" placeholder="如 10.0.0.10" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="子网掩码" prop="mask">
                <el-input v-model="ipForm.mask" placeholder="如 255.255.255.0" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="网关" prop="gateway">
                <el-input v-model="ipForm.gateway" placeholder="如 192.168.1.1" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-alert
            title="业务IP与管理IP在系统内全局唯一，保存时将自动校验。"
            type="info"
            :closable="false"
            show-icon
          />
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="项目关联" name="project">
        <el-form label-width="96px">
          <el-form-item label="关联项目">
            <el-select
              v-model="projectIds"
              multiple
              clearable
              filterable
              placeholder="选择关联项目"
              style="width: 100%"
            >
              <el-option
                v-for="item in projects"
                :key="item.id"
                :label="`${item.projectName}（${item.projectCode}）`"
                :value="item.id!"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="附件管理" name="attachment" :disabled="!currentDeviceId">
        <template v-if="currentDeviceId">
          <div class="attachment-section">
            <h4>采购合同</h4>
            <UploadPanel
              v-model="contractFiles"
              :device-id="currentDeviceId"
              category="contract"
              file-type-code="PURCHASE_CONTRACT"
            />
          </div>
          <div class="attachment-section">
            <h4>设备照片</h4>
            <UploadPanel
              v-model="photoFiles"
              :device-id="currentDeviceId"
              category="image"
              file-type-code="DEVICE_PHOTO"
              list-type="picture-card"
            />
          </div>
        </template>
        <el-empty v-else description="请先保存设备基本信息后再上传附件" />
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
        v-if="activeTab === 'attachment' && currentDeviceId && !props.device"
        type="primary"
        @click="handleFinish"
      >
        完成
      </el-button>
      <el-button v-else type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.attachment-section + .attachment-section {
  margin-top: 20px;
}

.attachment-section h4 {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
}
</style>
