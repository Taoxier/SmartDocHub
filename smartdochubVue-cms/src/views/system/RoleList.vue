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
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" style="width: 200px"></el-input>
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="searchForm.roleCode" placeholder="请输入角色编码" style="width: 150px"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="roles" style="width: 100%" border>
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="角色ID" width="80"></el-table-column>
        <el-table-column prop="roleName" label="角色名称" width="150"></el-table-column>
        <el-table-column prop="roleCode" label="角色编码" width="150"></el-table-column>
        <el-table-column prop="description" label="角色描述" min-width="200"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editRole(scope.row.id)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteRole(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 搜索表单
const searchForm = reactive({
  roleName: '',
  roleCode: ''
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 角色列表
const roles = ref([
  {
    id: 1,
    roleName: '超级管理员',
    roleCode: 'ROOT',
    description: '拥有系统所有权限',
    createTime: '2026-04-06 20:27:32'
  },
  {
    id: 2,
    roleName: '管理员',
    roleCode: 'ADMIN',
    description: '拥有系统管理权限',
    createTime: '2026-04-06 20:27:32'
  },
  {
    id: 3,
    roleName: '普通用户',
    roleCode: 'USER',
    description: '普通用户权限',
    createTime: '2026-04-06 20:27:32'
  },
  {
    id: 4,
    roleName: '游客',
    roleCode: 'GUEST',
    description: '游客权限',
    createTime: '2026-04-06 20:27:32'
  }
])

// 搜索
function search() {
  console.log('搜索条件:', searchForm)
  // 这里可以添加搜索API调用
}

// 重置
function reset() {
  searchForm.roleName = ''
  searchForm.roleCode = ''
}

// 添加角色
function addRole() {
  router.push('/admin/role-edit')
}

// 编辑角色
function editRole(id) {
  router.push(`/admin/role-edit/${id}`)
}

// 删除角色
function deleteRole(id) {
  ElMessageBox.confirm('确定要删除这个角色吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    console.log('删除角色:', id)
    ElMessage.success('删除成功')
    // 这里可以添加删除角色的API调用
  }).catch(() => {
    // 取消删除
  })
}

// 分页处理
function handleSizeChange(size) {
  pagination.pageSize = size
  console.log('每页条数:', size)
  // 这里可以添加分页API调用
}

function handleCurrentChange(current) {
  pagination.currentPage = current
  console.log('当前页码:', current)
  // 这里可以添加分页API调用
}
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