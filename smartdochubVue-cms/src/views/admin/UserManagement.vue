<template>
  <div class="user-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <div>
            <el-button type="success" size="small" @click="handleExport">导出</el-button>
            <el-button type="warning" size="small" @click="handleImport">导入</el-button>
            <el-button type="primary" size="small" @click="handleAdd">新增用户</el-button>
            <el-button type="danger" size="small" @click="handleBatchDelete">批量删除</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" style="width: 150px"></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" style="width: 150px"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 100px">
            <el-option label="全部" value=""></el-option>
            <el-option label="启用" value="1"></el-option>
            <el-option label="禁用" value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="users" style="width: 100%" border v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="用户ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
        <el-table-column prop="mobile" label="手机号" width="120"></el-table-column>
        <el-table-column prop="sex" label="性别" width="70">
          <template #default="scope">
            <span>{{ scope.row.sex === 1 ? '男' : scope.row.sex === 2 ? '女' : '未知' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-switch v-model="scope.row.status" active-color="#13ce66" inactive-color="#ff4949" :active-value="1"
              :inactive-value="0" @change="handleStatusChange(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="editUser(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" @click="resetPassword(scope.row)">重置密码</el-button>
            <el-button size="small" type="danger" @click="deleteUser(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination :current-page="pagination.pageNum" @update:current-page="pagination.pageNum = $event"
          :page-size="pagination.pageSize" @update:page-size="pagination.pageSize = $event"
          :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total"
          @size-change="handleSizeChange" @current-change="handleCurrentChange"></el-pagination>
      </div>
    </el-card>

    <el-dialog v-model="showAddDialog" :title="addForm.id ? '编辑用户' : '新增用户'" width="550px">
      <el-form :model="addForm" :rules="addRules" ref="addFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="addForm.username" placeholder="请输入用户名" :disabled="!!addForm.id"></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="addForm.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="addForm.mobile" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="addForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="addForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="addForm.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleOptions" :key="role.value" :label="role.label" :value="role.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAdd" :loading="addLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showResetDialog" title="重置密码" width="400px">
      <el-form :model="resetForm" ref="resetFormRef" label-width="80px">
        <el-form-item label="用户">
          <span>{{ resetForm.username }}</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetForm.password" type="password" placeholder="请输入新密码" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResetDialog = false">取消</el-button>
        <el-button type="primary" @click="submitResetPassword" :loading="resetLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showImportDialog" title="导入用户" width="450px">
      <el-upload
        ref="importUploadRef"
        action="/api/v1/users/import"
        :headers="uploadHeaders"
        :on-success="handleImportSuccess"
        :on-error="handleImportError"
        :auto-upload="false"
        accept=".xlsx,.xls"
        :limit="1"
      >
        <template #trigger>
          <el-button type="primary">选择文件</el-button>
        </template>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 .xlsx / .xls 格式，
            <el-link type="primary" @click="downloadTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button type="primary" @click="submitImport">确认导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, saveUser, getUserForm, updateUser, updateUserStatus, resetUserPassword, deleteUsers, getRoleOptions } from '@/api/admin'
import { accessHeader } from '@/net'

const loading = ref(false)
const users = ref([])
const selectedIds = ref([])
const roleOptions = ref([])

const searchForm = reactive({
  username: '',
  nickname: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const showAddDialog = ref(false)
const addLoading = ref(false)
const addFormRef = ref()
const addForm = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  mobile: '',
  gender: 0,
  status: 1,
  roleIds: []
})

const addRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  roleIds: [{ required: true, message: '请选择角色', trigger: 'change', type: 'array' }]
}

const showResetDialog = ref(false)
const resetLoading = ref(false)
const resetFormRef = ref()
const resetForm = reactive({
  id: null,
  username: '',
  password: ''
})

const showImportDialog = ref(false)
const importUploadRef = ref()
const uploadHeaders = computed(() => accessHeader())

function loadUsers() {
  loading.value = true
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    username: searchForm.username,
    nickname: searchForm.nickname,
    status: searchForm.status
  }
  getUserPage(params, (data) => {
    users.value = data.records || data.list || []
    pagination.total = data.total || 0
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载用户列表失败')
    loading.value = false
  })
}

function loadRoleOptions() {
  getRoleOptions((data) => {
    roleOptions.value = data || []
  }, () => {})
}

function search() {
  pagination.pageNum = 1
  loadUsers()
}

function reset() {
  searchForm.username = ''
  searchForm.nickname = ''
  searchForm.status = ''
  pagination.pageNum = 1
  loadUsers()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function handleStatusChange(row) {
  updateUserStatus(row.id, row.status, () => {
    ElMessage.success('状态更新成功')
  }, (msg) => {
    ElMessage.error(msg || '状态更新失败')
    loadUsers()
  })
}

function handleAdd() {
  addForm.id = null
  addForm.username = ''
  addForm.nickname = ''
  addForm.email = ''
  addForm.mobile = ''
  addForm.gender = 0
  addForm.status = 1
  addForm.roleIds = []
  showAddDialog.value = true
}

function editUser(row) {
  addForm.id = row.id
  addForm.username = row.username || ''
  addForm.nickname = row.nickname || ''
  addForm.email = row.email || ''
  addForm.mobile = row.mobile || ''
  addForm.gender = row.sex || 0
  addForm.status = row.status !== undefined ? row.status : 1
  addForm.roleIds = []

  getUserForm(row.id, (data) => {
    if (data) {
      addForm.roleIds = data.roleIds || []
    }
    showAddDialog.value = true
  }, () => {
    showAddDialog.value = true
  })
}

function submitAdd() {
  addFormRef.value.validate((valid) => {
    if (!valid) return
    addLoading.value = true

    const formData = {
      id: addForm.id,
      username: addForm.username,
      nickname: addForm.nickname,
      email: addForm.email,
      mobile: addForm.mobile,
      gender: addForm.gender,
      status: addForm.status,
      roleIds: addForm.roleIds
    }

    if (addForm.id) {
      updateUser(addForm.id, formData, () => {
        ElMessage.success('更新成功')
        addLoading.value = false
        showAddDialog.value = false
        loadUsers()
      }, (msg) => {
        ElMessage.error(msg || '更新失败')
        addLoading.value = false
      })
    } else {
      saveUser(formData, () => {
        ElMessage.success('新增成功')
        addLoading.value = false
        showAddDialog.value = false
        loadUsers()
      }, (msg) => {
        ElMessage.error(msg || '新增失败')
        addLoading.value = false
      })
    }
  })
}

function resetPassword(row) {
  resetForm.id = row.id
  resetForm.username = row.username
  resetForm.password = ''
  showResetDialog.value = true
}

function submitResetPassword() {
  if (!resetForm.password || resetForm.password.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  resetLoading.value = true
  resetUserPassword(resetForm.id, resetForm.password, () => {
    ElMessage.success('密码重置成功')
    resetLoading.value = false
    showResetDialog.value = false
  }, (msg) => {
    ElMessage.error(msg || '密码重置失败')
    resetLoading.value = false
  })
}

function deleteUser(row) {
  ElMessageBox.confirm(`确定要删除用户「${row.username}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteUsers(row.id, () => {
      ElMessage.success('删除成功')
      loadUsers()
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => { })
}

function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的用户')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个用户吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteUsers(selectedIds.value.join(','), () => {
      ElMessage.success('删除成功')
      loadUsers()
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => { })
}

function handleExport() {
  window.location.href = '/api/v1/users/export'
}

function handleImport() {
  showImportDialog.value = true
}

function submitImport() {
  importUploadRef.value.submit()
}

function handleImportSuccess(response) {
  if (response.code === '00000') {
    ElMessage.success('导入成功')
    showImportDialog.value = false
    loadUsers()
  } else {
    ElMessage.error(response.msg || '导入失败')
  }
}

function handleImportError() {
  ElMessage.error('导入失败，请检查文件格式')
}

function downloadTemplate() {
  window.location.href = '/api/v1/users/template'
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadUsers()
}

function handleCurrentChange(current) {
  pagination.pageNum = current
  loadUsers()
}

onMounted(() => {
  loadUsers()
  loadRoleOptions()
})
</script>

<style scoped>
.user-management {
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

.search-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
