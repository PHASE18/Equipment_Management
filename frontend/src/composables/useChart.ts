import { onBeforeUnmount, onMounted, shallowRef, watch, type Ref } from 'vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'

/** 管理 ECharts 实例的创建、响应式更新、尺寸调整和销毁。 */
export function useChart(option: Ref<EChartsOption | null>) {
  const chartRef = shallowRef<HTMLElement>()
  let chart: echarts.ECharts | null = null

  function render() {
    if (!chartRef.value || !option.value) {
      return
    }
    if (!chart) {
      chart = echarts.init(chartRef.value)
    }
    chart.setOption(option.value, true)
  }

  function resize() {
    chart?.resize()
  }

  onMounted(() => {
    render()
    window.addEventListener('resize', resize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resize)
    chart?.dispose()
    chart = null
  })

  watch(option, render, { deep: true })

  return { chartRef, render, resize }
}

/** 创建无数据时使用的空图表配置。 */
export function emptyChartOption(title: string): EChartsOption {
  return {
    title: {
      text: title,
      left: 'center',
      top: 'middle',
      textStyle: { color: '#9ca3af', fontSize: 14, fontWeight: 400 }
    }
  }
}

/** 创建环形/饼图配置。 */
export function pieOption(title: string, data: Array<{ name: string; value: number }>): EChartsOption {
  if (!data.length) {
    return emptyChartOption('暂无数据')
  }
  return {
    title: { text: title, left: 0, textStyle: { fontSize: 15, fontWeight: 600 } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '45%'],
        data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.2)'
          }
        }
      }
    ]
  }
}

export function barOption(
  title: string,
  categories: string[],
  values: number[],
  horizontal = false
): EChartsOption {
  if (!categories.length) {
    return emptyChartOption('暂无数据')
  }
  return {
    title: { text: title, left: 0, textStyle: { fontSize: 15, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 20, top: 48, bottom: horizontal ? 20 : 56 },
    xAxis: horizontal
      ? { type: 'value' }
      : { type: 'category', data: categories, axisLabel: { rotate: categories.length > 6 ? 30 : 0 } },
    yAxis: horizontal ? { type: 'category', data: categories } : { type: 'value' },
    series: [
      {
        type: 'bar',
        data: values,
        itemStyle: { borderRadius: horizontal ? [0, 6, 6, 0] : [6, 6, 0, 0] },
        barMaxWidth: 36
      }
    ]
  }
}

export function lineOption(title: string, categories: string[], values: number[]): EChartsOption {
  if (!categories.length) {
    return emptyChartOption('暂无数据')
  }
  return {
    title: { text: title, left: 0, textStyle: { fontSize: 15, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 20, top: 48, bottom: 40 },
    xAxis: { type: 'category', data: categories },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        type: 'line',
        smooth: true,
        data: values,
        areaStyle: { opacity: 0.12 }
      }
    ]
  }
}

export function dualBarOption(
  title: string,
  categories: string[],
  countValues: number[],
  amountValues: number[]
): EChartsOption {
  if (!categories.length) {
    return emptyChartOption('暂无数据')
  }
  return {
    title: { text: title, left: 0, textStyle: { fontSize: 15, fontWeight: 600 } },
    tooltip: { trigger: 'axis' },
    legend: { top: 28 },
    grid: { left: 56, right: 56, top: 72, bottom: 40 },
    xAxis: { type: 'category', data: categories },
    yAxis: [
      { type: 'value', name: '次数', minInterval: 1 },
      { type: 'value', name: '费用(元)' }
    ],
    series: [
      { name: '维修次数', type: 'bar', data: countValues, barMaxWidth: 28 },
      { name: '维修费用', type: 'line', yAxisIndex: 1, smooth: true, data: amountValues }
    ]
  }
}
