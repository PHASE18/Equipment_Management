<script setup lang="ts">
import type { DeviceStatusOption } from '@/types/device'

const model = defineModel<string | undefined>()

withDefaults(
  defineProps<{
    clearable?: boolean
    placeholder?: string
    options?: DeviceStatusOption[]
  }>(),
  {
    clearable: true,
    placeholder: '设备状态',
    options: () => [
      { label: '采购中', value: 'PURCHASING', type: 'info' },
      { label: '库存', value: 'IN_STOCK', type: 'primary' },
      { label: '待上架', value: 'PENDING_ONLINE', type: 'warning' },
      { label: '在用', value: 'IN_USE', type: 'success' },
      { label: '维修中', value: 'MAINTAINING', type: 'warning' },
      { label: '停用', value: 'STOPPED', type: 'info' },
      { label: '报废', value: 'SCRAPPED', type: 'danger' }
    ]
  }
)
</script>

<template>
  <el-select v-model="model" :clearable="clearable" :placeholder="placeholder" style="width: 160px">
    <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value">
      <el-tag :type="item.type" size="small">{{ item.label }}</el-tag>
    </el-option>
  </el-select>
</template>
