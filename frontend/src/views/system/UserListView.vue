<script setup lang="ts">
// 用户管理页：完成用户分页、角色分配、启停用和密码维护。
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AppPagination from '@/components/common/AppPagination.vue'
import { useCrudPage } from '@/composables/useCrudPage'
import {
  buildDepartmentTreeOptions,
  departmentApi,
  findDepartmentName,
  listUserRoleIdsApi,
  roleApi,
  syncUserRolesApi,
  userApi
} from '@/api/system'
import type { SysDepartment, SysRole, SysUser } from '@/types/system'

const formRef = ref<FormInstance>()
const departments = ref<SysDepartment[]>([])
const roles = ref<SysRole[]>([])
const roleIds = ref<number[]>([])

const departmentOptions = computed(() => buildDepartmentTreeOptions(departments.value))

const {
  loading,
  saving,
  dialogVisible,
  isEdit,
  tableData,
  total,
  form,
  query,
  loadData,
  handleSearch,
  resetQuery,
  openCreate,
  openEdit,
  handleDelete
} = useCrudPage<SysUser>({
  entityName: '用户',
  defaultForm: () => ({
    username: '',
    password: '',
    realName: '',
    departmentId: undefined,
    phone: '',
    email: '',
    status: 1
  }),
  fetchPage: params => userApi.list(params),
  deleteItem: id => userApi.remove(id)
})

const formRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const dialogRules = computed<FormRules>(() => ({
  ...formRules,
  password: isEdit.value
    ? []
    : [{ required: true, message: '请输入密码', trigger: 'blur' }]
}))

async function loadOptions() {
  const [deptList, rolePage] = await Promise.all([
    departmentApi.tree(),
    roleApi.page({ pageNum: 1, pageSize: 200 })
  ])
  departments.value = deptList
  roles.value = rolePage.records
}

async function handleOpenCreate() {
  roleIds.value = []
  openCreate()
}

async function handleOpenEdit(row: SysUser) {
  openEdit(row)
  if (row.id) {
    roleIds.value = await listUserRoleIdsApi(row.id)
  }
}

async function submitForm() {
  if (!formRef.value) {
    return
  }
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      const payload = { ...form }
      if (!payload.password) {
        delete payload.password
      }
      await userApi.update(payload)
      if (form.id) {
        await syncUserRolesApi(form.id, roleIds.value)
      }
      ElMessage.success('更新成功')
    } else {
      await userApi.create(form)
      const result = await userApi.list({ pageNum: 1, pageSize: 1, keyword: form.username })
      const userId = result.records[0]?.id
      if (userId && roleIds.value.length) {
        await syncUserRolesApi(userId, roleIds.value)
      }
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadData()
  } finally {
    saving.value = false
  }
}

async function handleResetPassword(row: SysUser) {
  if (!row.id) {
    return
  }
  await ElMessageBox.confirm(`确定将用户「${row.username}」密码重置为 123456 吗？`, '重置密码', {
    type: 'warning'
  })
  await userApi.resetPassword(row.id)
  ElMessage.success('密码已重置')
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadData()])
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <span>用户管理</span>
        <el-button v-permission="'system:user:add'" type="primary" @click="handleOpenCreate">新增用户</el-button>
      </div>
    </template>

    <el-form :inline="true" class="search-form" @submit.prevent="handleSearch">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="用户名 / 姓名" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" stripe border>
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="120" />
      <el-table-column label="部门" min-width="140">
        <template #default="{ row }">
          {{ findDepartmentName(departments, row.departmentId) }}
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:user:edit'" link type="primary" @click="handleOpenEdit(row)">编辑</el-button>
          <el-button v-permission="'system:user:edit'" link type="warning" @click="handleResetPassword(row)">
            重置密码
          </el-button>
          <el-button v-permission="'system:user:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <AppPagination
      v-model:page-num="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      @change="loadData"
    />
  </el-card>

  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    width="520px"
    destroy-on-close
  >
    <el-form ref="formRef" :model="form" :rules="dialogRules" label-width="88px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" :disabled="isEdit" placeholder="登录用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          show-password
          :placeholder="isEdit ? '留空则不修改' : '登录密码'"
        />
      </el-form-item>
      <el-form-item label="姓名" prop="realName">
        <el-input v-model="form.realName" placeholder="真实姓名" />
      </el-form-item>
      <el-form-item label="部门">
        <el-tree-select
          v-model="form.departmentId"
          :data="departmentOptions"
          check-strictly
          clearable
          filterable
          placeholder="选择部门"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="roleIds" multiple clearable placeholder="选择角色" style="width: 100%">
          <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id!" />
        </el-select>
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="手机号" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="邮箱" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.page-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.search-form {
  margin-bottom: 12px;
}
</style>
