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
  background: var(--em-sidebar-bg);
  color: var(--em-sidebar-text);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  height: 64px;
  padding: 0 18px;
  border-bottom: 1px solid rgb(255 255 255 / 8%);
}

.brand-mark {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 8px;
  background: var(--em-primary);
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.02em;
}

.brand strong {
  color: var(--em-sidebar-text);
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.side-menu {
  flex: 1;
  border-right: 0;
  background: transparent;
  --el-menu-bg-color: transparent;
  --el-menu-hover-bg-color: #243041;
  --el-menu-text-color: var(--em-sidebar-muted);
  --el-menu-active-color: #fff;
}

.side-menu :deep(.el-menu-item),
.side-menu :deep(.el-sub-menu__title) {
  color: var(--em-sidebar-muted);
}

.side-menu :deep(.el-menu-item:hover),
.side-menu :deep(.el-sub-menu__title:hover) {
  color: var(--em-sidebar-text);
  background: #243041;
}

.side-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: var(--em-sidebar-active);
  font-weight: 500;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: var(--em-surface);
  border-bottom: 1px solid var(--em-border);
}

.header-title {
  font-size: 18px;
  font-weight: 650;
  color: var(--em-text);
}

.header-subtitle {
  margin-top: 3px;
  color: var(--em-text-secondary);
  font-size: 12px;
}

.scope-tag {
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--el-color-primary-light-9);
  color: var(--em-primary);
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
  background: var(--em-page-bg);
}
</style>
