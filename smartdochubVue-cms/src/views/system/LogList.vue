<template>
  <div class="log-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>日志管理</span>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="请输入操作人" style="width: 150px"></el-input>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="请选择操作类型" style="width: 150px">
            <el-option label="全部" value=""></el-option>
            <el-option label="登录" value="LOGIN"></el-option>
            <el-option label="上传" value="UPLOAD"></el-option>
            <el-option label="下载" value="DOWNLOAD"></el-option>
            <el-option label="删除" value="DELETE"></el-option>
            <el-option label="审核" value="AUDIT"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
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
      
      <el-table :data="logs" style="width: 100%" border>
        <el-table-column prop="id" label="日志ID" width="80"></el-table-column>
        <el-table-column prop="username" label="操作人" width="120"></el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="scope">
            <el-tag>{{ scope.row.operationType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationContent" label="操作内容" min-width="300"></el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="150"></el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180"></el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetails(scope.row.id)">详情</el-button>
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
  operationType: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 日志列表
const logs = ref([
  {
    id: 1,
    username: 'admin',
    operationType: 'LOGIN',
    operationContent: '用户登录',
    ipAddress: '192.168.1.100',
    createTime: '2026-04-08 10:00:00'
  },
  {
    id: 2,
    username: 'user1',
    operationType: 'UPLOAD',
    operationContent: '上传文档：毕设题目详解',
    ipAddress: '192.168.1.101',
    createTime: '2026-04-08 10:30:00'
  },
  {
    id: 3,
    username: 'admin',
    operationType: 'AUDIT',
    operationContent: '审核文档：毕设题目详解，状态：通过',
    ipAddress: '192.168.1.100',
    createTime: '2026-04-08 11:00:00'
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
  searchForm.operationType = ''
  searchForm.dateRange = []
}

// 查看详情
function viewDetails(id) {
  console.log('查看日志详情:', id)
  // 这里可以添加查看日志详情的逻辑
  ElMessage.info('查看详情功能正在开发中')
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
.log-list {
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