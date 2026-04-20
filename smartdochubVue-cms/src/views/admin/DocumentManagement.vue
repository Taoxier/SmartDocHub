<template>
  <div class="document-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>文档管理</span>
          <el-button type="primary" size="small">批量操作</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="请输入文档标题或描述" style="width: 250px"></el-input>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="searchForm.fileType" placeholder="请选择文件类型" style="width: 140px">
            <el-option label="全部" value=""></el-option>
            <el-option label="PDF" value="pdf"></el-option>
            <el-option label="Word" value="docx"></el-option>
            <el-option label="Excel" value="xlsx"></el-option>
            <el-option label="PowerPoint" value="pptx"></el-option>
            <el-option label="图片" value="image"></el-option>
            <el-option label="其他" value="other"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" style="width: 140px">
            <el-option label="全部" value=""></el-option>
            <el-option label="待审核" value="PENDING"></el-option>
            <el-option label="已发布" value="PUBLISHED"></el-option>
            <el-option label="已删除" value="DELETED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="documents" style="width: 100%" border>
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="文档ID" width="80"></el-table-column>
        <el-table-column prop="title" label="文档标题" min-width="200">
          <template #default="scope">
            <el-link type="primary" @click="viewDocument(scope.row.id)">{{ scope.row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="上传用户" width="120"></el-table-column>
        <el-table-column prop="fileType" label="文件类型" width="100">
          <template #default="scope">
            <el-tag>{{ scope.row.fileType.toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="文件大小" width="100">
          <template #default="scope">
            {{ formatSize(scope.row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="180"></el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="80"></el-table-column>
        <el-table-column prop="downloadCount" label="下载量" width="80"></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDocument(scope.row.id)">预览</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" size="small" type="primary"
              @click="approveDocument(scope.row.id)">通过</el-button>
            <el-button v-if="scope.row.status === 'PENDING'" size="small" type="danger"
              @click="rejectDocument(scope.row.id)">拒绝</el-button>
            <el-button v-else size="small" type="warning" @click="editDocument(scope.row.id)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteDocument(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total"
          @size-change="handleSizeChange" @current-change="handleCurrentChange"></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminDocumentList, deleteAdminDocument } from '@/api/admin'

// 搜索表单
const searchForm = reactive({
  keyword: '',
  fileType: '',
  status: '',
  categoryId: null
})

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 文档列表
const documents = ref([])

// 加载文档列表
function loadDocuments() {
  const params = {
    pageNum: pagination.currentPage,
    pageSize: pagination.pageSize,
    keyword: searchForm.keyword,
    fileType: searchForm.fileType
  }
  getAdminDocumentList(params, (data) => {
    documents.value = data.list || []
    pagination.total = data.total || 0
  }, (error) => {
    ElMessage.error('加载文档列表失败: ' + (error.message || '未知错误'))
  })
}

// 格式化文件大小
function formatSize(size) {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  }
}

// 获取状态类型
function getStatusType(status) {
  switch (status) {
    case 'PENDING':
      return 'warning'
    case 'PUBLISHED':
      return 'success'
    case 'DELETED':
      return 'danger'
    default:
      return ''
  }
}

// 搜索
function search() {
  pagination.currentPage = 1
  loadDocuments()
}

// 重置
function reset() {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.fileType = ''
  searchForm.categoryId = null
  pagination.currentPage = 1
  loadDocuments()
}

// 查看文档
function viewDocument(id) {
  window.open(`/document/${id}`, '_blank')
}

// 删除文档
function deleteDocument(id) {
  ElMessageBox.confirm('确定要删除这个文档吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteAdminDocument(id, () => {
      ElMessage.success('删除成功')
      loadDocuments()
    }, (error) => {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    })
  }).catch(() => {
    // 取消删除
  })
}

// 分页处理
function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadDocuments()
}

function handleCurrentChange(current) {
  pagination.currentPage = current
  loadDocuments()
}

// 组件挂载时加载数据
onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.document-management {
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