<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import AppPagination from '@/components/common/AppPagination.vue'
import { useCrudPage } from '@/composables/useCrudPage'
import { deviceBrandApi } from '@/api/system'
import type { SysDict } from '@/types/system'

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
} = useCrudPage<SysDict>({
  entityName: '品牌',
  defaultForm: () => ({
    dictCode: '',
    dictName: '',
    sort: 0,
    status: 1
  }),
  fetchPage: params => deviceBrandApi.page(params),
  createItem: data => deviceBrandApi.create(data),
  updateItem: data => deviceBrandApi.update(data),
  deleteItem: id => deviceBrandApi.remove(id)
})

const formRules: FormRules = {
  dictCode: [{ required: true, message: '请输入品牌编码', trigger: 'blur' }],
  dictName: [{ required: true, message: '请输入品牌名称', trigger: 'blur' }]
}

onMounted(loadData)
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <span>设备品牌</span>
        <el-button v-permission="'system:brand:add'" type="primary" @click="openCreate">新增品牌</el-button>
      </div>
    </template>

    <el-form :inline="true" class="search-form" @submit.prevent="handleSearch">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="品牌名称 / 编码" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" stripe border>
      <el-table-column prop="dictCode" label="品牌编码" min-width="140" />
      <el-table-column prop="dictName" label="品牌名称" min-width="160" />
      <el-table-column prop="sort" label="排序" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:brand:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'system:brand:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑品牌' : '新增品牌'" width="480px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="88px">
      <el-form-item label="品牌编码" prop="dictCode">
        <el-input v-model="form.dictCode" :disabled="isEdit" placeholder="如 DELL" />
      </el-form-item>
      <el-form-item label="品牌名称" prop="dictName">
        <el-input v-model="form.dictName" placeholder="品牌名称" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="form.sort" :min="0" :max="9999" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">禁用</el-radio>
        </el-radio-group>
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
