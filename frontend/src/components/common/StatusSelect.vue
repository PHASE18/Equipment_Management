<script setup lang="ts">
// 状态下拉框：将后端状态编码转换为统一的可选项展示。
import { computed } from 'vue'
import { DEVICE_MAIN_STATUS_OPTIONS, DEVICE_STATUS_OPTIONS } from '@/types/device'

const model = defineModel<string | undefined>()

const props = withDefaults(
  defineProps<{
    clearable?: boolean
    placeholder?: string
    disabled?: boolean
    allowedValues?: string[]
    /** filter=含维修中筛选；main=表单主状态（不含维修中伪码） */
    mode?: 'filter' | 'main'
  }>(),
  {
    clearable: true,
    placeholder: '设备状态',
    disabled: false,
    mode: 'filter'
  }
)

const baseOptions = computed(() =>
  props.mode === 'main' ? DEVICE_MAIN_STATUS_OPTIONS : DEVICE_STATUS_OPTIONS
)

const options = computed(() => {
  if (!props.allowedValues?.length) {
    return baseOptions.value
  }
  return baseOptions.value.filter(item => props.allowedValues!.includes(item.value))
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
