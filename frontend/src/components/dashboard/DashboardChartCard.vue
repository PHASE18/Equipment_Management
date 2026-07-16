<script setup lang="ts">
import { computed } from 'vue'
import type { EChartsOption } from 'echarts'
import { useChart } from '@/composables/useChart'

const props = defineProps<{
  option: EChartsOption | null
  height?: string
}>()

const optionRef = computed(() => props.option)
const { chartRef } = useChart(optionRef)
</script>

<template>
  <div ref="chartRef" class="chart-box" :style="{ height: height || '320px' }" />
</template>

<style scoped>
.chart-box {
  width: 100%;
}
</style>
