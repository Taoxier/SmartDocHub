<template>
  <div class="category-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>分类管理</span>
          <div>
            <el-button type="danger" size="small" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
            <el-button type="primary" size="small" @click="handleAdd">添加分类</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" min-width="150">
          <template #default="scope">
            <span v-if="!scope.row.editing">{{ scope.row.name }}</span>
            <el-input v-else v-model="scope.row.editName" size="small" placeholder="分类名称"></el-input>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="scope">
            <span v-if="!scope.row.editing">{{ scope.row.description || '-' }}</span>
            <el-input v-else v-model="scope.row.editDescription" size="small" placeholder="分类描述"></el-input>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="100">
          <template #default="scope">
            <span v-if="!scope.row.editing">{{ scope.row.sort || 0 }}</span>
            <el-input-number v-else v-model="scope.row.editSort" :min="0" :max="9999" size="small"></el-input-number>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <template v-if="!scope.row.editing">
              <el-button size="small" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </template>
            <template v-else>
              <el-button size="small" type="success" @click="handleSave(scope.row)">保存</el-button>
              <el-button size="small" @click="handleCancel(scope.row)">取消</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAddDialog" title="添加分类" width="500px">
      <el-form :model="addForm" :rules="formRules" ref="addFormRef" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入分类名称"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="addForm.description" type="textarea" :rows="3" placeholder="请输入分类描述"></el-input>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="addForm.sort" :min="0" :max="9999"></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="addForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAdd" :loading="addLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryPage, createCategory, updateCategory, deleteCategory, batchDeleteCategories } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])
const showAddDialog = ref(false)
const addLoading = ref(false)
const addFormRef = ref()

const addForm = reactive({
  name: '',
  description: '',
  sort: 0,
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

function loadCategories() {
  loading.value = true
  getCategoryPage({ pageNum: 1, pageSize: 100 }, (data) => {
    tableData.value = (data.records || data.list || []).map(item => ({
      ...item,
      editing: false,
      editName: '',
      editDescription: '',
      editSort: 0
    }))
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载分类列表失败')
    loading.value = false
  })
}

function handleAdd() {
  addForm.name = ''
  addForm.description = ''
  addForm.sort = 0
  addForm.status = 1
  showAddDialog.value = true
}

function submitAdd() {
  addFormRef.value.validate((valid) => {
    if (!valid) return
    addLoading.value = true
    createCategory(addForm, () => {
      ElMessage.success('添加成功')
      addLoading.value = false
      showAddDialog.value = false
      loadCategories()
    }, (msg) => {
      ElMessage.error(msg || '添加失败')
      addLoading.value = false
    })
  })
}

function handleEdit(row) {
  row.editName = row.name
  row.editDescription = row.description || ''
  row.editSort = row.sort || 0
  row.editing = true
}

function handleSave(row) {
  if (!row.editName) {
    ElMessage.warning('分类名称不能为空')
    return
  }
  const data = {
    id: row.id,
    name: row.editName,
    description: row.editDescription,
    sort: row.editSort,
    status: row.status
  }
  updateCategory(data, () => {
    ElMessage.success('更新成功')
    loadCategories()
  }, (msg) => {
    ElMessage.error(msg || '更新失败')
  })
}

function handleCancel(row) {
  row.editing = false
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除分类「${row.name}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteCategory(row.id, () => {
      ElMessage.success('删除成功')
      loadCategories()
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => { })
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的分类')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个分类吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    batchDeleteCategories(selectedIds.value, () => {
      ElMessage.success('删除成功')
      loadCategories()
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => { })
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-management {
  padding: 10px;
}

.el-card {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15) !important;
  margin-bottom: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>