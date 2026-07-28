<script setup lang="ts">
// 设备列表页：组合分页查询、设备表单、附件入口和生命周期操作。
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppPagination from '@/components/common/AppPagination.vue'
import StatusSelect from '@/components/common/StatusSelect.vue'
import DeviceFormDialog from '@/components/device/DeviceFormDialog.vue'
import DeviceMigrateDialog from '@/components/device/DeviceMigrateDialog.vue'
import DeviceMigrateHistoryDialog from '@/components/device/DeviceMigrateHistoryDialog.vue'
import LifecycleHistoryDialog from '@/components/device/LifecycleHistoryDialog.vue'
import LifecycleTransitionDialog from '@/components/device/LifecycleTransitionDialog.vue'
import {
  deleteDeviceApi,
  exportDevicesApi,
  pageDevicesApi
} from '@/api/device'
import { optionsApi } from '@/api/options'
import { buildDepartmentTreeOptions, findDepartmentName } from '@/api/system'
import type { SysDepartment } from '@/types/system'
import type { Device, DeviceStatusChangeResult } from '@/types/device'
import { getDeviceDisplayStatusLabel, getDeviceDisplayStatusType } from '@/types/device'

const loading = ref(false)
const exporting = ref(false)
const tableData = ref<Device[]>([])
const total = ref(0)
const departments = ref<SysDepartment[]>([])
const formVisible = ref(false)
const lifecycleVisible = ref(false)
const historyVisible = ref(false)
const migrateVisible = ref(false)
const migrateHistoryVisible = ref(false)
const editingDevice = ref<Device | null>(null)
const lifecycleDevice = ref<Device | null>(null)
const historyDevice = ref<Device | null>(null)
const migrateDevice = ref<Device | null>(null)
const migrateHistoryDevice = ref<Device | null>(null)

const query = ref({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  status: '',
  departmentId: undefined as number | undefined
})

const departmentOptions = computed(() => buildDepartmentTreeOptions(departments.value))

async function loadDepartments() {
  departments.value = await optionsApi.departments()
}

async function loadData() {
  loading.value = true
  try {
    const result = await pageDevicesApi({
      pageNum: query.value.pageNum,
      pageSize: query.value.pageSize,
      keyword: query.value.keyword || undefined,
      status: query.value.status || undefined,
      departmentId: query.value.departmentId
    })
    tableData.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.value.pageNum = 1
  loadData()
}

function resetQuery() {
  query.value.keyword = ''
  query.value.status = ''
  query.value.departmentId = undefined
  query.value.pageNum = 1
  loadData()
}

function openCreate() {
  editingDevice.value = null
  formVisible.value = true
}

function openEdit(row: Device) {
  editingDevice.value = { ...row }
  formVisible.value = true //弹窗编辑设备
}

function openLifecycle(row: Device) {
  lifecycleDevice.value = row
  lifecycleVisible.value = true
}

function openHistory(row: Device) {
  historyDevice.value = row
  historyVisible.value = true
}

function openMigrate(row: Device) {
  migrateDevice.value = row
  migrateVisible.value = true
}

function openMigrateHistory(row: Device) {
  migrateHistoryDevice.value = row
  migrateHistoryVisible.value = true
}

async function handleDelete(row: Device) {
  await ElMessageBox.confirm(`确定删除设备「${row.deviceName}」吗？`, '提示', { type: 'warning' })
  await deleteDeviceApi(row.id!)
  ElMessage.success('删除成功')
  await loadData()
}

async function handleLifecycleSuccess(result: DeviceStatusChangeResult) {
  const row = tableData.value.find(item => item.id === result.deviceId)
  if (row) {
    row.statusCode = result.newStatusCode
    if (result.maintainingFlag !== undefined) {
      row.maintainingFlag = result.maintainingFlag
    }
  }
  await loadData()
}

async function handleMigrateSuccess() {
  await loadData()
}

async function handleExport() {
  exporting.value = true
  try {
    await exportDevicesApi({
      keyword: query.value.keyword || undefined,
      status: query.value.status || undefined,
      departmentId: query.value.departmentId
    })
    ElMessage.success('导出成功')
  } catch (error) {
    const message = error instanceof Error ? error.message : '导出失败'
    ElMessage.error(message)
  } finally {
    exporting.value = false
  }
}

async function handleFormSuccess() {
  await loadData()
}

onMounted(async () => {
  await Promise.all([loadDepartments(), loadData()])
})
</script>

<template>
  <el-card shadow="never" class="page-card" data-testid="device-page">
    <template #header>
      <div class="card-header">
        <span>设备档案</span>
        <div class="header-actions">
          <el-button v-permission="'device:export'" :loading="exporting" @click="handleExport">导出</el-button>
          <el-button v-permission="'device:add'" type="primary" @click="openCreate">新增设备</el-button>
        </div>
      </div>
    </template>

    <el-form :inline="true" class="search-form" @submit.prevent="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          placeholder="编号 / 名称 / SN"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="状态">
        <StatusSelect v-model="query.status" />
      </el-form-item>
      <el-form-item label="管理部门">
        <el-tree-select
          v-model="query.departmentId"
          :data="departmentOptions"
          check-strictly
          clearable
          filterable
          placeholder="全部管理部门"
          style="width: 180px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" stripe border data-testid="device-table">
      <el-table-column prop="deviceNo" label="设备编号" min-width="130" fixed="left" />
      <el-table-column prop="deviceName" label="设备名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="sn" label="SN号" min-width="130" show-overflow-tooltip />
      <el-table-column prop="brandCode" label="品牌" width="100" />
      <el-table-column prop="deviceTypeCode" label="类型" width="100" />
      <el-table-column label="管理部门" min-width="120">
        <template #default="{ row }">
          {{ findDepartmentName(departments, row.departmentId) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getDeviceDisplayStatusType(row.statusCode, row.maintainingFlag)" size="small">
            {{ getDeviceDisplayStatusLabel(row.statusCode, row.maintainingFlag) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="location" label="所在机房" min-width="140" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="360" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'device:view'" link type="primary" @click="openHistory(row)">流转史</el-button>
          <el-button
            v-permission="'device:view'"
            link
            type="primary"
            :data-testid="`device-migrate-history-${row.id}`"
            @click="openMigrateHistory(row)"
          >
            迁移史
          </el-button>
          <el-button v-permission="'device:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            v-permission="'device:edit'"
            link
            type="success"
            :data-testid="`device-migrate-${row.id}`"
            @click="openMigrate(row)"
          >
            迁移
          </el-button>
          <el-button v-permission="'device:edit'" link type="warning" @click="openLifecycle(row)">流转</el-button>
          <el-button v-permission="'device:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <AppPagination
      v-model:page-num="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      @change="loadData"
    />
  </el-card>

  <DeviceFormDialog v-model="formVisible" :device="editingDevice" @success="handleFormSuccess" />

  <LifecycleTransitionDialog
    v-model="lifecycleVisible"
    :device="lifecycleDevice || undefined"
    @success="handleLifecycleSuccess"
  />

  <LifecycleHistoryDialog v-model="historyVisible" :device="historyDevice" />

  <DeviceMigrateDialog
    v-model="migrateVisible"
    :device="migrateDevice"
    @success="handleMigrateSuccess"
  />

  <DeviceMigrateHistoryDialog v-model="migrateHistoryVisible" :device="migrateHistoryDevice" />
</template>

<style scoped>
.page-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.search-form {
  margin-bottom: 12px;
}
</style>
