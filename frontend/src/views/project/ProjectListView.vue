<script setup lang="ts">
// 项目列表页：提供项目基础信息维护及设备项目关联操作。
import { onMounted, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import AppPagination from '@/components/common/AppPagination.vue'
import { useCrudPage } from '@/composables/useCrudPage'
import { projectApi } from '@/api/project'
import { buildDepartmentTreeOptions, findDepartmentName } from '@/api/system'
import { optionsApi } from '@/api/options'
import type { Project } from '@/types/device'
import type { SysDepartment } from '@/types/system'

const formRef = ref<FormInstance>()
const departments = ref<SysDepartment[]>([])

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
} = useCrudPage<Project>({
  entityName: '项目',
  defaultForm: () => ({
    projectName: '',
    projectCode: '',
    departmentId: undefined,
    remark: ''
  }),
  fetchPage: params => projectApi.page(params),
  createItem: data => projectApi.create(data),
  updateItem: data => projectApi.update(data),
  deleteItem: id => projectApi.remove(id)
})

const formRules: FormRules = {
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  projectCode: [{ required: true, message: '请输入项目编码', trigger: 'blur' }]
}

onMounted(async () => {
  departments.value = await optionsApi.departments()
  await loadData()
})
</script>

<template>
  <el-card shadow="never" class="page-card" data-testid="project-page">
    <template #header>
      <div class="card-header">
        <span>项目管理</span>
        <el-button
          data-testid="project-create-btn"
          v-permission="'project:add'"
          type="primary"
          @click="openCreate"
        >
          新增项目
        </el-button>
      </div>
    </template>

    <el-form :inline="true" class="search-form" data-testid="project-search-form" @submit.prevent="handleSearch">
      <el-form-item label="关键词">
        <el-input
          v-model="query.keyword"
          data-testid="project-keyword-input"
          placeholder="项目名称 / 编码"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-button data-testid="project-search-btn" type="primary" @click="handleSearch">查询</el-button>
        <el-button data-testid="project-reset-btn" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" stripe border data-testid="project-table">
      <el-table-column prop="projectCode" label="项目编码" min-width="140" />
      <el-table-column prop="projectName" label="项目名称" min-width="180" />
      <el-table-column label="所属部门" min-width="140">
        <template #default="{ row }">
          {{ findDepartmentName(departments, row.departmentId) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="创建时间" min-width="170" />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button
            :data-testid="`project-edit-${row.id}`"
            v-permission="'project:edit'"
            link
            type="primary"
            @click="openEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            :data-testid="`project-delete-${row.id}`"
            v-permission="'project:delete'"
            link
            type="danger"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
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
    :title="isEdit ? '编辑项目' : '新增项目'"
    width="520px"
    destroy-on-close
    data-testid="project-dialog"
  >
    <el-form ref="formRef" :model="form" :rules="formRules" label-width="96px" data-testid="project-form">
      <el-form-item label="项目编码" prop="projectCode">
        <el-input
          v-model="form.projectCode"
          data-testid="project-code-input"
          :disabled="isEdit"
          placeholder="如 PRJ-2026-001"
        />
      </el-form-item>
      <el-form-item label="项目名称" prop="projectName">
        <el-input v-model="form.projectName" data-testid="project-name-input" placeholder="项目名称" />
      </el-form-item>
      <el-form-item label="所属部门">
        <el-tree-select
          v-model="form.departmentId"
          data-testid="project-dept-select"
          :data="buildDepartmentTreeOptions(departments)"
          check-strictly
          clearable
          filterable
          placeholder="请选择部门"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="备注">
        <el-input
          v-model="form.remark"
          data-testid="project-remark-input"
          type="textarea"
          :rows="3"
          placeholder="备注"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="project-cancel-btn" @click="dialogVisible = false">取消</el-button>
      <el-button
        data-testid="project-save-btn"
        type="primary"
        :loading="saving"
        @click="formRef?.validate().then(submitForm)"
      >
        保存
      </el-button>
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
