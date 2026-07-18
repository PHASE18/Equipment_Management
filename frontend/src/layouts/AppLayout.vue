<script setup lang="ts">
// 主布局：组合侧边栏、顶部用户区和路由内容区域。
import { useRoute, useRouter } from 'vue-router'
import SidebarMenu from '@/components/common/SidebarMenu.vue'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

async function handleLogout() {
  await authStore.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="app-shell">
    <el-aside class="app-aside" width="224px">
      <div class="brand">
        <div class="brand-mark">EM</div>
        <div>
          <strong>设备管理系统</strong>
          <span>Equipment</span>
        </div>
      </div>

      <el-menu class="side-menu" :default-active="route.path" router>
        <SidebarMenu :menus="authStore.menus" />
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="app-header">
        <div>
          <div class="header-title">{{ String(route.meta.title || '首页统计') }}</div>
          <div class="header-subtitle">
            {{ authStore.user?.department || '全局视图' }}
            <span v-if="authStore.dataScopeLabel" class="scope-tag">{{ authStore.dataScopeLabel }}</span>
          </div>
        </div>
        <div class="header-actions">
          <span class="user-name">{{ authStore.user?.name || authStore.user?.username }}</span>
          <el-button type="primary" plain @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.app-shell {
  height: 100%;
}

.app-aside {
  display: flex;
  flex-direction: column;
  background: #101827;
  color: #fff;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 64px;
  padding: 0 18px;
  border-bottom: 1px solid rgb(255 255 255 / 10%);
}

.brand-mark {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 8px;
  background: #2f7cf6;
  font-weight: 700;
}

.brand span {
  display: block;
  margin-top: 2px;
  color: #9ca3af;
  font-size: 12px;
}

.side-menu {
  flex: 1;
  border-right: 0;
  background: transparent;
}

.side-menu :deep(.el-menu-item),
.side-menu :deep(.el-sub-menu__title) {
  color: #cbd5e1;
}

.side-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: #1d4ed8;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.header-title {
  font-size: 18px;
  font-weight: 650;
}

.header-subtitle {
  margin-top: 3px;
  color: #6b7280;
  font-size: 12px;
}

.scope-tag {
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #eff6ff;
  color: #2563eb;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: #374151;
  font-size: 14px;
}

.app-main {
  overflow: auto;
  padding: 20px 24px;
  background: #f5f7fb;
}
</style>
