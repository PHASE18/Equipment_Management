<template>
  <div class="forbidden-page">
    <el-result icon="warning" title="403" sub-title="您没有权限访问该页面">
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goLogin">重新登录</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

function goHome() {
  const firstMenu = authStore.menus[0]
  router.replace(firstMenu?.path || '/login')
}

async function goLogin() {
  await authStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.forbidden-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  background: #f5f7fb;
}
</style>
