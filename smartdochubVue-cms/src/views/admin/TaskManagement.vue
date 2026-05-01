<template>
  <div class="task-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>任务管理</span>
          <div>
            <el-select v-model="filterStatus" placeholder="任务状态" style="width: 120px; margin-right: 10px" clearable
              @change="handleFilterChange">
              <el-option label="全部" value=""></el-option>
              <el-option label="待处理" value="PENDING"></el-option>
              <el-option label="进行中" value="RUNNING"></el-option>
              <el-option label="已完成" value="COMPLETED"></el-option>
              <el-option label="失败" value="FAILED"></el-option>
            </el-select>
            <el-button type="danger" size="small" @click="handleBatchDelete" :disabled="selectedIds.length === 0">批量删除</el-button>
            <el-button type="primary" size="small" @click="handleRefresh">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%" v-loading="loading" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="taskType" label="任务类型" width="120">
          <template #default="scope">
            <el-tag size="small" :type="getTaskTypeTagType(scope.row.taskType)">
              {{ getTaskTypeText(scope.row.taskType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag size="small" :type="getStatusTagType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="150">
          <template #default="scope">
            <el-progress :percentage="scope.row.progress || 0" :status="getProgressStatus(scope.row.status)" />
          </template>
        </el-table-column>
        <el-table-column prop="result" label="结果" min-width="150">
          <template #default="scope">
            <span v-if="scope.row.result" class="result-text">{{ scope.row.result }}</span>
            <span v-else-if="scope.row.errorMsg" class="error-text">{{ scope.row.errorMsg }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleView(scope.row)">详情</el-button>
            <el-button v-if="scope.row.status === 'RUNNING' || scope.row.status === 'PENDING'" size="small"
              type="danger" @click="handleCancel(scope.row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination :current-page="pagination.pageNum" :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total"
          @size-change="handleSizeChange" @current-change="handleCurrentChange"></el-pagination>
      </div>
    </el-card>

    <el-dialog v-model="showDetailDialog" title="任务详情" width="600px">
      <el-descriptions :column="2" border v-if="currentTask">
        <el-descriptions-item label="任务ID" :span="2">{{ currentTask.id }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">{{ getTaskTypeText(currentTask.taskType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="getStatusTagType(currentTask.status)">
            {{ getStatusText(currentTask.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="进度">
          <el-progress :percentage="currentTask.progress || 0" :status="getProgressStatus(currentTask.status)"
            style="width: 200px" />
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ currentTask.updateTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="任务结果" :span="2">
          <div v-if="currentTask.result" class="result-detail">{{ currentTask.result }}</div>
          <div v-else-if="currentTask.errorMsg" class="error-detail">{{ currentTask.errorMsg }}</div>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTaskList, getTaskDetail, deleteTask, batchDeleteTasks } from '@/api/admin'

const loading = ref(false)
const tableData = ref([])
const selectedIds = ref([])
const filterStatus = ref('')
const showDetailDialog = ref(false)
const currentTask = ref(null)

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

function loadTasks() {
  loading.value = true
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    status: filterStatus.value
  }
  getTaskList(params, (data) => {
    tableData.value = data.records || data.list || []
    pagination.total = data.total || 0
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载任务列表失败')
    loading.value = false
  })
}

function handleRefresh() {
  loadTasks()
}

function handleFilterChange() {
  pagination.pageNum = 1
  loadTasks()
}

function handleView(row) {
  getTaskDetail(row.id, (data) => {
    currentTask.value = data
    showDetailDialog.value = true
  }, (msg) => {
    ElMessage.error(msg || '加载任务详情失败')
  })
}

function handleCancel(row) {
  ElMessageBox.confirm(`确定要取消该任务吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteTask(row.id, () => {
      ElMessage.success('任务已取消')
      loadTasks()
    }, (msg) => {
      ElMessage.error(msg || '取消失败')
    })
  }).catch(() => { })
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的任务')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个任务吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    batchDeleteTasks(selectedIds.value, () => {
      ElMessage.success('删除成功')
      loadTasks()
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => { })
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadTasks()
}

function handleCurrentChange(current) {
  pagination.pageNum = current
  loadTasks()
}

function getTaskTypeText(type) {
  const map = {
    'DOCUMENT_UPLOAD': '文档上传',
    'DOCUMENT_AUDIT': '文档审核',
    'SIMILARITY_CHECK': '相似度检测',
    'CONTENT_CHUNK': '内容分块',
    'VECTOR_GENERATE': '向量生成',
    'AI_ANALYSIS': 'AI分析',
    'EXPORT': '导出任务',
    'IMPORT': '导入任务'
  }
  return map[type] || type || '-'
}

function getTaskTypeTagType(type) {
  const map = {
    'DOCUMENT_UPLOAD': 'primary',
    'DOCUMENT_AUDIT': 'warning',
    'SIMILARITY_CHECK': 'success',
    'CONTENT_CHUNK': 'info',
    'VECTOR_GENERATE': 'danger',
    'AI_ANALYSIS': 'warning',
    'EXPORT': 'primary',
    'IMPORT': 'primary'
  }
  return map[type] || 'info'
}

function getStatusText(status) {
  const map = {
    'PENDING': '待处理',
    'RUNNING': '进行中',
    'COMPLETED': '已完成',
    'FAILED': '失败',
    'CANCELLED': '已取消'
  }
  return map[status] || status || '-'
}

function getStatusTagType(status) {
  const map = {
    'PENDING': 'info',
    'RUNNING': 'primary',
    'COMPLETED': 'success',
    'FAILED': 'danger',
    'CANCELLED': 'warning'
  }
  return map[status] || 'info'
}

function getProgressStatus(status) {
  if (status === 'COMPLETED') return 'success'
  if (status === 'FAILED') return 'exception'
  return undefined
}

onMounted(() => {
  loadTasks()
})
</script>

<style scoped>
.task-management {
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.result-text {
  color: #67c23a;
  font-size: 13px;
}

.error-text {
  color: #f56c6c;
  font-size: 13px;
}

.result-detail {
  white-space: pre-wrap;
  word-break: break-word;
  color: #67c23a;
  font-size: 13px;
}

.error-detail {
  white-space: pre-wrap;
  word-break: break-word;
  color: #f56c6c;
  font-size: 13px;
}
</style>