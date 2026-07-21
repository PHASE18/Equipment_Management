<script setup lang="ts">
// 登录页：校验账号密码，调用认证状态并跳转到原目标页面。
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, User } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

//普通对象转换成响应式变量
const loginForm = reactive({
  username: '',
  password: ''
})

//用来做"用户名/密码不能为空"这类前端校验
const loginRules: FormRules = { //Element Plus 提供的类型，约束这个变量必须符合"表单规则"的格式
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!loginFormRef.value) {
    return
  }

  await loginFormRef.value.validate() //调用 Element Plus 表单实例的 validate() 方法，触发之前定义的 loginRules 规则做整体校验
  loading.value = true
  try {
    await authStore.login(loginForm) //把 loginForm（包含 username、password）发送给后端登录接口
    ElMessage.success('登录成功')
    // 优先跳回原目标；否则进入首个有权限菜单，无菜单时进 403，避免硬编码 /dashboard 导致白屏
    const redirect =
      typeof route.query.redirect === 'string'
        ? route.query.redirect
        : authStore.menus[0]?.path || '/403'
    router.replace(redirect)
  } catch { 
    // 错误提示由 http 拦截器统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <template #header>
        <div class="login-header">
          <h2>设备管理系统</h2>
          <p>用户登录</p>
        </div>
      </template>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            clearable
            autofocus
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" native-type="submit" :loading="loading" class="login-button">
            登录
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <span>© 2026 设备管理系统</span>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  width: 420px;
  border-radius: 12px;
}

.login-header {
  text-align: center;
}

.login-header h2 {
  margin: 0;
  color: #409eff;
}

.login-header p {
  margin: 8px 0 0;
  color: #909399;
  font-size: 14px;
}

.login-button {
  width: 100%;
}

.login-footer {
  text-align: center;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
