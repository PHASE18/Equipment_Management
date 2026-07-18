<script setup lang="ts">
// 状态下拉框：将后端状态编码转换为统一的可选项展示。
import { computed } from 'vue'
import { DEVICE_STATUS_OPTIONS } from '@/types/device'

const model = defineModel<string | undefined>()

const props = withDefaults(
  defineProps<{
    clearable?: boolean
    placeholder?: string
    disabled?: boolean
    allowedValues?: string[]
  }>(),
  {
    clearable: true,
    placeholder: '设备状态',
    disabled: false
  }
)

const options = computed(() => {
  if (!props.allowedValues?.length) {
    return DEVICE_STATUS_OPTIONS
  }
  return DEVICE_STATUS_OPTIONS.filter(item => props.allowedValues!.includes(item.value))
})
</script>

<template>
  <el-select
    v-model="model"
    :clearable="clearable"
    :placeholder="placeholder"
    :disabled="disabled"
    style="width: 160px"
  >
    <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value">
      <el-tag :type="item.type" size="small">{{ item.label }}</el-tag>
    </el-option>
  </el-select>
</template>
