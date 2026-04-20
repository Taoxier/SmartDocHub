<template>
  <div class="comment-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>评论管理</span>
          <el-button type="primary" size="small">批量操作</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文档标题">
          <el-input v-model="searchForm.docTitle" placeholder="请输入文档标题" style="width: 200px"></el-input>
        </el-form-item>
        <el-form-item label="评论用户">
          <el-input v-model="searchForm.username" placeholder="请输入评论用户" style="width: 150px"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 120px">
            <el-option label="全部" value=""></el-option>
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已通过" value="APPROVED"></el-option>
            <el-option label="已拒绝" value="REJECTED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
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
      
      <el-table :data="comments" style="width: 100%" border>
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="评论ID" width="80"></el-table-column>
        <el-table-column prop="docTitle" label="文档标题" min-width="200">
          <template #default="scope">
            <el-link type="primary" @click="viewDocument(scope.row.docId)">{{ scope.row.docTitle }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="评论用户" width="120"></el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="300">
          <template #default="scope">
            <div class="comment-content">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="80">
          <template #default="scope">
            <div class="rating">
              <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900"></el-rate>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="180"></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button size="small" v-if="scope.row.status === 'PENDING'" type="primary" @click="approveComment(scope.row.id)">通过</el-button>
            <el-button size="small" v-if="scope.row.status === 'PENDING'" type="danger" @click="rejectComment(scope.row.id)">拒绝</el-button>
            <el-button size="small" type="danger" @click="deleteComment(scope.row.id)">删除</el-button>
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
  docTitle: '',
  username: '',
  status: '',
  dateRange: []
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 评论列表
const comments = ref([
  {
    id: 1,
    docId: 1,
    docTitle: '毕设题目详解',
    username: 'user1',
    content: '这是一篇非常详细的文档，对我帮助很大！',
    rating: 5,
    status: 'APPROVED',
    createTime: '2026-04-08 10:00:00'
  },
  {
    id: 2,
    docId: 1,
    docTitle: '毕设题目详解',
    username: 'user2',
    content: '文档内容很全面，感谢分享！',
    rating: 4,
    status: 'PENDING',
    createTime: '2026-04-08 11:00:00'
  }
])

// 获取状态类型
function getStatusType(status) {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'APPROVED':
      return 'success'
    case 'REJECTED':
      return 'danger'
    default:
      return ''
  }
}

// 搜索
function search() {
  console.log('搜索条件:', searchForm)
  // 这里可以添加搜索API调用
}

// 重置
function reset() {
  searchForm.docTitle = ''
  searchForm.username = ''
  searchForm.status = ''
  searchForm.dateRange = []
}

// 查看文档
function viewDocument(docId) {
  console.log('查看文档:', docId)
  // 这里可以添加查看文档的逻辑
}

// 批准评论
function approveComment(id) {
  console.log('批准评论:', id)
  // 这里可以添加批准评论的API调用
  ElMessage.success('评论已批准')
}

// 拒绝评论
function rejectComment(id) {
  console.log('拒绝评论:', id)
  // 这里可以添加拒绝评论的API调用
  ElMessage.success('评论已拒绝')
}

// 删除评论
function deleteComment(id) {
  ElMessageBox.confirm('确定要删除这个评论吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    console.log('删除评论:', id)
    ElMessage.success('删除成功')
    // 这里可以添加删除评论的API调用
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
.comment-management {
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

.comment-content {
  line-height: 1.4;
  white-space: normal;
  word-break: break-word;
}

.rating {
  display: flex;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>