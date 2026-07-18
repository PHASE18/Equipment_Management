<script setup lang="ts">
// 侧边栏菜单：渲染认证状态中的树形菜单并处理路由跳转。
import type { MenuItem } from '@/types/auth'

defineOptions({ name: 'SidebarMenu' })

defineProps<{
  menus: MenuItem[]
}>()
</script>

<template>
  <template v-for="menu in menus" :key="menu.id">
    <el-sub-menu v-if="menu.children?.length" :index="String(menu.id)">
      <template #title>
        <span>{{ menu.title }}</span>
      </template>
      <SidebarMenu :menus="menu.children" />
    </el-sub-menu>
    <el-menu-item v-else :index="menu.path">
      <span>{{ menu.title }}</span>
    </el-menu-item>
  </template>
</template>
