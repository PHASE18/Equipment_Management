<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import AppPagination from '@/components/common/AppPagination.vue'
import { logApi, type LogQuery } from '@/api/log'
import type { DeviceStatusLog, LoginLog, OperationLog } from '@/types/log'

type LogTab = 'login' | 'operation' | 'status'

const activeTab = ref<LogTab>('login')
const loading = ref(false)
const total = ref(0)
const loginRows = ref<LoginLog[]>([])
const operationRows = ref<OperationLog[]>([])
const statusRows = ref<DeviceStatusLog[]>([])

const query = reactive<LogQuery>({
  pageNum: 1,
  pageSize: 20,
  username: '',
  operationType: '',
  tableName: '',
  deviceId: undefined
})

async function loadData() {
  loading.value = true
  try {
    if (activeTab.value === 'login') {
      const result = await logApi.login({
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        username: query.username || undefined
      })
      loginRows.value = result.records
      total.value = result.total
      return
    }
    if (activeTab.value === 'operation') {
      const result = await logApi.operation({
        pageNum: query.pageNum,
        pageSize: query.pageSize,
        operationType: query.operationType || undefined,
        tableName: query.tableName || undefined
      })
      operationRows.value = result.records
      total.value = result.total
      return
    }
    const result = await logApi.status({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      deviceId: query.deviceId
    })
    statusRows.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  loadData()
}

function resetQuery() {
  query.username = ''
  query.operationType = ''
  query.tableName = ''
  query.deviceId = undefined
  query.pageNum = 1
  loadData()
}

watch(activeTab, () => {
  query.pageNum = 1
  loadData()
})

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card" data-testid="audit-log-page">
    <template #header>
      <div class="card-header">
        <span>日志审计</span>
        <el-tag type="info">日志永久留存，禁止删除</el-tag>
      </div>
    </template>

    <el-tabs v-model="activeTab" data-testid="audit-log-tabs">
      <el-tab-pane label="登录日志" name="login" data-testid="tab-login" />
      <el-tab-pane label="操作日志" name="operation" data-testid="tab-operation" />
      <el-tab-pane label="生命周期日志" name="status" data-testid="tab-status" />
    </el-tabs>

    <el-form :inline="true" class="search-form" data-testid="audit-search-form" @submit.prevent="handleSearch">
      <template v-if="activeTab === 'login'">
        <el-form-item label="用户名">
          <el-input
            v-model="query.username"
            data-testid="audit-username-input"
            placeholder="登录用户名"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
      </template>
      <template v-else-if="activeTab === 'operation'">
        <el-form-item label="操作类型">
          <el-input
            v-model="query.operationType"
            data-testid="audit-operation-type-input"
            placeholder="如 INSERT/UPDATE"
            clearable
          />
        </el-form-item>
        <el-form-item label="表名">
          <el-input
            v-model="query.tableName"
            data-testid="audit-table-name-input"
            placeholder="如 project"
            clearable
          />
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item label="设备ID">
          <el-input-number
            v-model="query.deviceId"
            data-testid="audit-device-id-input"
            :min="1"
            controls-position="right"
          />
        </el-form-item>
      </template>
      <el-form-item>
        <el-button data-testid="audit-search-btn" type="primary" @click="handleSearch">查询</el-button>
        <el-button data-testid="audit-reset-btn" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table
      v-if="activeTab === 'login'"
      v-loading="loading"
      :data="loginRows"
      stripe
      border
      data-testid="audit-login-table"
    >
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="loginIp" label="IP地址" min-width="130" />
      <el-table-column prop="browser" label="浏览器" min-width="180" show-overflow-tooltip />
      <el-table-column prop="loginTime" label="登录时间" min-width="170" />
      <el-table-column label="结果" width="90">
        <template #default="{ row }">
          <el-tag :type="row.result === 1 ? 'success' : 'danger'">
            {{ row.result === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-table
      v-else-if="activeTab === 'operation'"
      v-loading="loading"
      :data="operationRows"
      stripe
      border
      data-testid="audit-operation-table"
    >
      <el-table-column prop="operationType" label="操作类型" width="110" />
      <el-table-column prop="tableName" label="操作对象" min-width="120" />
      <el-table-column prop="businessId" label="业务ID" width="100" />
      <el-table-column prop="operatorId" label="操作人ID" width="100" />
      <el-table-column prop="ip" label="IP地址" min-width="120" />
      <el-table-column prop="browser" label="浏览器" min-width="160" show-overflow-tooltip />
      <el-table-column prop="beforeJson" label="修改前" min-width="160" show-overflow-tooltip />
      <el-table-column prop="afterJson" label="修改后" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="操作时间" min-width="170" />
    </el-table>

    <el-table
      v-else
      v-loading="loading"
      :data="statusRows"
      stripe
      border
      data-testid="audit-status-table"
    >
      <el-table-column prop="deviceId" label="设备ID" width="100" />
      <el-table-column prop="oldStatusCode" label="原状态" min-width="120" />
      <el-table-column prop="newStatusCode" label="新状态" min-width="120" />
      <el-table-column prop="changeReason" label="变更原因" min-width="160" show-overflow-tooltip />
      <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
      <el-table-column prop="operatorId" label="操作人ID" width="100" />
      <el-table-column prop="changeTime" label="变更时间" min-width="170" />
    </el-table>

    <AppPagination
      v-model:page-num="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      @change="loadData"
    />
  </el-card>
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

.search-form {
  margin-bottom: 12px;
}
</style>
