<template>
  <div class="document-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>文档管理</span>
          <div>
            <el-button type="danger" size="small" @click="handleBatchDelete"
              :disabled="selectedIds.length === 0">批量删除</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="标题/描述" style="width: 160px" clearable></el-input>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="searchForm.fileType" placeholder="全部" style="width: 100px" clearable>
            <el-option label="PDF" value="pdf"></el-option>
            <el-option label="Word" value="docx"></el-option>
            <el-option label="Excel" value="xlsx"></el-option>
            <el-option label="PPT" value="pptx"></el-option>
            <el-option label="TXT" value="txt"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.processStatus" placeholder="全部" style="width: 110px" clearable>
            <el-option label="已上传" value="UPLOADED"></el-option>
            <el-option label="解析中" value="PARSING"></el-option>
            <el-option label="查重中" value="SIMILARITY_CHECKING"></el-option>
            <el-option label="AI检测中" value="AI_DETECTING"></el-option>
            <el-option label="已完成" value="COMPLETED"></el-option>
            <el-option label="失败" value="FAILED"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="documents" style="width: 100%" border v-loading="loading"
        @selection-change="handleSelectionChange" :header-cell-style="{ textAlign: 'center' }"
        :cell-style="{ textAlign: 'center' }">
        <el-table-column type="selection" width="45"></el-table-column>
        <el-table-column prop="id" label="ID" width="65"></el-table-column>
        <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip
          :header-cell-style="{ textAlign: 'center' }" :cell-style="{ textAlign: 'left' }">
          <template #default="scope">
            <el-link type="primary" @click="previewDocument(scope.row)">{{ scope.row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="fileType" label="类型" width="76">
          <template #default="scope">
            <el-tag size="small" :type="getFileTypeTagType(scope.row.fileType)">{{ scope.row.fileType?.toUpperCase()
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fileSize" label="大小" width="80">
          <template #default="scope">{{ formatFileSize(scope.row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="uploadUserName" label="上传者" width="100" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.uploadUserName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.category || '-' }}</template>
        </el-table-column>
        <el-table-column prop="processStatus" label="处理状态" width="90">
          <template #default="scope">
            <el-tag size="small" :type="getProcessStatusType(scope.row.processStatus)">{{
              getProcessStatusText(scope.row.processStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overallSimilarity" label="总重复率" width="100">
          <template #default="scope">
            <span v-if="scope.row.overallSimilarity != null"
              :style="{ color: getSimilarityColor(scope.row.overallSimilarity) }">
              {{ scope.row.overallSimilarity }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="textSimilarity" label="文字重复率" width="100">
          <template #default="scope">
            <span v-if="scope.row.textSimilarity != null"
              :style="{ color: getSimilarityColor(scope.row.textSimilarity) }">
              {{ scope.row.textSimilarity }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="tableSimilarity" label="表格重复率" width="100">
          <template #default="scope">
            <span v-if="scope.row.tableSimilarity != null"
              :style="{ color: getSimilarityColor(scope.row.tableSimilarity) }">
              {{ scope.row.tableSimilarity }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="formulaSimilarity" label="公式重复率" width="100">
          <template #default="scope">
            <span v-if="scope.row.formulaSimilarity != null"
              :style="{ color: getSimilarityColor(scope.row.formulaSimilarity) }">
              {{ scope.row.formulaSimilarity }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="aiProbability" label="AI率" width="165">
          <template #default="scope">
            <span v-if="scope.row.aiProbability != null"
              :style="{ color: getAiColor(scope.row.aiProbability) }">
              {{ scope.row.aiProbability }}%
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="qualityScore" label="质量评分" width="100">
          <template #default="scope">{{ scope.row.qualityScore != null ? scope.row.qualityScore : '-' }}</template>
        </el-table-column>
        <el-table-column prop="readabilityScore" label="可读性评分" width="100">
          <template #default="scope">{{ scope.row.readabilityScore != null ? scope.row.readabilityScore : '-' }}</template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览数" width="70"></el-table-column>
        <el-table-column prop="downloadCount" label="下载数" width="70"></el-table-column>
        <el-table-column prop="favoriteCount" label="收藏数" width="70">
          <template #default="scope">{{ scope.row.favoriteCount ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="上传时间" width="165"></el-table-column>
        <el-table-column prop="updateTime" label="修改时间" width="165">
          <template #default="scope">{{ scope.row.updateTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="50" fixed="right">
          <template #default="scope">
            <el-dropdown trigger="click">
              <el-button :icon="MoreFilled" circle size="small"></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="previewDocument(scope.row)">预览</el-dropdown-item>
                  <el-dropdown-item @click="viewAuditRecord(scope.row)">审核记录</el-dropdown-item>
                  <el-dropdown-item @click="editDocument(scope.row)">编辑</el-dropdown-item>
                  <el-dropdown-item @click="deleteDocument(scope.row)" divided
                    style="color: #F56C6C">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total"
          @size-change="handleSizeChange" @current-change="handleCurrentChange"></el-pagination>
      </div>
    </el-card>

    <el-dialog v-model="showPreviewDialog" :title="previewTitle" width="90%" top="8vh" destroy-on-close >
      <div class="preview-container" v-loading="previewLoading">
        <iframe v-if="previewUrl" :src="previewUrl" style="width: 100%; height: 75vh; border: none;"></iframe>
        <div v-else-if="!previewLoading" class="preview-empty">该文件类型不支持在线预览</div>
      </div>
    </el-dialog>

    <el-dialog v-model="showAuditDialog" title="审核记录" width="700px" top="26vh">
      <el-table :data="auditRecords" style="width: 100%" border v-loading="auditLoading">
        <el-table-column prop="auditType" label="审核类型" width="100">
          <template #default="scope">{{ getAuditTypeText(scope.row.auditType) }}</template>
        </el-table-column>
        <el-table-column prop="overallStatus" label="审核状态" width="100">
          <template #default="scope">
            <el-tag size="small" :type="getAuditStatusType(scope.row.overallStatus || scope.row.status)">{{ getAuditStatusText(scope.row.overallStatus || scope.row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="审核人" width="100">
          <template #default="scope">{{ scope.row.operator || '-' }}</template>
        </el-table-column>
        <el-table-column label="审核结果" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <span>{{ formatAuditResult(scope.row.textDetails || scope.row.imageDetails) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="auditTime" label="审核时间" width="160">
          <template #default="scope">{{ scope.row.auditTime || '-' }}</template>
        </el-table-column>
      </el-table>
      <div v-if="!auditLoading && auditRecords.length === 0" style="text-align: center; padding: 30px; color: #999;">
        暂无审核记录
      </div>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="编辑文档" width="800px" top="18vh">
      <el-form :model="editForm" ref="editFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入文档标题"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入文档描述"></el-input>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category" placeholder="请选择分类" clearable style="width: 100%">
            <el-option v-for="cat in categoryList" :key="cat.id" :label="cat.name" :value="cat.name"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="editForm.tagValues" multiple filterable allow-create default-first-option
            placeholder="请选择或输入标签" style="width: 100%">
            <el-option v-for="tag in tagList" :key="tag.id" :label="tag.topicValue" :value="tag.topicValue"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRejectDialog" title="拒绝审核" width="400px">
      <el-form :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="请输入拒绝原因"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRejectDialog = false">取消</el-button>
        <el-button type="danger" @click="submitReject" :loading="rejectLoading">确定拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MoreFilled } from '@element-plus/icons-vue'
import {
  getAdminDocumentList, deleteAdminDocument, batchDeleteAdminDocuments,
  approveDocument, rejectDocument, updateAdminDocument,
  getDocumentAuditList, getDocumentPreviewUrl,
  getCategoryList, getTagList
} from '@/api/admin'

const loading = ref(false)
const documents = ref([])
const selectedIds = ref([])

const searchForm = reactive({
  keyword: '',
  fileType: '',
  processStatus: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const showPreviewDialog = ref(false)
const previewLoading = ref(false)
const previewTitle = ref('')
const previewUrl = ref('')

const showAuditDialog = ref(false)
const auditLoading = ref(false)
const auditRecords = ref([])
const currentAuditDoc = ref(null)

const showEditDialog = ref(false)
const editLoading = ref(false)
const editFormRef = ref()
const editForm = reactive({
  id: null,
  title: '',
  description: '',
  category: '',
  tagValues: []
})
const categoryList = ref([])
const tagList = ref([])

const showRejectDialog = ref(false)
const rejectLoading = ref(false)
const rejectForm = reactive({
  id: null,
  reason: ''
})

function loadDocuments() {
  loading.value = true
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    keyword: searchForm.keyword,
    fileType: searchForm.fileType,
    processStatus: searchForm.processStatus
  }
  getAdminDocumentList(params, (data) => {
    documents.value = data.records || data.list || []
    pagination.total = data.total || 0
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载文档列表失败')
    loading.value = false
  })
}

function search() {
  pagination.pageNum = 1
  loadDocuments()
}

function reset() {
  searchForm.keyword = ''
  searchForm.fileType = ''
  searchForm.processStatus = ''
  pagination.pageNum = 1
  loadDocuments()
}

function handleSelectionChange(rows) {
  selectedIds.value = rows.map(r => r.id)
}

function previewDocument(row) {
  previewTitle.value = `预览 - ${row.title}`
  previewUrl.value = ''
  previewLoading.value = true
  showPreviewDialog.value = true

  getDocumentPreviewUrl(row.id, (data) => {
    previewUrl.value = data.previewUrl
    previewLoading.value = false
  }, (msg) => {
    ElMessage.error(msg || '获取预览地址失败')
    previewLoading.value = false
  })
}

function viewAuditRecord(row) {
  auditLoading.value = true
  auditRecords.value = []
  showAuditDialog.value = true
  currentAuditDoc.value = row

  getDocumentAuditList(row.id, (data) => {
    const list = Array.isArray(data) ? data : []
    if (list.length > 0) {
      auditRecords.value = list
    } else if (row.auditStatus || row.processStatus) {
      auditRecords.value = [{
        auditType: 'ALL',
        overallStatus: row.auditStatus || row.processStatus,
        status: row.auditStatus || row.processStatus,
        operator: 'SYSTEM',
        textDetails: row.auditResult || null,
        auditTime: row.updateTime || row.createTime
      }]
    }
    auditLoading.value = false
  }, () => {
    if (row.auditStatus || row.processStatus) {
      auditRecords.value = [{
        auditType: 'ALL',
        overallStatus: row.auditStatus || row.processStatus,
        status: row.auditStatus || row.processStatus,
        operator: 'SYSTEM',
        textDetails: row.auditResult || null,
        auditTime: row.updateTime || row.createTime
      }]
    }
    auditLoading.value = false
  })
}

function editDocument(row) {
  editForm.id = row.id
  editForm.title = row.title || ''
  editForm.description = row.description || ''
  editForm.category = row.category || ''
  editForm.tagValues = (row.topics || []).map(t => t.topicValue)
  showEditDialog.value = true

  loadCategoryAndTags()
}

function loadCategoryAndTags() {
  getCategoryList((data) => {
    categoryList.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
  }, () => { })

  getTagList((data) => {
    tagList.value = Array.isArray(data) ? data : (data?.records || data?.list || [])
  }, () => { })
}

function submitEdit() {
  if (!editForm.title) {
    ElMessage.warning('标题不能为空')
    return
  }
  editLoading.value = true
  const data = {
    title: editForm.title,
    description: editForm.description,
    category: editForm.category
  }
  updateAdminDocument(editForm.id, data, () => {
    ElMessage.success('更新成功')
    editLoading.value = false
    showEditDialog.value = false
    loadDocuments()
  }, (msg) => {
    ElMessage.error(msg || '更新失败')
    editLoading.value = false
  })
}

function deleteDocument(row) {
  ElMessageBox.confirm(`确定要删除文档「${row.title}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteAdminDocument(row.id, () => {
      ElMessage.success('删除成功')
      loadDocuments()
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => { })
}

function handleBatchDelete() {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择要删除的文档')
    return
  }
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个文档吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    batchDeleteAdminDocuments(selectedIds.value, () => {
      ElMessage.success('删除成功')
      loadDocuments()
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => { })
}

function submitReject() {
  rejectLoading.value = true
  rejectDocument(rejectForm.id, { reason: rejectForm.reason }, () => {
    ElMessage.success('已拒绝')
    rejectLoading.value = false
    showRejectDialog.value = false
    loadDocuments()
  }, (msg) => {
    ElMessage.error(msg || '操作失败')
    rejectLoading.value = false
  })
}

function formatFileSize(bytes) {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
}

function getSimilarityColor(val) {
  if (val > 80) return '#F56C6C'
  if (val > 50) return '#E6A23C'
  return '#67C23A'
}

function getAiColor(val) {
  if (val > 70) return '#F56C6C'
  if (val > 40) return '#E6A23C'
  return '#67C23A'
}

function getFileTypeTagType(type) {
  const map = { pdf: 'danger', docx: 'primary', doc: 'primary', xlsx: 'success', xls: 'success', pptx: 'warning', ppt: 'warning', txt: 'info' }
  return map[type?.toLowerCase()] || ''
}

function getProcessStatusType(status) {
  const map = { UPLOADED: 'info', PARSING: 'warning', SIMILARITY_CHECKING: 'warning', AI_DETECTING: 'warning', COMPLETED: 'success', FAILED: 'danger' }
  return map[status] || 'info'
}

function getProcessStatusText(status) {
  const map = { UPLOADED: '已上传', PARSING: '解析中', SIMILARITY_CHECKING: '查重中', AI_DETECTING: 'AI检测中', COMPLETED: '已完成', FAILED: '失败' }
  return map[status] || status || '-'
}

function getAuditTypeText(type) {
  const map = { TEXT: '文本审核', IMAGE: '图片审核', ALL: '全量审核' }
  return map[type] || type || '-'
}

function getAuditStatusType(status) {
  const map = { PENDING: 'info', PROCESSING: 'warning', PASS: 'success', REJECT: 'danger', WARN: 'warning' }
  return map[status] || 'info'
}

function getAuditStatusText(status) {
  const map = { PENDING: '待审核', PROCESSING: '审核中', PASS: '通过', REJECT: '拒绝', WARN: '警告' }
  return map[status] || status || '-'
}

function formatAuditResult(result) {
  if (!result) return '-'
  try {
    const obj = JSON.parse(result)
    return obj.suggestion || obj.message || result
  } catch {
    if (result.length > 100) return result.substring(0, 100) + '...'
    return result
  }
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadDocuments()
}

function handleCurrentChange(current) {
  pagination.pageNum = current
  loadDocuments()
}

onMounted(() => {
  loadDocuments()
})
</script>

<style scoped>
.document-management {
  padding: 10px;
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

.preview-container {
  min-height: 300px;
}

.preview-empty {
  text-align: center;
  padding: 80px 0;
  color: #999;
  font-size: 16px;
}

/* 关键：深度作用选择器 + 控制内容区高度 */
:deep(.long-dialog .el-dialog__body) {
  /* 想拉多长就改这里！单位 vh 是屏幕高度 */
  max-height: 85vh;
  /* 内容超长自动出滚动条 */
  overflow-y: auto;
}

</style>
