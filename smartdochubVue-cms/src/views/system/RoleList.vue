<template>
  <div class="role-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" size="small" @click="addRole">新增角色</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keywords" placeholder="角色名称/编码" style="width: 200px" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="roles" style="width: 100%" border v-loading="loading">
        <el-table-column prop="id" label="角色ID" width="80"></el-table-column>
        <el-table-column prop="name" label="角色名称" width="150"></el-table-column>
        <el-table-column prop="code" label="角色编码" width="150"></el-table-column>
        <el-table-column prop="sort" label="排序" width="80"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              active-text="启用"
              inactive-text="禁用"
              @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editRole(scope.row.id)">编辑</el-button>
            <el-button size="small" type="primary" @click="assignPermissions(scope.row.id)">分配权限</el-button>
            <el-button size="small" type="danger" @click="deleteRole(scope.row.id, scope.row.name)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <el-dialog v-model="showPermDialog" title="分配菜单权限" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedMenuIds"
        :props="{ label: 'name', children: 'children' }"
      ></el-tree>
      <template #footer>
        <el-button @click="showPermDialog = false">取消</el-button>
        <el-button type="primary" @click="submitPermissions" :loading="permLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRolePage, deleteRoles, updateRoleStatus, getRoleMenuIds, assignMenusToRole, getMenuList } from '@/api/admin'

const router = useRouter()
const loading = ref(false)
const roles = ref([])

const searchForm = reactive({
  keywords: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const showPermDialog = ref(false)
const permLoading = ref(false)
const menuTree = ref([])
const checkedMenuIds = ref([])
const currentRoleId = ref(null)
const menuTreeRef = ref()

function loadRoles() {
  loading.value = true
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    keywords: searchForm.keywords
  }
  getRolePage(params, (data) => {
    roles.value = data.records || data.list || []
    pagination.total = data.total || 0
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载角色列表失败')
    loading.value = false
  })
}

function search() {
  pagination.pageNum = 1
  loadRoles()
}

function reset() {
  searchForm.keywords = ''
  pagination.pageNum = 1
  loadRoles()
}

function addRole() {
  router.push('/admin/role-edit')
}

function editRole(id) {
  router.push(`/admin/role-edit/${id}`)
}

function deleteRole(id, name) {
  ElMessageBox.confirm(`确定要删除角色「${name}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteRoles(id, () => {
      ElMessage.success('删除成功')
      loadRoles()
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => {})
}

function handleStatusChange(row) {
  updateRoleStatus(row.id, row.status, () => {
    ElMessage.success('状态更新成功')
  }, (msg) => {
    ElMessage.error(msg || '状态更新失败')
    row.status = row.status === 1 ? 0 : 1
  })
}

function assignPermissions(roleId) {
  currentRoleId.value = roleId
  checkedMenuIds.value = []
  showPermDialog.value = true

  getMenuList((data) => {
    menuTree.value = data || []
  }, (msg) => {
    ElMessage.error(msg || '加载菜单树失败')
  })

  getRoleMenuIds(roleId, (data) => {
    checkedMenuIds.value = data || []
  }, (msg) => {
    ElMessage.error(msg || '加载角色权限失败')
  })
}

function submitPermissions() {
  permLoading.value = true
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  const allKeys = [...checkedKeys, ...halfCheckedKeys]

  assignMenusToRole(currentRoleId.value, allKeys, () => {
    ElMessage.success('权限分配成功')
    permLoading.value = false
    showPermDialog.value = false
  }, (msg) => {
    ElMessage.error(msg || '权限分配失败')
    permLoading.value = false
  })
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadRoles()
}

function handleCurrentChange(current) {
  pagination.currentPage = current
  loadRoles()
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
.role-list {
  padding: 20px;
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
