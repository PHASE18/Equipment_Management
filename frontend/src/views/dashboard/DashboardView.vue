<script setup lang="ts">
// 仪表盘：加载汇总指标和图表数据，响应筛选条件并刷新可视化内容。
import { computed, onMounted, reactive, ref } from 'vue'
import DashboardChartCard from '@/components/dashboard/DashboardChartCard.vue'
import { getDashboardApi } from '@/api/statistics'
import { pageProjectsApi } from '@/api/project'
import { optionsApi } from '@/api/options'
import { buildDepartmentTreeOptions } from '@/api/system'
import type { DashboardData, StatisticsQuery } from '@/types/statistics'
import type { SysDepartment, SysDict } from '@/types/system'
import type { Project } from '@/types/device'
import { getDeviceStatusLabel } from '@/types/device'
import {
  barOption,
  dualBarOption,
  lineOption,
  pieOption
} from '@/composables/useChart'
import type { EChartsOption } from 'echarts'

const loading = ref(false)
const dashboard = ref<DashboardData | null>(null)
const departments = ref<SysDepartment[]>([])
const projects = ref<Project[]>([])
const brands = ref<SysDict[]>([])
const deviceTypes = ref<SysDict[]>([])

const query = reactive<StatisticsQuery>({
  departmentId: undefined,
  projectId: undefined,
  brandCode: '',
  deviceTypeCode: '',
  startDate: '',
  endDate: ''
})

// 计算属性 departmentOptions，自动把原始部门列表 departments 转换成下拉 / 树形选择器可用的选项数组
const departmentOptions = computed(() => buildDepartmentTreeOptions(departments.value))

const summaryCards = computed(() => {
  const s = dashboard.value?.summary
  if (!s) {
    return []
  }
  return [
    { label: '设备总数', value: s.deviceTotal, color: '#2563eb' },
    { label: '在用设备', value: s.inUseCount, color: '#16a34a' },
    { label: '维修设备', value: s.maintainingCount, color: '#d97706' },
    { label: '停用设备', value: s.stoppedCount, color: '#6b7280' },
    { label: '报废设备', value: s.scrappedCount, color: '#dc2626' },
    { label: '即将到保', value: s.warrantyExpiringCount, color: '#7c3aed' },
    { label: '本月新增', value: s.monthNewDeviceCount, color: '#0891b2' },
    { label: '本月维修', value: s.monthMaintenanceCount, color: '#ea580c' }
  ]
})

function mapStatusName(code?: string, name?: string) {
  return name && name !== code ? name : getDeviceStatusLabel(code)
}

const statusOption = computed<EChartsOption | null>(() => {
  const data = dashboard.value?.statusChart.map(item => ({
    name: mapStatusName(item.code, item.name),
    value: item.value
  }))
  return pieOption('设备状态分布', data || [])
})

const brandOption = computed(() =>
  barOption(
    '品牌分布统计',
    dashboard.value?.brandChart.map(i => i.name || i.code || '-') || [],
    dashboard.value?.brandChart.map(i => i.value) || []
  )
)

const typeOption = computed(() =>
  barOption(
    '设备类型统计',
    dashboard.value?.typeChart.map(i => i.name || i.code || '-') || [],
    dashboard.value?.typeChart.map(i => i.value) || []
  )
)

const departmentOption = computed(() =>
  barOption(
    '部门设备统计',
    dashboard.value?.departmentChart.map(i => i.name || '-') || [],
    dashboard.value?.departmentChart.map(i => i.value) || [],
    true
  )
)

const projectOption = computed(() =>
  barOption(
    '项目设备统计',
    dashboard.value?.projectChart.map(i => i.name || '-') || [],
    dashboard.value?.projectChart.map(i => i.value) || []
  )
)

const faultOption = computed(() =>
  pieOption(
    '故障类型统计',
    dashboard.value?.faultChart.map(i => ({ name: i.name || i.code || '-', value: i.value })) || []
  )
)

const maintenanceTrendOption = computed(() =>
  lineOption(
    '维修次数趋势',
    dashboard.value?.maintenanceTrendChart.map(i => i.name || '-') || [],
    dashboard.value?.maintenanceTrendChart.map(i => i.value) || []
  )
)

const maintenanceCostOption = computed(() =>
  dualBarOption(
    '维修费用统计',
    dashboard.value?.maintenanceCostChart.map(i => i.name || '-') || [],
    dashboard.value?.maintenanceCostChart.map(i => i.value) || [],
    dashboard.value?.maintenanceCostChart.map(i => Number(i.amount || 0)) || []
  )
)

const warrantyOption = computed(() =>
  barOption(
    '到保设备统计',
    dashboard.value?.warrantyChart.map(i => i.name || '-') || [],
    dashboard.value?.warrantyChart.map(i => i.value) || []
  )
)

const supplierOption = computed(() =>
  barOption(
    '供应商统计',
    dashboard.value?.supplierChart?.map(i => i.name || '-') || [],
    dashboard.value?.supplierChart?.map(i => i.value) || []
  )
)

const maintenanceCompanyOption = computed(() =>
  barOption(
    '维保单位统计',
    dashboard.value?.maintenanceCompanyChart?.map(i => i.name || '-') || [],
    dashboard.value?.maintenanceCompanyChart?.map(i => i.value) || []
  )
)

const modelOption = computed(() =>
  barOption(
    '设备型号排行',
    dashboard.value?.modelChart?.map(i => i.name || '-') || [],
    dashboard.value?.modelChart?.map(i => i.value) || [],
    true
  )
)

const scrapOption = computed(() =>
  lineOption(
    '报废统计',
    dashboard.value?.scrapChart?.map(i => i.name || '-') || [],
    dashboard.value?.scrapChart?.map(i => i.value) || []
  )
)

async function loadFilterOptions() {
  const [deptList, projectPage, brandList, typeList] = await Promise.all([
    optionsApi.departments(),
    pageProjectsApi({ pageNum: 1, pageSize: 200 }),
    optionsApi.brands(),
    optionsApi.deviceTypes()
  ])
  departments.value = deptList
  projects.value = projectPage.records
  brands.value = brandList
  deviceTypes.value = typeList
}

async function loadDashboard() {
  loading.value = true
  try {
    dashboard.value = await getDashboardApi({
      departmentId: query.departmentId,
      projectId: query.projectId,
      brandCode: query.brandCode || undefined,
      deviceTypeCode: query.deviceTypeCode || undefined,
      startDate: query.startDate || undefined,
      endDate: query.endDate || undefined
    })
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  loadDashboard()
}

function resetQuery() {
  query.departmentId = undefined
  query.projectId = undefined
  query.brandCode = ''
  query.deviceTypeCode = ''
  query.startDate = ''
  query.endDate = ''
  loadDashboard()
}

onMounted(async () => {
  await Promise.all([loadFilterOptions(), loadDashboard()])
})
</script>

<template>
  <div v-loading="loading" class="dashboard-page" data-testid="statistics-page">
    <el-card shadow="never" class="filter-card" data-testid="statistics-filter">
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="部门">
          <el-tree-select
            v-model="query.departmentId"
            data-testid="statistics-dept-filter"
            :data="departmentOptions"
            check-strictly
            clearable
            filterable
            placeholder="全部"
            style="width: 160px"
          />
        </el-form-item>
        <el-form-item label="项目">
          <el-select
            v-model="query.projectId"
            data-testid="statistics-project-filter"
            clearable
            filterable
            placeholder="全部"
            style="width: 160px"
          >
            <el-option
              v-for="item in projects"
              :key="item.id"
              :label="item.projectName"
              :value="item.id!"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select
            v-model="query.brandCode"
            data-testid="statistics-brand-filter"
            clearable
            filterable
            placeholder="全部"
            style="width: 140px"
          >
            <el-option v-for="item in brands" :key="item.dictCode" :label="item.dictName" :value="item.dictCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select
            v-model="query.deviceTypeCode"
            data-testid="statistics-type-filter"
            clearable
            filterable
            placeholder="全部"
            style="width: 140px"
          >
            <el-option v-for="item in deviceTypes" :key="item.dictCode" :label="item.dictName" :value="item.dictCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="query.startDate"
            data-testid="statistics-start-date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="开始日期"
            style="width: 140px"
          />
          <span class="date-sep">至</span>
          <el-date-picker
            v-model="query.endDate"
            data-testid="statistics-end-date"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="结束日期"
            style="width: 140px"
          />
        </el-form-item>
        <el-form-item>
          <el-button data-testid="statistics-search-btn" type="primary" @click="handleSearch">查询</el-button>
          <el-button data-testid="statistics-reset-btn" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="summary-grid" data-testid="statistics-summary">
      <el-card
        v-for="item in summaryCards"
        :key="item.label"
        :data-testid="`statistics-card-${item.label}`"
        shadow="hover"
        class="summary-card"
      >
        <div class="summary-value" :style="{ color: item.color }">{{ item.value }}</div>
        <div class="summary-label">{{ item.label }}</div>
      </el-card>
    </div>

    <el-row :gutter="16" class="chart-row" data-testid="statistics-charts">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card" data-testid="statistics-chart-status">
          <DashboardChartCard :option="statusOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card" data-testid="statistics-chart-fault">
          <DashboardChartCard :option="faultOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <DashboardChartCard :option="brandOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <DashboardChartCard :option="typeOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <DashboardChartCard :option="departmentOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <DashboardChartCard :option="projectOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <DashboardChartCard :option="maintenanceTrendOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <DashboardChartCard :option="maintenanceCostOption" />
        </el-card>
      </el-col>
      <el-col :span="24">
        <el-card shadow="never" class="chart-card" data-testid="statistics-chart-warranty">
          <DashboardChartCard :option="warrantyOption" height="280px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card" data-testid="statistics-chart-supplier">
          <DashboardChartCard :option="supplierOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card" data-testid="statistics-chart-maintenance-company">
          <DashboardChartCard :option="maintenanceCompanyOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card" data-testid="statistics-chart-model">
          <DashboardChartCard :option="modelOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card" data-testid="statistics-chart-scrap">
          <DashboardChartCard :option="scrapOption" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card,
.chart-card {
  border-radius: 12px;
}

.date-sep {
  margin: 0 8px;
  color: #9ca3af;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  border-radius: 12px;
  text-align: center;
}

.summary-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
}

.summary-label {
  margin-top: 6px;
  color: #6b7280;
  font-size: 13px;
}

.chart-row .el-col {
  margin-bottom: 16px;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
