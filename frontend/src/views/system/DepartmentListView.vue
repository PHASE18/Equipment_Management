<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import AppPagination from '@/components/common/AppPagination.vue'
import { useCrudPage } from '@/composables/useCrudPage'
import { buildDepartmentTreeOptions, departmentApi, findDepartmentName } from '@/api/system'
import type { SysDepartment } from '@/types/system'

const formRef = ref<FormInstance>()
const departments = ref<SysDepartment[]>([])

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
  submitForm,
  handleDelete
} = useCrudPage<SysDepartment>({
  entityName: '部门',
  defaultForm: () => ({
    departmentName: '',
    parentId: 0,
    leader: '',
    phone: '',
    remark: ''
  }),
  fetchPage: params => departmentApi.page(params),
  createItem: data => departmentApi.create(data),
  updateItem: data => departmentApi.update(data),
  deleteItem: id => departmentApi.remove(id)
})

const formRules: FormRules = {
  departmentName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

async function loadDepartments() {
  departments.value = await departmentApi.tree()
}

onMounted(async () => {
  await Promise.all([loadDepartments(), loadData()])
})
</script>

<template>
  <el-card shadow="never" class="page-card">
    <template #header>
      <div class="card-header">
        <span>部门管理</span>
        <el-button v-permission="'system:dept:add'" type="primary" @click="openCreate">新增部门</el-button>
      </div>
    </template>

    <el-form :inline="true" class="search-form" @submit.prevent="handleSearch">
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="部门名称" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" stripe border>
      <el-table-column prop="departmentName" label="部门名称" min-width="180" />
      <el-table-column label="上级部门" min-width="160">
        <template #default="{ row }">
          {{ row.parentId ? findDepartmentName(departments, row.parentId) : '顶级部门' }}
        </template>
      </el-table-column>
      <el-table-column prop="leader" label="负责人" min-width="120" />
      <el-table-column prop="phone" label="联系电话" min-width="130" />
      <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:dept:edit'" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button v-permission="'system:dept:delete'" link type="danger" @click="handleDelete(row)">删除</el-button>
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

  <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="480px" destroy-on-close>
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="88px">
      <el-form-item label="部门名称" prop="departmentName">
        <el-input v-model="form.departmentName" placeholder="部门名称" />
      </el-form-item>
      <el-form-item label="上级部门">
        <el-tree-select
          v-model="form.parentId"
          :data="departmentOptions"
          check-strictly
          clearable
          filterable
          placeholder="不选则为顶级部门"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="负责人">
        <el-input v-model="form.leader" placeholder="负责人" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.phone" placeholder="联系电话" />
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
