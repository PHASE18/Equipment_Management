<script setup lang="ts">
// 设备表单对话框：复用于设备新增和编辑，负责表单校验及关联数据选择。
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import UploadPanel from '@/components/common/UploadPanel.vue'
import StatusSelect from '@/components/common/StatusSelect.vue'
import {
  createDeviceApi,
  getDeviceConfigApi,
  getDeviceIpApi,
  listDeviceProjectIdsApi,
  pageDevicesApi,
  saveDeviceConfigApi,
  saveDeviceIpApi,
  syncDeviceProjectsApi,
  updateDeviceApi
} from '@/api/device'
import { listDeviceFilesApi } from '@/api/file'
import { pageProjectsApi } from '@/api/project'
import { optionsApi, type UserOption } from '@/api/options'
import { buildDepartmentTreeOptions } from '@/api/system'
import type { SysDepartment, SysDict } from '@/types/system'
import type { Device, DeviceConfig, DeviceIp, Project } from '@/types/device'
import { MGMT_LOGIN_METHOD_OPTIONS } from '@/types/device'
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
const users = ref<UserOption[]>([])
const projectIds = ref<number[]>([])
const contractFiles = ref<FileMeta[]>([])
const photoFiles = ref<FileMeta[]>([])

const isEdit = computed(() => !!currentDeviceId.value)

const departmentOptions = computed(() => buildDepartmentTreeOptions(departments.value))

const emptyDevice = (): Device => ({
  deviceNo: '',
  deviceName: '',
  sn: '',
  assetNo: '',
  isFixedAsset: 0,
  brandCode: '',
  model: '',
  deviceTypeCode: '',
  departmentId: undefined,
  useDepartmentId: undefined,
  managerUserId: undefined,
  useUserName: '',
  originalValue: undefined,
  approvalNo: '',
  supplier: '',
  maintenanceCompany: '',
  purchaseDate: '',
  manufactureDate: '',
  onlineDate: '',
  warrantyEnd: '',
  scrapDate: '',
  statusCode: 'PURCHASING',
  cabinet: '',
  location: '',
  remark: ''
})

const emptyIp = (): DeviceIp => ({
  deviceId: undefined,
  businessIp: '',
  managementIp: '',
  mask: '',
  gateway: '',
  mountedBusiness: '',
  networkZone: '',
  mgmtLoginMethod: ''
})

const emptyConfig = (): DeviceConfig => ({
  deviceId: undefined,
  cpu: '',
  memory: '',
  disk: '',
  raid: '',
  gpu: '',
  fiberCard: '',
  nic: '',
  powerSupply: '',
  os: '',
  dbVersion: '',
  firmware: '',
  bios: '',
  remark: ''
})

const form = reactive<Device>(emptyDevice())
const ipForm = reactive<DeviceIp>(emptyIp())
const configForm = reactive<DeviceConfig>(emptyConfig())

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
  Object.assign(form, emptyDevice())
  Object.assign(ipForm, emptyIp())
  Object.assign(configForm, emptyConfig())
  projectIds.value = []
  contractFiles.value = []
  photoFiles.value = []
  activeTab.value = 'basic'
}

async function loadOptions() {
  const [deptList, brandList, typeList, projectPage, userList] = await Promise.all([
    optionsApi.departments(),
    optionsApi.brands(),
    optionsApi.deviceTypes(),
    pageProjectsApi({ pageNum: 1, pageSize: 200 }),
    optionsApi.users()
  ])
  departments.value = deptList
  brands.value = brandList
  deviceTypes.value = typeList
  projects.value = projectPage.records
  users.value = userList
}

async function loadRelatedData(deviceId: number) {
  const [ip, config, ids, files] = await Promise.all([
    getDeviceIpApi(deviceId).catch(() => null),
    getDeviceConfigApi(deviceId).catch(() => null),
    listDeviceProjectIdsApi(deviceId),
    listDeviceFilesApi(deviceId)
  ])
  Object.assign(ipForm, {
    ...emptyIp(),
    deviceId,
    businessIp: ip?.businessIp || '',
    managementIp: ip?.managementIp || '',
    mask: ip?.mask || '',
    gateway: ip?.gateway || '',
    mountedBusiness: ip?.mountedBusiness || '',
    networkZone: ip?.networkZone || '',
    mgmtLoginMethod: ip?.mgmtLoginMethod || '',
    id: ip?.id
  })
  Object.assign(configForm, {
    ...emptyConfig(),
    deviceId,
    cpu: config?.cpu || '',
    memory: config?.memory || '',
    disk: config?.disk || '',
    raid: config?.raid || '',
    gpu: config?.gpu || '',
    fiberCard: config?.fiberCard || '',
    nic: config?.nic || '',
    powerSupply: config?.powerSupply || '',
    os: config?.os || '',
    dbVersion: config?.dbVersion || '',
    firmware: config?.firmware || '',
    bios: config?.bios || '',
    remark: config?.remark || '',
    id: config?.id
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
      Object.assign(form, emptyDevice(), device, {
        isFixedAsset: device.isFixedAsset ?? 0
      })
      await loadRelatedData(device.id)
    } else {
      currentDeviceId.value = undefined
      resetForm()
    }
  },
  { immediate: true }
)

function hasIpData() {
  return !!(
    ipForm.businessIp ||
    ipForm.managementIp ||
    ipForm.mask ||
    ipForm.gateway ||
    ipForm.mountedBusiness ||
    ipForm.networkZone ||
    ipForm.mgmtLoginMethod
  )
}

function hasConfigData() {
  return !!(
    configForm.cpu ||
    configForm.memory ||
    configForm.disk ||
    configForm.raid ||
    configForm.gpu ||
    configForm.fiberCard ||
    configForm.nic ||
    configForm.powerSupply ||
    configForm.os ||
    configForm.dbVersion ||
    configForm.firmware ||
    configForm.bios ||
    configForm.remark
  )
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

async function saveConfig(deviceId: number) {
  if (!hasConfigData() && !configForm.id) {
    return
  }
  await saveDeviceConfigApi({ ...configForm, deviceId })
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
    await saveConfig(deviceId)
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
    width="920px"
    destroy-on-close
    class="device-form-dialog"
  >
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="basic">
        <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
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
              <el-form-item label="是否固定资产">
                <el-switch
                  v-model="form.isFixedAsset"
                  :active-value="1"
                  :inactive-value="0"
                  inline-prompt
                  active-text="是"
                  inactive-text="否"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备状态" prop="statusCode">
                <StatusSelect v-model="form.statusCode" style="width: 100%" />
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
              <el-form-item label="批准文号">
                <el-input v-model="form.approvalNo" placeholder="批准文号" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="管理部门">
                <el-tree-select
                  v-model="form.departmentId"
                  :data="departmentOptions"
                  check-strictly
                  clearable
                  filterable
                  placeholder="选择管理部门"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="使用部门">
                <el-tree-select
                  v-model="form.useDepartmentId"
                  :data="departmentOptions"
                  check-strictly
                  clearable
                  filterable
                  placeholder="选择使用部门"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="责任人">
                <el-select
                  v-model="form.managerUserId"
                  clearable
                  filterable
                  placeholder="选择责任人"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in users"
                    :key="item.id"
                    :label="item.realName ? `${item.realName}（${item.username}）` : item.username"
                    :value="item.id!"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="使用人">
                <el-input v-model="form.useUserName" placeholder="使用人姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备原值">
                <el-input-number
                  v-model="form.originalValue"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  placeholder="原值"
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
              <el-form-item label="出厂日期">
                <el-date-picker
                  v-model="form.manufactureDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="出厂日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="上架日期">
                <el-date-picker
                  v-model="form.onlineDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="上架日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="到保日期">
                <el-date-picker
                  v-model="form.warrantyEnd"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="到保日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="报废日期">
                <el-date-picker
                  v-model="form.scrapDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="报废日期"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所在机房">
                <el-input v-model="form.location" placeholder="所在机房" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="机柜U位">
                <el-input v-model="form.cabinet" placeholder="机柜U位，如 A01-U12" />
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

      <el-tab-pane label="配置信息" name="config">
        <el-form :model="configForm" label-width="110px">
          <el-divider content-position="left">设备配置</el-divider>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="CPU">
                <el-input v-model="configForm.cpu" placeholder="CPU" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="内存">
                <el-input v-model="configForm.memory" placeholder="内存" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="硬盘">
                <el-input v-model="configForm.disk" placeholder="硬盘" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="Raid">
                <el-input v-model="configForm.raid" placeholder="Raid" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="GPU">
                <el-input v-model="configForm.gpu" placeholder="GPU" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="光纤卡">
                <el-input v-model="configForm.fiberCard" placeholder="光纤卡" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="网卡">
                <el-input v-model="configForm.nic" placeholder="网卡" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电源">
                <el-input v-model="configForm.powerSupply" placeholder="电源" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-divider content-position="left">设备基本信息</el-divider>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="系统">
                <el-input v-model="configForm.os" placeholder="操作系统" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="数据库版本">
                <el-input v-model="configForm.dbVersion" placeholder="数据库版本" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="固件版本">
                <el-input v-model="configForm.firmware" placeholder="固件版本" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="BIOS">
                <el-input v-model="configForm.bios" placeholder="BIOS版本" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注">
                <el-input v-model="configForm.remark" type="textarea" :rows="2" placeholder="配置备注" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="网络信息" name="network">
        <el-form ref="ipFormRef" :model="ipForm" :rules="ipRules" label-width="130px">
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
            <el-col :span="12">
              <el-form-item label="挂载业务">
                <el-input v-model="ipForm.mountedBusiness" placeholder="挂载业务" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属网络">
                <el-input v-model="ipForm.networkZone" placeholder="所属网络" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="管理地址登录方式">
                <el-select
                  v-model="ipForm.mgmtLoginMethod"
                  clearable
                  filterable
                  allow-create
                  default-first-option
                  placeholder="如 SSH / HTTPS / IPMI"
                  style="width: 100%"
                >
                  <el-option v-for="item in MGMT_LOGIN_METHOD_OPTIONS" :key="item" :label="item" :value="item" />
                </el-select>
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

      <el-tab-pane label="所属项目" name="project">
        <el-form label-width="110px">
          <el-form-item label="所属项目">
            <el-select
              v-model="projectIds"
              multiple
              clearable
              filterable
              placeholder="选择所属项目"
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
