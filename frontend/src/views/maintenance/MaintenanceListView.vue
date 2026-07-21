<script setup lang="ts">
// 维修列表页：维护维修记录查询、编辑、删除和故障统计展示。
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppPagination from '@/components/common/AppPagination.vue'
import FaultTypeStatsPanel from '@/components/maintenance/FaultTypeStatsPanel.vue'
import MaintenanceFormDialog from '@/components/maintenance/MaintenanceFormDialog.vue'
import {
  completeMaintenanceApi,
  deleteMaintenanceApi,
  listFaultTypesApi,
  pageMaintenanceApi
} from '@/api/maintenance'
import type { MaintenanceQuery, MaintenanceRecord } from '@/types/maintenance'

const loading = ref(false)
const tableData = ref<MaintenanceRecord[]>([])
const total = ref(0)
const faultTypes = ref<Array<{ dictCode: string; dictName: string }>>([])
const formVisible = ref(false)
const editingRecord = ref<MaintenanceRecord | null>(null)
const statsPanelRef = ref<InstanceType<typeof FaultTypeStatsPanel>>()

const query = ref<MaintenanceQuery>({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  faultType: '',
  isResolved: undefined
})

async function loadFaultTypes() {
  faultTypes.value = await listFaultTypesApi()
}

async function loadData() {
  loading.value = true
  try {
    const result = await pageMaintenanceApi({
      pageNum: query.value.pageNum,
      pageSize: query.value.pageSize,
      keyword: query.value.keyword || undefined,
      faultType: query.value.faultType || undefined,
      isResolved: query.value.isResolved
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
  query.value.faultType = ''
  query.value.isResolved = undefined
  query.value.pageNum = 1
  loadData()
}

function openCreate() {
  editingRecord.value = null
  formVisible.value = true
}

function openEdit(row: MaintenanceRecord) {
  editingRecord.value = { ...row }
  formVisible.value = true
}

async function handleDelete(row: MaintenanceRecord) {
  await ElMessageBox.confirm(`确定删除该维修记录吗？`, '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  await deleteMaintenanceApi(row.id!)
  ElMessage.success('删除成功')
  await refreshAll()
}

async function handleComplete(row: MaintenanceRecord) {
  await ElMessageBox.confirm('确认维修完成？设备将自动恢复为「在用」状态。', '完成维修', { type: 'info',    confirmButtonText: '确定',
  cancelButtonText: '取消'})
  await completeMaintenanceApi(row.id!)
  ElMessage.success('维修已完成')
  await refreshAll()
}

async function refreshAll() {
  await Promise.all([loadData(), statsPanelRef.value?.reload()])
}

onMounted(async () => {
  await Promise.all([loadFaultTypes(), loadData()])
})
</script>

<template>
  <div class="maintenance-page">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card shadow="never" class="page-card">
          <template #header>
            <div class="card-header">
              <span>维修管理</span>
              <el-button v-permission="'maintenance:add'" type="primary" @click="openCreate">新增工单</el-button>
            </div>
          </template>

          <el-form :inline="true" class="search-form" @submit.prevent="handleSearch">
            <el-form-item label="关键词">
              <el-input
                v-model="query.keyword"
                placeholder="人员 / 单位 / 故障现象"
                clearable
                style="width: 200px"
                @keyup.enter="handleSearch"
              />
            </el-form-item>
            <el-form-item label="故障类型">
              <el-select v-model="query.faultType" clearable placeholder="全部" style="width: 140px">
                <el-option
                  v-for="item in faultTypes"
                  :key="item.dictCode"
                  :label="item.dictName"
                  :value="item.dictCode"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="query.isResolved" clearable placeholder="全部" style="width: 120px">
                <el-option label="未解决" :value="0" />
                <el-option label="已解决" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="resetQuery">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table v-loading="loading" :data="tableData" stripe border>
            <el-table-column label="设备" min-width="160" fixed="left">
              <template #default="{ row }">
                <div>{{ row.deviceName }}</div>
                <div class="sub-text">{{ row.deviceNo }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="maintenanceDate" label="维修日期" width="120" />
            <el-table-column prop="maintenancePerson" label="维修人员" width="100" />
            <el-table-column prop="faultTypeName" label="故障类型" width="110" />
            <el-table-column prop="faultDescription" label="故障现象" min-width="180" show-overflow-tooltip />
            <el-table-column label="费用" width="100">
              <template #default="{ row }">
                {{ row.maintenanceCost ?? '-' }}
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.isResolved === 1 ? 'success' : 'warning'" size="small">
                  {{ row.isResolved === 1 ? '已解决' : '处理中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recoverDate" label="恢复日期" width="120" />
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button v-permission="'maintenance:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
                <el-button
                  v-if="row.isResolved !== 1"
                  v-permission="'maintenance:edit'"
                  link
                  type="success"
                  @click="handleComplete(row)"
                >
                  完成
                </el-button>
                <el-button v-permission="'maintenance:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
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
      </el-col>

      <el-col :span="8">
        <FaultTypeStatsPanel ref="statsPanelRef" />
      </el-col>
    </el-row>

    <MaintenanceFormDialog v-model="formVisible" :record="editingRecord" @success="refreshAll" />
  </div>
</template>

<style scoped>
.maintenance-page {
  width: 100%;
}

.page-card {
  border-radius: 12px;
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

.sub-text {
  color: #9ca3af;
  font-size: 12px;
}
</style>
