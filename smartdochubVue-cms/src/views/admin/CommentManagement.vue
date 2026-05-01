<template>
  <div class="comment-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>评论管理</span>
          <div class="header-actions">
            <el-button type="success" size="small" @click="batchAudit('APPROVED')" :disabled="!selectedIds.length">
              批量通过
            </el-button>
            <el-button type="danger" size="small" @click="batchAudit('REJECTED')" :disabled="!selectedIds.length">
              批量拒绝
            </el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="文档ID">
          <el-input v-model="searchForm.documentId" placeholder="请输入文档ID" style="width: 120px"></el-input>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="评论内容" style="width: 150px"></el-input>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="searchForm.auditStatus" placeholder="请选择" style="width: 130px" clearable>
            <el-option label="全部" value=""></el-option>
            <el-option label="通过" value="APPROVED"></el-option>
            <el-option label="拒绝" value="REJECTED"></el-option>
            <el-option label="待人工审核" value="MANUAL_REVIEW"></el-option>
            <el-option label="待审核" value="PENDING"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="comments" style="width: 100%" border v-loading="loading"
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="id" label="ID" width="70"></el-table-column>
        <el-table-column prop="documentId" label="文档ID" width="80"></el-table-column>
        <el-table-column prop="userId" label="用户ID" width="80"></el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="250">
          <template #default="scope">
            <div class="comment-content">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" width="120">
          <template #default="scope">
            <el-tag :type="getAuditStatusType(scope.row.auditStatus)" size="small">
              {{ getAuditStatusText(scope.row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditConfidence" label="置信度" width="90">
          <template #default="scope">
            <span v-if="scope.row.auditConfidence != null"
              :style="{ color: getConfidenceColor(scope.row.auditConfidence) }">
              {{ (scope.row.auditConfidence * 100).toFixed(1) }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="auditReason" label="审核原因" min-width="150">
          <template #default="scope">
            <span>{{ scope.row.auditReason || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="170">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewDetail(scope.row)">详情</el-button>
            <el-button size="small"
              v-if="scope.row.auditStatus === 'MANUAL_REVIEW' || scope.row.auditStatus === 'PENDING'" type="success"
              @click="manualAudit(scope.row, 'APPROVED')">通过</el-button>
            <el-button size="small"
              v-if="scope.row.auditStatus === 'MANUAL_REVIEW' || scope.row.auditStatus === 'PENDING'" type="danger"
              @click="manualAudit(scope.row, 'REJECTED')">拒绝</el-button>
            <el-button size="small" type="danger" @click="deleteComment(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination :current-page="pagination.pageNum" :page-size="pagination.pageSize"
          @update:current-page="pagination.pageNum = $event; handleCurrentChange($event)"
          @update:page-size="pagination.pageSize = $event; handleSizeChange($event)" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" :total="pagination.total" @size-change="handleSizeChange"
          @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <el-dialog v-model="showDetailDialog" title="评论详情" width="600px">
      <el-descriptions :column="2" border v-if="currentComment">
        <el-descriptions-item label="评论ID" :span="2">{{ currentComment.id }}</el-descriptions-item>
        <el-descriptions-item label="文档ID">{{ currentComment.documentId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentComment.userId }}</el-descriptions-item>
        <el-descriptions-item label="评论内容" :span="2">
          <div style="white-space: pre-wrap; word-break: break-word;">{{ currentComment.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getAuditStatusType(currentComment.auditStatus)">
            {{ getAuditStatusText(currentComment.auditStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="置信度">
          <span v-if="currentComment.auditConfidence != null"
            :style="{ color: getConfidenceColor(currentComment.auditConfidence) }">
            {{ (currentComment.auditConfidence * 100).toFixed(1) }}%
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="审核原因" :span="2">{{ currentComment.auditReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ formatDate(currentComment.auditTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="评论时间">{{ formatDate(currentComment.createTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAuditDialog" title="审核评论" width="400px">
      <el-form :model="auditForm" ref="auditFormRef" label-width="80px">
        <el-form-item label="评论内容">
          <div class="audit-content">{{ auditForm.content }}</div>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditForm.result">
            <el-radio label="APPROVED">通过</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核原因">
          <el-input v-model="auditForm.reason" type="textarea" :rows="3" placeholder="请输入审核原因（可选）"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAuditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitAudit" :loading="auditLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminCommentPage, getAdminCommentDetail, auditComment, batchAuditComments, deleteComment as deleteCommentApi } from '@/api/admin'

const loading = ref(false)
const comments = ref([])
const selectedIds = ref([])
const showDetailDialog = ref(false)
const currentComment = ref(null)
const showAuditDialog = ref(false)
const auditLoading = ref(false)
const auditFormRef = ref()

const searchForm = reactive({
  documentId: '',
  keyword: '',
  auditStatus: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const auditForm = reactive({
  id: null,
  content: '',
  result: 'APPROVED',
  reason: ''
})

function loadComments() {
  loading.value = true
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    documentId: searchForm.documentId || null,
    keyword: searchForm.keyword || null,
    auditStatus: searchForm.auditStatus || null
  }
  getAdminCommentPage(params, (data) => {
    comments.value = data.records || data.list || []
    pagination.total = data.total || 0
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载评论列表失败')
    loading.value = false
  })
}

function search() {
  pagination.pageNum = 1
  loadComments()
}

function reset() {
  searchForm.documentId = ''
  searchForm.keyword = ''
  searchForm.auditStatus = ''
  pagination.pageNum = 1
  loadComments()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function viewDetail(row) {
  getAdminCommentDetail(row.id, (data) => {
    currentComment.value = data
    showDetailDialog.value = true
  }, (msg) => {
    ElMessage.error(msg || '加载评论详情失败')
  })
}

function manualAudit(row, result) {
  auditForm.id = row.id
  auditForm.content = row.content
  auditForm.result = result
  auditForm.reason = ''
  showAuditDialog.value = true
}

function submitAudit() {
  auditLoading.value = true
  auditComment(auditForm.id, auditForm.result, auditForm.reason, () => {
    ElMessage.success('审核成功')
    auditLoading.value = false
    showAuditDialog.value = false
    loadComments()
  }, (msg) => {
    ElMessage.error(msg || '审核失败')
    auditLoading.value = false
  })
}

function batchAudit(result) {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先选择要审核的评论')
    return
  }
  ElMessageBox.confirm(`确定要批量${result === 'APPROVED' ? '通过' : '拒绝'}选中的 ${selectedIds.value.length} 条评论吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    batchAuditComments(selectedIds.value, result, '', () => {
      ElMessage.success('批量审核成功')
      loadComments()
    }, (msg) => {
      ElMessage.error(msg || '批量审核失败')
    })
  }).catch(() => { })
}

function deleteComment(row) {
  ElMessageBox.confirm('确定要删除该评论吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteCommentApi(row.id, () => {
      ElMessage.success('删除成功')
      loadComments()
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => { })
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadComments()
}

function handleCurrentChange(current) {
  pagination.pageNum = current
  loadComments()
}

function getAuditStatusType(status) {
  const map = { 'APPROVED': 'success', 'REJECTED': 'danger', 'MANUAL_REVIEW': 'warning', 'PENDING': 'info' }
  return map[status] || 'info'
}

function getAuditStatusText(status) {
  const map = { 'APPROVED': '通过', 'REJECTED': '拒绝', 'MANUAL_REVIEW': '待人工审核', 'PENDING': '待审核' }
  return map[status] || status || '-'
}

function getConfidenceColor(val) {
  if (val >= 0.8) return '#f56c6c'
  if (val >= 0.5) return '#e6a23c'
  return '#67c23a'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
}

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.comment-management {
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

.header-actions {
  display: flex;
  gap: 8px;
}

.search-form {
  margin-bottom: 20px;
}

.comment-content {
  line-height: 1.4;
  white-space: normal;
  word-break: break-word;
  max-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.audit-content {
  white-space: pre-wrap;
  word-break: break-word;
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  max-height: 100px;
  overflow-y: auto;
}
</style>