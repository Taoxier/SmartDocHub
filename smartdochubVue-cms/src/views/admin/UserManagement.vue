<template>
  <div class="user-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" size="small">批量操作</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" style="width: 150px"></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="searchForm.nickname" placeholder="请输入昵称" style="width: 150px"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" style="width: 120px">
            <el-option label="全部" value=""></el-option>
            <el-option label="超级管理员" value="ROOT"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
            <el-option label="普通用户" value="USER"></el-option>
            <el-option label="游客" value="GUEST"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 100px">
            <el-option label="全部" value=""></el-option>
            <el-option label="启用" value="1"></el-option>
            <el-option label="禁用" value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="注册时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 200px"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="users" style="width: 100%" border>
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="用户ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
        <el-table-column prop="mobile" label="手机号" width="120"></el-table-column>
        <el-table-column prop="role" label="角色" width="100">
          <template #default="scope">
            <el-tag>{{ scope.row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              active-color="#13ce66"
              inactive-color="#ff4949"
              @change="handleStatusChange(scope.row)"
            ></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180"></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="editUser(scope.row.id)">编辑</el-button>
            <el-button size="small" type="primary" @click="assignRole(scope.row.id)">分配角色</el-button>
            <el-button size="small" type="danger" @click="resetPassword(scope.row.id)">重置密码</el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'

// 搜索表单
const searchForm = reactive({
  username: '',
  nickname: '',
  role: '',
  status: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 用户列表
const users = ref([
  {
    id: 1,
    username: 'admin',
    nickname: '超级管理员',
    email: 'admin@example.com',
    mobile: '13800138000',
    role: 'ROOT',
    status: 1,
    createTime: '2026-04-06 20:27:32'
  },
  {
    id: 2,
    username: 'user1',
    nickname: '普通用户',
    email: 'user1@example.com',
    mobile: '13800138001',
    role: 'USER',
    status: 1,
    createTime: '2026-04-07 10:00:00'
  }
])

// 搜索
function search() {
  console.log('搜索条件:', searchForm)
  // 这里可以添加搜索API调用
}

// 重置
function reset() {
  searchForm.username = ''
  searchForm.nickname = ''
  searchForm.role = ''
  searchForm.status = ''
  searchForm.dateRange = []
}

// 处理状态变更
function handleStatusChange(user) {
  console.log('用户状态变更:', user.id, user.status)
  // 这里可以添加状态变更API调用
  ElMessage.success('状态更新成功')
}

// 编辑用户
function editUser(id) {
  console.log('编辑用户:', id)
  // 这里可以添加编辑用户的逻辑
}

// 分配角色
function assignRole(id) {
  console.log('分配角色:', id)
  // 这里可以添加分配角色的逻辑
}

// 重置密码
function resetPassword(id) {
  ElMessageBox.prompt('请输入新密码', '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '新密码',
    inputType: 'password'
  }).then(({ value }) => {
    console.log('重置密码:', id, value)
    ElMessage.success('密码重置成功')
    // 这里可以添加重置密码的API调用
  }).catch(() => {
    // 取消重置
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
.user-management {
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