<script setup lang="ts">
// 故障类型统计面板：加载统计数据并以图表方式呈现故障分布。
import { onMounted, ref } from 'vue'
import { faultTypeStatsApi } from '@/api/maintenance'
import type { FaultTypeStat } from '@/types/maintenance'

const loading = ref(false)
const stats = ref<FaultTypeStat[]>([])

async function loadStats() {
  loading.value = true
  try {
    stats.value = await faultTypeStatsApi()
  } finally {
    loading.value = false
  }
}

onMounted(loadStats)

defineExpose({ reload: loadStats })
</script>

<template>
  <el-card shadow="never" class="stats-card" v-loading="loading">
    <template #header>
      <span>故障类型统计</span>
    </template>
    <el-table v-if="stats.length" :data="stats" size="small" stripe>
      <el-table-column prop="faultTypeName" label="故障类型" min-width="120" />
      <el-table-column prop="count" label="工单数" width="90" />
      <el-table-column label="占比" min-width="160">
        <template #default="{ row }">
          <el-progress
            :percentage="Math.round((row.count / stats[0].count) * 100)"
            :stroke-width="10"
            :show-text="false"
          />
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else description="暂无故障统计数据" />
  </el-card>
</template>

<style scoped>
.stats-card {
  border-radius: 12px;
}
</style>
