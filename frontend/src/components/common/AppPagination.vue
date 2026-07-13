<script setup lang="ts">
const pageNum = defineModel<number>('pageNum', { required: true })
const pageSize = defineModel<number>('pageSize', { required: true })

defineProps<{
  total: number
}>()

const emit = defineEmits<{
  change: []
}>()
</script>

<template>
  <div class="pagination-wrap">
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="emit('change')"
      @current-change="emit('change')"
    />
  </div>
</template>

<style scoped>
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  padding-top: 14px;
}
</style>
