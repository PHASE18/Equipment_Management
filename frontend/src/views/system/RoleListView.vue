<script setup lang="ts">
// 角色管理页：维护角色基础信息并配置角色权限集合。
import { onMounted, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import AppPagination from '@/components/common/AppPagination.vue'
import { useCrudPage } from '@/composables/useCrudPage'
import { roleApi } from '@/api/system'
import type { SysRole } from '@/types/system'

const formRef = ref<FormInstance>()

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
  submitForm,
  handleDelete
} = useCrudPage<SysRole>({
  entityName: '角色',
  defaultForm: () => ({
    roleName: '',
    roleCode: '',
    remark: ''
  }),
  fetchPage: params => roleApi.page(params),
  createItem: data => roleApi.create(data),
  updateItem: data => roleApi.update(data),
  deleteItem: id => roleApi.remove(id)
})

const formRules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <span>角色管理</span>
        <el-button v-permission="'system:role:add'" type="primary" @click="openCreate">新增角色</el-button>
      </div>
    </template>

    <el-form :inline="true" class="search-form" @submit.prevent="handleSearch">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="角色名称 / 编码" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" stripe border>
      <el-table-column prop="roleName" label="角色名称" min-width="160" />
      <el-table-column prop="roleCode" label="角色编码" min-width="160" />
      <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:role:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'system:role:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="480px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="88px">
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="form.roleName" placeholder="角色名称" />
      </el-form-item>
      <el-form-item label="角色编码" prop="roleCode">
        <el-input v-model="form.roleCode" :disabled="isEdit" placeholder="如 ADMIN" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="formRef?.validate().then(submitForm)">保存</el-button>
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
