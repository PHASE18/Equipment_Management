<script setup lang="ts">
import { computed } from 'vue'
import { getDeviceStatusLabel, getDeviceStatusType } from '@/types/device'
import type { DeviceStatusLogItem } from '@/types/device'

const props = defineProps<{
  history: DeviceStatusLogItem[]
}>()

const sortedHistory = computed(() =>
  [...props.history].sort((a, b) => {
    const timeA = a.changeTime ? new Date(a.changeTime).getTime() : 0
    const timeB = b.changeTime ? new Date(b.changeTime).getTime() : 0
    return timeB - timeA
  })
)
</script>

<template>
  <el-timeline v-if="sortedHistory.length">
    <el-timeline-item
      v-for="item in sortedHistory"
      :key="item.id"
      :timestamp="item.changeTime"
      placement="top"
    >
      <div class="history-item">
        <div class="history-status">
          <el-tag v-if="item.oldStatusCode" :type="getDeviceStatusType(item.oldStatusCode)" size="small">
            {{ item.oldStatusName || getDeviceStatusLabel(item.oldStatusCode) }}
          </el-tag>
          <span v-if="item.oldStatusCode" class="arrow">→</span>
          <el-tag :type="getDeviceStatusType(item.newStatusCode)" size="small">
            {{ item.newStatusName || getDeviceStatusLabel(item.newStatusCode) }}
          </el-tag>
        </div>
        <div v-if="item.changeReason" class="history-reason">原因：{{ item.changeReason }}</div>
        <div v-if="item.remark" class="history-remark">备注：{{ item.remark }}</div>
        <div class="history-meta">
          操作人：{{ item.operatorName || '系统' }}
        </div>
      </div>
    </el-timeline-item>
  </el-timeline>
  <el-empty v-else description="暂无流转记录" />
</template>

<style scoped>
.history-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-status {
  display: flex;
  align-items: center;
  gap: 8px;
}

.arrow {
  color: #9ca3af;
}

.history-reason,
.history-remark,
.history-meta {
  color: #6b7280;
  font-size: 13px;
}
</style>
