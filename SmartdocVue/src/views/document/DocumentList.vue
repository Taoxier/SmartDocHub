<template>
  <div class="document-list">
    <div class="page-header" v-if="isSpecialPage && isHistoryPage">
      <div class="action-buttons">
        <el-button type="danger" plain @click="handleClearHistory">
          清空历史
        </el-button>
      </div>
    </div>

    <div class="search-bar" v-if="!isSpecialPage">
      <el-input v-model="searchParams.keyword" placeholder="搜索文档标题、描述..." :prefix-icon="Search" clearable
        style="width: 300px" @keyup.enter="handleSearch" />
      <el-select v-model="searchParams.fileType" placeholder="文件类型" clearable style="width: 140px">
        <el-option label="PDF" value="pdf" />
        <el-option label="Word" value="docx" />
        <el-option label="TXT" value="txt" />
      </el-select>
      <el-select v-model="searchParams.sortBy" placeholder="排序方式" style="width: 140px">
        <el-option label="最新上传" value="createTime" />
        <el-option label="最多浏览" value="viewCount" />
        <el-option label="最多下载" value="downloadCount" />
      </el-select>
      <el-button type="warning" plain @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <div class="document-grid" v-loading="loading">
      <el-empty v-if="documentList.length === 0 && !loading" :description="emptyDescription">
      </el-empty>

      <div v-for="doc in documentList" :key="doc.id" class="document-card" @click="goToDetail(doc.id)">
        <div class="card-header">
          <div class="file-icon" :style="{ backgroundColor: getFileIcon(doc.fileType).color + '20' }">
            <el-icon :size="24" :color="getFileIcon(doc.fileType).color">
              <Document />
            </el-icon>
          </div>
          <div class="file-type-tag">{{ doc.fileType?.toUpperCase() }}</div>
          <div class="card-actions">
            <el-dropdown trigger="hover" @click.stop>
              <el-button circle text>
                <el-icon>
                  <More />
                </el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="isMyDocsPage" @click.stop="handleRename(doc)">
                    <el-icon>
                      <Edit />
                    </el-icon> 重命名
                  </el-dropdown-item>
                  <el-dropdown-item @click.stop="handleDownload(doc)">
                    <el-icon>
                      <Download />
                    </el-icon> 下载
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isMyDocsPage" @click.stop="handleDelete(doc)">
                    <el-icon>
                      <Delete />
                    </el-icon> 删除
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isFavoritePage" @click.stop="handleUnfavorite(doc)">
                    <el-icon>
                      <Star />
                    </el-icon> 取消收藏
                  </el-dropdown-item>
                  <el-dropdown-item v-if="isHistoryPage" @click.stop="handleRemoveHistory(doc)">
                    <el-icon>
                      <Delete />
                    </el-icon> 删除记录
                  </el-dropdown-item>
                  <el-dropdown-item @click.stop="handleShare(doc)">
                    <el-icon>
                      <Share />
                    </el-icon> 分享
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <div class="card-body">
          <h3 class="doc-title" :title="doc.title">{{ doc.title }}</h3>
          <p class="doc-desc" :title="doc.description">{{ doc.description || '暂无描述' }}</p>
          <div class="doc-category" v-if="doc.category">
            <el-tag size="small" class="doc-category-tag">{{ doc.category }}</el-tag>
          </div>
          <div class="doc-tags" v-if="doc.topics && doc.topics.length > 0">
            <el-tag v-for="topic in doc.topics.slice(0, 3)" :key="topic.id" size="small" class="doc-topic-tag">{{
              topic.topicValue
            }}</el-tag>
            <el-tag v-if="doc.topics.length > 3" size="small" class="doc-topic-tag">+{{ doc.topics.length - 3
              }}</el-tag>
          </div>
        </div>

        <div class="card-footer">
          <div class="ai-stats" v-if="doc.overallSimilarity !== undefined || doc.aiProbability !== undefined">
            <div class="stat-item" v-if="doc.overallSimilarity !== undefined">
              <span class="stat-label">相似度</span>
              <span class="stat-value" :class="getSimilarityClass(doc.overallSimilarity)">{{ (doc.overallSimilarity *
                100).toFixed(1) }}%</span>
            </div>
            <div class="stat-item" v-if="doc.aiProbability !== undefined">
              <span class="stat-label">AI率</span>
              <span class="stat-value" :class="getAIProbabilityClass(doc.aiProbability)">{{ (doc.aiProbability *
                100).toFixed(1)
                }}%</span>
            </div>
          </div>
          <div class="stats-right">
            <div class="stats">
              <span><el-icon>
                  <View />
                </el-icon> {{ doc.viewCount || 0 }}</span>
              <span><el-icon>
                  <Download />
                </el-icon> {{ doc.downloadCount || 0 }}</span>
            </div>
            <div class="file-size">{{ formatFileSize(doc.fileSize) }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination v-model:current-page="searchParams.pageNum" v-model:page-size="searchParams.pageSize"
        :page-sizes="[20, 50, 100]" :total="total"
        :layout="isMyUploadsOrFavorites ? 'total, sizes, prev, pager, next, jumper' : 'sizes, prev, pager, next, jumper'"
        @size-change="loadDocuments" @current-change="loadDocuments" />
    </div>

    <el-dialog v-model="showUploadDialog" title="上传文档" width="500px" :close-on-click-modal="false"
      v-if="!isSpecialPage">
      <el-upload ref="uploadRef" class="upload-area" drag :auto-upload="false" :limit="10" :on-change="handleFileChange"
        :on-exceed="handleExceed" accept=".pdf,.doc,.docx,.txt">
        <el-icon class="el-icon--upload" :size="48">
          <UploadFilled />
        </el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 PDF、Word、TXT 格式，单个文件不超过 50MB，最多同时上传 10 个文件
          </div>
        </template>
      </el-upload>

      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          确认上传
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRateDialog" title="文档评分" width="400px">
      <div class="rate-content">
        <p class="rate-title">{{ currentDoc?.title }}</p>
        <el-rate v-model="ratingValue" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-text />
      </div>
      <template #footer>
        <el-button @click="showRateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRating">确认评分</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRenameDialog" title="重命名文档" width="400px">
      <el-input v-model="renameValue" placeholder="请输入新的文档名称" />
      <template #footer>
        <el-button @click="showRenameDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRename">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDeleteDialog" title="确认删除" width="400px">
      <p>确定要删除文档 "{{ currentDoc?.title }}" 吗？</p>
      <template #footer>
        <el-button @click="showDeleteDialog = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search, Upload, Document, View, Download, Star, UploadFilled, More, Share, Edit, Delete
} from '@element-plus/icons-vue'
import {
  getDocumentList, uploadDocument, uploadMultipleDocuments, downloadDocument, rateDocument,
  formatFileSize, getFileIcon, getMyUploadedDocuments, getMyFavoriteDocuments, getMyHistoryDocuments,
  batchRemoveFavorite, batchRemoveHistory, clearHistory, unfavoriteDocument
} from '@/api/document'

const route = useRoute()
const router = useRouter()

const props = defineProps({
  isMyDocs: {
    type: Boolean,
    default: false
  },
  queryParams: {
    type: Object,
    default: () => ({})
  }
})

const loading = ref(false)
const uploading = ref(false)
const documentList = ref([])
const total = ref(0)
const showUploadDialog = ref(false)
const showRateDialog = ref(false)
const showRenameDialog = ref(false)
const showDeleteDialog = ref(false)
const currentDoc = ref(null)
const ratingValue = ref(5)
const uploadRef = ref()
const selectedFiles = ref([])
const renameValue = ref('')

const searchParams = reactive({
  pageNum: 1,
  pageSize: 20,
  keyword: '',
  fileType: '',
  sortBy: 'createTime',
  sortOrder: 'desc'
})

// 计算属性：当前路由名称
const currentRouteName = computed(() => route.name)

// 计算属性：当前路径
const currentPath = computed(() => route.path)

// 计算属性：是否为特殊页面（分类浏览、标签浏览、我的上传、我的收藏、浏览历史）
const isSpecialPage = computed(() => {
  const path = currentPath.value
  return path.includes('/user/uploaded') || path.includes('/user/favorites') || path.includes('/user/history') || path.includes('/categories') || path.includes('/tags')
})

// 计算属性：是否为我的上传或我的收藏页面
const isMyUploadsOrFavorites = computed(() => {
  const path = currentPath.value
  return path.includes('/user/uploaded') || path.includes('/user/favorites')
})

// 计算属性：页面标题
const pageTitle = computed(() => {
  const path = currentPath.value
  if (path.includes('/user/uploaded')) {
    return '我的上传'
  } else if (path.includes('/user/favorites')) {
    return '我的收藏'
  } else if (path.includes('/user/history')) {
    return '浏览历史'
  } else if (path.includes('/categories')) {
    return '分类浏览'
  } else if (path.includes('/tags')) {
    return '标签浏览'
  } else {
    return '文档管理'
  }
})

// 计算属性：空状态描述
const emptyDescription = computed(() => {
  const path = currentPath.value
  if (path.includes('/user/uploaded')) {
    return '暂无上传文档'
  } else if (path.includes('/user/favorites')) {
    return '暂无收藏文档'
  } else if (path.includes('/user/history')) {
    return '暂无浏览历史'
  } else if (path.includes('/categories') || path.includes('/tags')) {
    return '暂无相关文档'
  } else {
    return '暂无文档'
  }
})

// 计算属性：是否为我的文档页面
const isMyDocsPage = computed(() => {
  return currentPath.value.includes('/user/uploaded')
})

// 计算属性：是否为收藏页面
const isFavoritePage = computed(() => {
  return currentPath.value.includes('/user/favorites')
})

// 计算属性：是否为历史页面
const isHistoryPage = computed(() => {
  return currentPath.value.includes('/user/history')
})

// 选中的文档ID列表
const selectedDocs = ref([])

watch(() => route.query.keyword, (val) => {
  if (val) {
    searchParams.keyword = val
    loadDocuments()
  }
}, { immediate: true })

// 监听路由路径变化，当路径变化时重新加载数据
watch(() => route.path, () => {
  // 重置搜索参数
  searchParams.pageNum = 1
  searchParams.keyword = ''
  searchParams.fileType = ''
  searchParams.sortBy = 'createTime'
  searchParams.sortOrder = 'desc'
  // 重新加载数据
  loadDocuments()
}, { immediate: true })

onMounted(() => {
  if (route.query.sortBy) {
    searchParams.sortBy = route.query.sortBy
  }
  loadDocuments()
})

function loadDocuments() {
  loading.value = true
  const params = { ...searchParams, ...props.queryParams }
  Object.keys(params).forEach(key => {
    if (!params[key]) delete params[key]
  })

  // 根据当前路径调用不同的API
  const path = currentPath.value
  if (path.includes('/user/uploaded')) {
    getMyUploadedDocuments(params, (data) => {
      documentList.value = data.list || []
      total.value = data.total || 0
      loading.value = false
    }, (msg) => {
      ElMessage.error(msg || '加载失败')
      loading.value = false
    })
  } else if (path.includes('/user/favorites')) {
    getMyFavoriteDocuments(params, (data) => {
      documentList.value = data.list || []
      total.value = data.total || 0
      loading.value = false
    }, (msg) => {
      ElMessage.error(msg || '加载失败')
      loading.value = false
    })
  } else if (path.includes('/user/history')) {
    getMyHistoryDocuments(params, (data) => {
      documentList.value = data.list || []
      total.value = data.total || 0
      loading.value = false
    }, (msg) => {
      ElMessage.error(msg || '加载失败')
      loading.value = false
    })
  } else if (path.includes('/categories') || path.includes('/tags')) {
    // 分类浏览和标签浏览暂时使用通用接口，后续可以添加专门的接口
    getDocumentList(params, (data) => {
      documentList.value = data.list || []
      total.value = data.total || 0
      loading.value = false
    }, (msg) => {
      ElMessage.error(msg || '加载失败')
      loading.value = false
    })
  } else {
    getDocumentList(params, (data) => {
      documentList.value = data.list || []
      total.value = data.total || 0
      loading.value = false
    }, (msg) => {
      ElMessage.error(msg || '加载失败')
      loading.value = false
    })
  }
}

function handleSearch() {
  searchParams.pageNum = 1
  loadDocuments()
}

function resetSearch() {
  searchParams.keyword = ''
  searchParams.fileType = ''
  searchParams.sortBy = 'createTime'
  handleSearch()
}

function goToDetail(id) {
  router.push(`/doc/detail/${id}`)
}

function handleDownload(doc) {
  downloadDocument(doc.id)
}

function handleRate(doc) {
  currentDoc.value = doc
  ratingValue.value = 5
  showRateDialog.value = true
}

function submitRating() {
  if (!currentDoc.value) return
  rateDocument(currentDoc.value.id, ratingValue.value, () => {
    ElMessage.success('评分成功')
    showRateDialog.value = false
  }, (msg) => {
    ElMessage.error(msg || '评分失败')
  })
}

function handleShare(doc) {
  // 生成分享链接
  const shareUrl = `${window.location.origin}/doc/detail/${doc.id}`
  // 复制到剪贴板
  navigator.clipboard.writeText(shareUrl).then(() => {
    ElMessage.success('分享链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制链接')
  })
}

function handleRename(doc) {
  currentDoc.value = doc
  renameValue.value = doc.title
  showRenameDialog.value = true
}

function handleDelete(doc) {
  currentDoc.value = doc
  showDeleteDialog.value = true
}

function submitRename() {
  if (!currentDoc.value || !renameValue.value) {
    ElMessage.warning('请输入新的名称')
    return
  }
  ElMessage.success('重命名成功')
  currentDoc.value.title = renameValue.value
  showRenameDialog.value = false
}

function confirmDelete() {
  if (!currentDoc.value) return
  ElMessage.success('删除成功')
  documentList.value = documentList.value.filter(d => d.id !== currentDoc.value.id)
  showDeleteDialog.value = false
}

// 取消收藏
function handleUnfavorite(doc) {
  unfavoriteDocument(doc.id, () => {
    ElMessage.success('取消收藏成功')
    documentList.value = documentList.value.filter(d => d.id !== doc.id)
  }, (msg) => {
    ElMessage.error(msg || '取消收藏失败')
  })
}

// 删除历史记录
function handleRemoveHistory(doc) {
  batchRemoveHistory([doc.id], () => {
    ElMessage.success('删除记录成功')
    documentList.value = documentList.value.filter(d => d.id !== doc.id)
  }, (msg) => {
    ElMessage.error(msg || '删除记录失败')
  })
}

// 批量操作
function handleBatchAction() {
  if (selectedDocs.value.length === 0) {
    ElMessage.warning('请先选择要操作的文档')
    return
  }

  if (isHistoryPage.value) {
    // 批量删除历史记录
    batchRemoveHistory(selectedDocs.value, () => {
      ElMessage.success('批量删除记录成功')
      documentList.value = documentList.value.filter(d => !selectedDocs.value.includes(d.id))
      selectedDocs.value = []
    }, (msg) => {
      ElMessage.error(msg || '批量删除记录失败')
    })
  }
}

// 清空历史记录
function handleClearHistory() {
  clearHistory(() => {
    ElMessage.success('清空历史记录成功')
    documentList.value = []
    selectedDocs.value = []
  }, (msg) => {
    ElMessage.error(msg || '清空历史记录失败')
  })
}

function handleFileChange(file, fileList) {
  selectedFiles.value = fileList.map(f => f.raw)
}

function handleExceed() {
  ElMessage.warning('最多同时上传 10 个文件')
}

function handleUpload() {
  if (selectedFiles.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }

  uploading.value = true
  if (selectedFiles.value.length === 1) {
    // 单个文件上传
    uploadDocument(selectedFiles.value[0], (percent) => {
      console.log('上传进度:', percent)
    }, (data) => {
      ElMessage.success('上传成功')
      showUploadDialog.value = false
      uploading.value = false
      selectedFiles.value = []
      uploadRef.value?.clearFiles()
      loadDocuments()
    }, (msg) => {
      ElMessage.error(msg || '上传失败')
      uploading.value = false
    })
  } else {
    // 多文件上传
    uploadMultipleDocuments(selectedFiles.value, (percent) => {
      console.log('上传进度:', percent)
    }, (data) => {
      ElMessage.success(`成功上传 ${data.length} 个文件`)
      showUploadDialog.value = false
      uploading.value = false
      selectedFiles.value = []
      uploadRef.value?.clearFiles()
      loadDocuments()
    }, (msg) => {
      ElMessage.error(msg || '上传失败')
      uploading.value = false
    })
  }
}

function getSimilarityClass(similarity) {
  if (similarity < 0.3) return 'similarity-low'
  if (similarity < 0.6) return 'similarity-medium'
  return 'similarity-high'
}

function getAIProbabilityClass(aiProbability) {
  if (aiProbability < 0.3) return 'ai-low'
  if (aiProbability < 0.6) return 'ai-medium'
  return 'ai-high'
}
</script>

<style scoped>
.document-list {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.document-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(224px, 1fr));
  gap: 20px;
  overflow-y: auto;
  align-items: start;
}

.document-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  min-height: 260px;
}

.document-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(255, 152, 0, 0.15);
  border-color: #ffcc80;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.file-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.file-type-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #fff0e6;
  color: #ff9800;
  font-weight: 500;
}

.card-body {
  margin-bottom: 12px;
}

.doc-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.doc-desc {
  font-size: 12px;
  color: #999;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.doc-category-tag {
  background-color: #fa8c16;
  color: white;
  border-color: #fa8c16;
}

.doc-topic-tag {
  background-color: #fff7e6;
  color: #fa8c16;
  border-color: #ffd591;
  font-size: 12px;
}

.doc-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.stats-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #999;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.file-size {
  font-size: 12px;
  color: #999;
}

.ai-stats {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.ai-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.ai-stats .stat-label {
  font-size: 11px;
  color: #999;
}

.ai-stats .stat-value {
  font-size: 12px;
  font-weight: 600;
}

.similarity-low {
  color: #4caf50;
}

.similarity-medium {
  color: #ff9800;
}

.similarity-high {
  color: #f44336;
}

.ai-low {
  color: #4caf50;
}

.ai-medium {
  color: #ff9800;
}

.ai-high {
  color: #f44336;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  background: white;
  border-radius: 12px;
  margin-top: 20px;
}

.upload-area {
  width: 100%;
}

:deep(.el-upload-dragger) {
  border: 2px dashed #ffcc80;
  background: #fffaf5;
  border-radius: 12px;
}

:deep(.el-upload-dragger:hover) {
  border-color: #ff9800;
}

.rate-content {
  text-align: center;
  padding: 20px 0;
}

.rate-title {
  font-size: 16px;
  color: #333;
  margin-bottom: 20px;
}
</style>
