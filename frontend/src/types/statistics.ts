export interface StatisticsQuery {
  departmentId?: number
  projectId?: number
  brandCode?: string
  deviceTypeCode?: string
  startDate?: string
  endDate?: string
}

export interface DashboardSummary {
  deviceTotal: number
  inUseCount: number
  maintainingCount: number
  stoppedCount: number
  scrappedCount: number
  warrantyExpiringCount: number
  monthNewDeviceCount: number
  monthMaintenanceCount: number
}

export interface ChartItem {
  code?: string
  name?: string
  value: number
  amount?: number
}

export interface DashboardData {
  summary: DashboardSummary
  statusChart: ChartItem[]
  brandChart: ChartItem[]
  typeChart: ChartItem[]
  departmentChart: ChartItem[]
  projectChart: ChartItem[]
  faultChart: ChartItem[]
  maintenanceTrendChart: ChartItem[]
  maintenanceCostChart: ChartItem[]
  warrantyChart: ChartItem[]
}
