<template>
  <div class="document-detail" v-loading="loading">
    <div class="detail-header">
      <el-button :icon="ArrowLeft" @click="goBack">
        <template #default>
          <span class="button-text">返回列表</span>
        </template>
      </el-button>
      <div class="header-actions">
        <el-tooltip content="下载文档" placement="bottom">
          <el-button type="primary" :icon="Download" @click="handleDownload" circle />
        </el-tooltip>
        <el-tooltip content="评分" placement="bottom">
          <el-button type="warning" :icon="Star" @click="showRateDialog = true" circle />
        </el-tooltip>
        <el-tooltip :content="isFavorite ? '取消收藏' : '收藏'" placement="bottom">
          <el-button :type="isFavorite ? 'success' : 'info'" :icon="isFavorite ? StarFilled : Star"
            @click="handleFavorite" circle />
        </el-tooltip>
        <el-tooltip content="分享" placement="bottom">
          <el-button type="info" :icon="Share" @click="handleShare" circle />
        </el-tooltip>
        <el-tooltip content="版本管理" placement="bottom" v-if="isOwner">
          <el-button :icon="Document" @click="showVersionDialog = true" circle />
        </el-tooltip>
        <el-tooltip content="编辑文档" placement="bottom" v-if="isOwner">
          <el-button :icon="Edit" @click="showEditDialog = true" circle />
        </el-tooltip>
        <el-tooltip content="重新生成知识图谱" placement="bottom" v-if="isOwner">
          <el-button type="warning" :icon="Document" @click="rebuildKnowledgeGraph" circle />
        </el-tooltip>
        <el-tooltip content="删除文档" placement="bottom" v-if="isOwner">
          <el-button type="danger" :icon="Delete" @click="handleDelete" circle />
        </el-tooltip>
      </div>
    </div>

    <div class="detail-content" v-if="document">
      <div class="main-info">
        <div class="content-card">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="完整内容" name="full">
              <div class="full-content">
                <div class="content-actions">
                  <el-button type="primary" size="small" circle @click="toggleFullscreen('full')">
                    <el-icon>
                      <FullScreen />
                    </el-icon>
                  </el-button>
                </div>
                <div ref="fullContentRef" class="full-content-inner">
                  <v-md-preview :text="document.parsedContent || '暂无内容'"></v-md-preview>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="在线预览" name="preview">
              <div class="preview-container">
                <div class="content-actions">
                  <el-button type="primary" size="small" circle @click="toggleFullscreen('preview')">
                    <el-icon>
                      <FullScreen />
                    </el-icon>
                  </el-button>
                </div>
                <div v-if="!document.storagePath" class="preview-empty">
                  <el-icon>
                    <DocumentRemove />
                  </el-icon>
                  <span>暂无文件预览</span>
                </div>
                <div v-else-if="!document.previewUrl" class="preview-unsupported">
                  <el-icon>
                    <Warning />
                  </el-icon>
                  <span>该文件格式不支持在线预览，请下载后查看</span>
                  <el-button type="primary" @click="handleDownload">下载文档</el-button>
                </div>
                <div v-else ref="previewContentRef" class="preview-content">
                  <iframe :src="document.previewUrl" class="preview-iframe" frameborder="0" allowfullscreen></iframe>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="分块内容" name="chunks" v-if="document.chunks?.length">
              <div class="content-list">
                <div v-for="chunk in document.chunks" :key="chunk.id" class="content-chunk">
                  <div class="chunk-header">
                    <el-tag size="small" :type="getChunkType(chunk.contentType)">
                      {{ chunk.contentType }}
                    </el-tag>
                    <span class="chunk-index">第 {{ chunk.chunkIndex + 1 }} 块</span>
                  </div>
                  <div class="chunk-content">{{ chunk.contentText }}</div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="文字" name="text" v-if="textChunks.length">
              <div class="content-list">
                <div v-for="chunk in textChunks" :key="chunk.id" class="content-chunk">
                  <div class="chunk-content">{{ chunk.contentText }}</div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="表格" name="table" v-if="tableChunks.length">
              <div class="content-list">
                <div v-for="chunk in tableChunks" :key="chunk.id" class="content-chunk">
                  <div class="chunk-content">{{ chunk.contentText }}</div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="知识图谱" name="kg">
              <div class="kg-container">
                <div class="content-actions">
                  <el-button type="primary" size="small" circle @click="toggleFullscreen('kg')">
                    <el-icon>
                      <FullScreen />
                    </el-icon>
                  </el-button>
                </div>
                <div v-if="loadingKg" class="kg-loading">
                  <el-icon class="is-loading">
                    <Loading />
                  </el-icon>
                  <span>加载知识图谱中...</span>
                </div>
                <div v-else-if="kgData.nodes && kgData.nodes.length" class="kg-content">
                  <div ref="kgContainer" class="kg-visualization"></div>
                </div>
                <div v-else class="kg-empty">
                  <el-icon>
                    <DocumentRemove />
                  </el-icon>
                  <span>暂无知识图谱数据</span>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>

      <div class="side-info">
        <div class="info-section">
          <div class="title-section">
            <div class="file-icon" :style="{ backgroundColor: getFileIcon(document.fileType).color + '20' }">
              <el-icon :size="24" :color="getFileIcon(document.fileType).color">
                <Document />
              </el-icon>
            </div>
            <div class="title-info">
              <h1 class="doc-title">{{ document.title }}</h1>
            </div>
          </div>
          <div class="meta-row">
            <div class="meta-tags">
              <el-tag>{{ document.fileType?.toUpperCase() }}</el-tag>
              <el-tag type="info">{{ formatFileSize(document.fileSize) }}</el-tag>
              <el-tag :type="document.isPublic ? 'success' : 'warning'">
                {{ document.isPublic ? '公开' : '私有' }}
              </el-tag>
            </div>
            <div class="category-row" v-if="document.category">
              <el-tag class="category-tag">{{ document.category }}</el-tag>
            </div>
          </div>

          <div class="stats-row">
            <div class="stat-item">
              <el-icon :size="20" color="#ff9800">
                <View />
              </el-icon>
              <span class="stat-value">{{ document.viewCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-icon :size="20" color="#52c41a">
                <Download />
              </el-icon>
              <span class="stat-value">{{ document.downloadCount || 0 }}</span>
            </div>
            <div class="stat-item">
              <el-icon :size="20" color="#faad14">
                <Star />
              </el-icon>
              <span class="stat-value">{{ document.avgRating?.toFixed(1) || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="info-section" v-if="document.topics?.length">
          <h4 class="side-title">关键词标签</h4>
          <div class="topic-tags">
            <el-tag v-for="topic in document.topics" :key="topic.id" size="small" class="topic-tag">
              {{ topic.topicValue }}
            </el-tag>
          </div>
        </div>

        <div class="info-section">
          <h4 class="side-title">文档信息</h4>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">上传时间</span>
              <span class="info-value">{{ formatDate(document.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">上传者</span>
              <span class="info-value">{{ document.uploadUserName || '未知' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">原始文件名</span>
              <span class="info-value" :title="document.originalFilename">
                {{ document.originalFilename }}
              </span>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h4 class="side-title">文档描述</h4>
          <p class="description-text">{{ document.description || '暂无描述' }}</p>
        </div>

        <div class="info-section" v-if="document.overallSimilarity !== undefined">
          <h4 class="side-title">相似度检测</h4>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">总体相似度</span>
              <span class="info-value" :class="getSimilarityClass(document.overallSimilarity)">
                {{ (document.overallSimilarity * 100).toFixed(1) }}%
              </span>
            </div>
            <div class="info-item">
              <span class="info-label">文本相似度</span>
              <span class="info-value" :class="getSimilarityClass(document.textSimilarity)">
                {{ (document.textSimilarity * 100).toFixed(1) }}%
              </span>
            </div>
            <div class="info-item" v-if="document.tableSimilarity !== undefined">
              <span class="info-label">表格相似度</span>
              <span class="info-value" :class="getSimilarityClass(document.tableSimilarity)">
                {{ (document.tableSimilarity * 100).toFixed(1) }}%
              </span>
            </div>
            <div class="info-item" v-if="document.formulaSimilarity !== undefined">
              <span class="info-label">公式相似度</span>
              <span class="info-value" :class="getSimilarityClass(document.formulaSimilarity)">
                {{ (document.formulaSimilarity * 100).toFixed(1) }}%
              </span>
            </div>
          </div>
        </div>

        <div class="info-section" v-if="document.aiProbability !== undefined">
          <h4 class="side-title">AI生成检测</h4>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">AI生成概率</span>
              <span class="info-value" :class="getAIProbabilityClass(document.aiProbability)">
                {{ (document.aiProbability * 100).toFixed(1) }}%
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showRateDialog" title="文档评分" width="400px">
      <div class="rate-content">
        <p class="rate-title">{{ document?.title }}</p>
        <el-rate v-model="ratingValue" :colors="['#99A9BF', '#F7BA2A', '#FF9900']" show-text size="large" />
      </div>
      <template #footer>
        <el-button @click="showRateDialog = false">取消</el-button>
        <el-button type="warning" @click="submitRating">确认评分</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="编辑文档" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" placeholder="请输入文档标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="4" placeholder="请输入文档描述" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="editForm.category" placeholder="请输入文档分类" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="editForm.tags" multiple filterable allow-create default-first-option
            placeholder="请输入或选择标签" style="width: 100%">
            <el-option v-for="tag in editForm.existingTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
        <el-form-item label="公开状态">
          <el-switch v-model="editForm.isPublic" active-text="公开" inactive-text="私有"
            style="--el-switch-on-color: #fa8c16" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="warning" @click="handleEdit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showVersionDialog" title="版本管理" width="600px">
      <div class="version-management">
        <div class="version-actions" style="margin-bottom: 20px;">
          <el-button type="primary" @click="showUploadVersionDialog = true"
            style="background-color: #fa8c16; border-color: #fa8c16;">
            上传新版本
          </el-button>
        </div>
        <el-table :data="versions" style="width: 100%">
          <el-table-column prop="versionNumber" label="版本号" width="100" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="originalFilename" label="文件名" />
          <el-table-column prop="fileSize" label="大小" width="120">
            <template #default="scope">
              {{ formatFileSize(scope.row.fileSize) }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="上传时间" width="200">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button size="small" type="warning" plain @click="switchVersion(scope.row.versionNumber)">
                查看
              </el-button>
              <el-button size="small" type="warning" plain @click="handleDownloadVersion(scope.row.versionNumber)">
                下载
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="showUploadVersionDialog" title="上传新版本" width="500px">
      <div class="upload-version">
        <el-upload class="upload-demo" :auto-upload="false" :on-change="(file) => uploadFile = file"
          :show-file-list="false" accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt">
          <el-button type="primary" style="width: 100%; background-color: #fa8c16; border-color: #fa8c16;">
            选择文件
          </el-button>
        </el-upload>
        <div v-if="uploadFile" class="file-info" style="margin-top: 16px;">
          <p>{{ uploadFile.name }}</p>
          <p>{{ formatFileSize(uploadFile.size) }}</p>
        </div>
        <el-progress :percentage="uploadProgress" :status="uploadProgress === 100 ? 'success' : ''"
          style="margin-top: 16px;" />
      </div>
      <template #footer>
        <el-button @click="showUploadVersionDialog = false">取消</el-button>
        <el-button type="primary" @click="handleUploadVersion"
          style="background-color: #fa8c16; border-color: #fa8c16;">
          上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, Download, Star, Edit, Delete, Document, View, StarFilled, Share, Loading, DocumentRemove, Warning, FullScreen, SwitchButton
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDocumentKnowledgeGraph, rebuildDocumentKnowledgeGraph } from '@/api/document'
import {
  getDocumentDetail, downloadDocument, rateDocument, updateDocumentMetadata, deleteDocument,
  favoriteDocument, unfavoriteDocument, shareDocument, getAllCategories, getDocumentCategories, setDocumentCategories,
  uploadNewVersion, getDocumentVersions, getDocumentVersionDetail, downloadDocumentVersion,
  formatFileSize, formatDate, getFileIcon
} from '@/api/document'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const document = ref(null)
const showRateDialog = ref(false)
const showEditDialog = ref(false)
const showVersionDialog = ref(false)
const showUploadVersionDialog = ref(false)
const ratingValue = ref(5)
const activeTab = ref('full')
const isFavorite = ref(false)
const categories = ref([])
const selectedCategories = ref([])
const versions = ref([])
const currentVersion = ref(1)
const uploadProgress = ref(0)
const uploadFile = ref(null)
const loadingKg = ref(false)
const kgData = ref({ nodes: [], links: [] })
const kgChart = ref(null)
const kgContainer = ref(null)
const fullContentRef = ref(null)
const kgPollingTimer = ref(null)
const previewContentRef = ref(null)
const isFullscreen = ref(false)
const currentFullscreenType = ref(null)

const editForm = reactive({
  title: '',
  description: '',
  isPublic: true,
  category: '',
  tags: [],
  existingTags: []
})

const isOwner = computed(() => {
  const authInfo = localStorage.getItem('authorize') || sessionStorage.getItem('authorize')
  if (!authInfo || !document.value) return false
  const auth = JSON.parse(authInfo)
  return auth.userId === document.value.uploadUserId
})

const textChunks = computed(() => {
  return document.value?.chunks?.filter(c => c.contentType === 'text') || []
})

const tableChunks = computed(() => {
  return document.value?.chunks?.filter(c => c.contentType === 'table') || []
})

const isPreviewSupported = computed(() => {
  if (!document.value?.fileType) return false
  const supportedTypes = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx']
  return supportedTypes.includes(document.value.fileType.toLowerCase())
})

// 监听tab切换，加载知识图谱
watch(activeTab, (newTab) => {
  if (newTab === 'kg' && document.value) {
    loadKnowledgeGraph()
  }
})

onMounted(() => {
  loadDocument()
})

function loadDocument() {
  const id = route.params.id
  if (!id) return

  loading.value = true
  getDocumentDetail(id, (data) => {
    document.value = data
    editForm.title = data.title
    editForm.description = data.description
    editForm.isPublic = data.isPublic === 1
    editForm.category = data.category || ''
    editForm.tags = data.topics ? data.topics.map(t => t.topicValue) : []
    editForm.existingTags = data.topics ? data.topics.map(t => t.topicValue) : []
    currentVersion.value = data.currentVersion || 1
    isFavorite.value = data.isFavorite || false
    loadCategories()
    loadVersions()
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载失败')
    loading.value = false
  })
}

function loadVersions() {
  const id = route.params.id
  if (!id) return

  getDocumentVersions(id, (data) => {
    versions.value = data
  }, (msg) => {
    console.error('加载版本列表失败:', msg)
  })
}

function switchVersion(versionNumber) {
  const id = route.params.id
  if (!id) return

  loading.value = true
  getDocumentVersionDetail(id, versionNumber, (data) => {
    document.value = data
    currentVersion.value = versionNumber
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载版本失败')
    loading.value = false
  })
}

function handleUploadVersion() {
  if (!uploadFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  const id = route.params.id
  if (!id) return

  loading.value = true
  uploadProgress.value = 0
  uploadNewVersion(id, uploadFile.value, (progress) => {
    uploadProgress.value = progress
  }, (data) => {
    ElMessage.success('版本上传成功')
    showUploadVersionDialog.value = false
    uploadFile.value = null
    loadDocument()
  }, (msg) => {
    ElMessage.error(msg || '版本上传失败')
    loading.value = false
  })
}

function handleDownloadVersion(versionNumber) {
  const id = route.params.id
  if (!id) return
  downloadDocumentVersion(id, versionNumber)
}

function loadCategories() {
  // 加载所有分类
  getAllCategories((data) => {
    categories.value = data
    // 加载文档的分类
    getDocumentCategories(document.value.id, (categoryIds) => {
      selectedCategories.value = categoryIds
    })
  })
}

function saveCategories() {
  setDocumentCategories(document.value.id, selectedCategories.value, () => {
    ElMessage.success('分类设置成功')
  }, (msg) => {
    ElMessage.error(msg || '分类设置失败')
  })
}

function goBack() {
  router.back()
}

function handleDownload() {
  downloadDocument(document.value.id)
}

function submitRating() {
  rateDocument(document.value.id, ratingValue.value, () => {
    ElMessage.success('评分成功')
    showRateDialog.value = false
    loadDocument()
  }, (msg) => {
    ElMessage.error(msg || '评分失败')
  })
}

function handleEdit() {
  updateDocumentMetadata({
    id: document.value.id,
    title: editForm.title,
    description: editForm.description,
    isPublic: editForm.isPublic ? 1 : 0,
    category: editForm.category,
    tags: editForm.tags
  }, () => {
    ElMessage.success('更新成功')
    showEditDialog.value = false
    loadDocument()
  }, (msg) => {
    ElMessage.error(msg || '更新失败')
  })
}

function handleDelete() {
  ElMessageBox.confirm('确定要删除这个文档吗？删除后无法恢复。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    deleteDocument(document.value.id, () => {
      ElMessage.success('删除成功')
      router.push('/doc/list')
    }, (msg) => {
      ElMessage.error(msg || '删除失败')
    })
  }).catch(() => { })
}

function handleFavorite() {
  if (isFavorite.value) {
    // 取消收藏
    unfavoriteDocument(document.value.id, () => {
      ElMessage.success('取消收藏成功')
      isFavorite.value = false
      loadDocument()
    }, (msg) => {
      ElMessage.error(msg || '取消收藏失败')
    })
  } else {
    // 收藏
    favoriteDocument(document.value.id, () => {
      ElMessage.success('收藏成功')
      isFavorite.value = true
      loadDocument()
    }, (msg) => {
      ElMessage.error(msg || '收藏失败')
    })
  }
}

function handleShare() {
  shareDocument(document.value.id, (shareUrl) => {
    // 复制分享链接到剪贴板
    navigator.clipboard.writeText(shareUrl).then(() => {
      ElMessage.success('分享链接已复制到剪贴板')
    }).catch(() => {
      ElMessage.error('复制失败，请手动复制')
    })
  }, (msg) => {
    ElMessage.error(msg || '生成分享链接失败')
  })
}

function loadKnowledgeGraph() {
  const id = route.params.id
  if (!id) return

  loadingKg.value = true
  // 清除之前的轮询
  if (kgPollingTimer.value) {
    clearInterval(kgPollingTimer.value)
    kgPollingTimer.value = null
  }

  // 从后端API获取知识图谱数据
  getDocumentKnowledgeGraph(id,
    (data) => {
      kgData.value = data
      loadingKg.value = false

      // 如果数据为空，启动轮询检测知识图谱是否生成
      if (!data.nodes || data.nodes.length === 0) {
        let pollingCount = 0
        const maxPollingCount = 30 // 最多轮询30次（约30秒）

        kgPollingTimer.value = setInterval(() => {
          pollingCount++
          getDocumentKnowledgeGraph(id,
            (pollingData) => {
              if (pollingData.nodes && pollingData.nodes.length > 0) {
                // 知识图谱已生成，停止轮询
                clearInterval(kgPollingTimer.value)
                kgPollingTimer.value = null
                kgData.value = pollingData
                nextTick(() => {
                  renderKnowledgeGraph()
                })
              } else if (pollingCount >= maxPollingCount) {
                // 达到最大轮询次数，停止轮询
                clearInterval(kgPollingTimer.value)
                kgPollingTimer.value = null
              }
            },
            () => {
              // 轮询失败，继续轮询
            }
          )
        }, 1000)
      } else {
        // 确保DOM更新后再渲染知识图谱
        nextTick(() => {
          renderKnowledgeGraph()
        })
      }
    },
    (error) => {
      console.error('获取知识图谱失败:', error)
      loadingKg.value = false
      ElMessage.error('获取知识图谱失败，请重试')
      // 使用默认数据作为回退
      kgData.value = {
        nodes: [
          { id: 1, label: '人工智能', type: 'CONCEPT' },
          { id: 2, label: '机器学习', type: 'CONCEPT' },
          { id: 3, label: '深度学习', type: 'CONCEPT' },
          { id: 4, label: '自然语言处理', type: 'CONCEPT' },
          { id: 5, label: 'AI', type: 'TECHNOLOGY' }
        ],
        links: [
          { source: 1, target: 2, label: '包含' },
          { source: 2, target: 3, label: '包含' },
          { source: 1, target: 4, label: '包含' },
          { source: 1, target: 5, label: '简称' }
        ]
      }
      nextTick(() => {
        renderKnowledgeGraph()
      })
    }
  )
}

function rebuildKnowledgeGraph() {
  const id = route.params.id
  if (!id) return

  ElMessageBox.confirm('确定要重新构建知识图谱吗？这将会覆盖现有的知识图谱数据。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    loadingKg.value = true
    rebuildDocumentKnowledgeGraph(id,
      () => {
        ElMessage.success('知识图谱重新构建成功')
        // 重新加载知识图谱
        loadKnowledgeGraph()
      },
      (error) => {
        console.error('重新构建知识图谱失败:', error)
        loadingKg.value = false
        ElMessage.error('重新构建知识图谱失败，请重试')
      }
    )
  }).catch(() => { })
}

function renderKnowledgeGraph() {
  const container = kgContainer.value
  if (!container) return

  // 销毁旧的图表实例
  if (kgChart.value) {
    kgChart.value.dispose()
  }

  // 创建新的图表实例
  kgChart.value = echarts.init(container)

  const nodes = kgData.value.nodes
  const links = kgData.value.links

  // 准备ECharts数据
  const echartsNodes = nodes.map(node => ({
    id: node.id.toString(),
    name: node.label,
    symbolSize: 50,
    value: node.label,
    category: node.type === 'CONCEPT' ? 0 : 1,
    itemStyle: {
      // 使用系统主题色
      color: node.type === 'CONCEPT' ? '#fa8c16' : '#f56c6c'
    }
  }))

  const echartsLinks = links.map(link => ({
    source: link.source.toString(),
    target: link.target.toString(),
    label: {
      show: true,
      formatter: link.label
    }
  }))

  const option = {
    title: {
      text: '',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: 'bottom',
      data: [
        { name: '概念', itemStyle: { color: '#fa8c16' } },
        { name: '技术', itemStyle: { color: '#f56c6c' } }
      ]
    },
    animationDurationUpdate: 1500,
    animationEasingUpdate: 'quinticInOut',
    series: [
      {
        type: 'graph',
        layout: 'force',
        data: echartsNodes,
        links: echartsLinks,
        categories: [
          {
            name: '概念'
          },
          {
            name: '技术'
          }
        ],
        roam: true,
        label: {
          show: true,
          position: 'inside',
          formatter: '{b}',
          color: '#fff'
        },
        lineStyle: {
          color: 'source',
          curveness: 0.3
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 4
          }
        },
        force: {
          repulsion: 1000,
          edgeLength: [80, 120]
        }
      }
    ]
  }

  // 渲染图表
  kgChart.value.setOption(option)

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  if (kgChart.value) {
    kgChart.value.resize()
  }
}

onUnmounted(() => {
  // 清理图表实例
  if (kgChart.value) {
    kgChart.value.dispose()
  }
  // 清除知识图谱轮询
  if (kgPollingTimer.value) {
    clearInterval(kgPollingTimer.value)
    kgPollingTimer.value = null
  }
  // 移除事件监听器
  window.removeEventListener('resize', handleResize)
})

function getChunkType(type) {
  const typeMap = {
    'text': '',
    'table': 'success',
    'formula': 'warning',
    'image': 'info'
  }
  return typeMap[type] || ''
}

function getSimilarityClass(similarity) {
  if (similarity >= 0.8) return 'similarity-high'
  if (similarity >= 0.5) return 'similarity-medium'
  return 'similarity-low'
}

function getAIProbabilityClass(aiProbability) {
  if (aiProbability >= 0.8) return 'ai-high'
  if (aiProbability >= 0.5) return 'ai-medium'
  return 'ai-low'
}

function toggleFullscreen(type) {
  if (isFullscreen.value && currentFullscreenType.value === type) {
    // 退出全屏
    if (document.exitFullscreen) {
      document.exitFullscreen()
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen()
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen()
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen()
    }
    isFullscreen.value = false
    currentFullscreenType.value = null
  } else {
    // 进入全屏
    let element = null
    if (type === 'full' && fullContentRef.value) {
      element = fullContentRef.value
    } else if (type === 'preview' && document.value?.previewUrl && previewContentRef.value) {
      element = previewContentRef.value
    } else if (type === 'kg' && kgContainer.value) {
      element = kgContainer.value
    }

    if (element) {
      if (element.requestFullscreen) {
        element.requestFullscreen()
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen()
      } else if (element.mozRequestFullScreen) {
        element.mozRequestFullScreen()
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen()
      }
      isFullscreen.value = true
      currentFullscreenType.value = type
    }
  }
}
</script>

<style scoped>
.document-detail {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: white;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.header-actions {
  display: flex;
  gap: 8px;
}

.button-text {
  margin-left: 8px;
}

.detail-content {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
}

.main-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.info-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.title-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.file-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.title-info {
  flex: 1;
}

.doc-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
}

.meta-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}

.meta-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.category-row {
  display: flex;
  gap: 6px;
}

.category-tag {
  background-color: #fa8c16;
  color: white;
  border-color: #fa8c16;
}

.topic-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.topic-tag {
  background-color: #fff7e6;
  color: #fa8c16;
  border-color: #ffd591;
  font-size: 12px;
}

.stats-row {
  display: flex;
  gap: 20px;
  padding-top: 20px;
  margin-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.description-card,
.content-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.content-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.full-content-inner {
  position: relative;
  max-height: 600px;
  overflow-y: auto;
}

.content-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

.content-actions .el-button {
  margin-left: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 全屏样式 */
.full-content-inner:-webkit-full-screen {
  max-height: 100vh;
  width: 100vw;
  background: white;
  z-index: 9999;
  position: fixed;
  top: 0;
  left: 0;
  padding: 20px;
}

.preview-content:-webkit-full-screen {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background: white;
  z-index: 9999;
  padding: 20px;
}

.preview-content:-webkit-full-screen .preview-iframe {
  width: 100%;
  height: 100%;
}

.kg-visualization:-webkit-full-screen {
  width: 100vw;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background: white;
  z-index: 9999;
  padding: 20px;
}

:-webkit-full-screen {
  background: white;
  z-index: 9999;
}

:-ms-fullscreen {
  background: white;
  z-index: 9999;
}

:fullscreen {
  background: white;
  z-index: 9999;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.description-text {
  font-size: 14px;
  color: #666;
  line-height: 1.8;
  margin: 0;
}

.content-list {
  min-height: 70vh;
  max-height: 80vh;
  overflow-y: auto;
}

.content-actions {
  margin-bottom: 16px;
  margin-right: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.edit-action-buttons {
  display: flex;
  gap: 8px;
}

.full-content {
  min-height: 600px;
  max-height: 800px;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.content-text {
  color: #333;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}

.content-chunk {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 12px;
}

.chunk-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.chunk-index {
  font-size: 12px;
  color: #999;
}

.chunk-content {
  color: #333;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-all;
}

.side-info {
  width: 200px;
  flex-shrink: 0;
  overflow-y: auto;
  max-height: calc(100vh - 140px);
}

.info-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.side-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #999;
}

.info-value {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-tag {
  background: linear-gradient(135deg, #fff0e6, #ffe4cc);
  border-color: #ffcc80;
  color: #e65100;
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

/* 全局样式：覆盖 Element Plus 默认样式 */
:deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 1px #fa8c16 inset;
}

:deep(.el-textarea__inner:focus) {
  border-color: #fa8c16;
  box-shadow: 0 0 0 2px rgba(250, 140, 22, 0.2);
}

:deep(.el-select .el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #fa8c16 inset;
}

:deep(.el-tabs__item.is-active) {
  color: #fa8c16;
}

:deep(.el-tabs__active-bar) {
  background-color: #fa8c16;
}

:deep(.el-tabs__item:hover) {
  color: #fa8c16;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #fa8c16;
  border-color: #fa8c16;
}

.similarity-high {
  color: #52c41a;
  font-weight: 600;
}

.similarity-medium {
  color: #faad14;
  font-weight: 600;
}

.similarity-low {
  color: #f5222d;
  font-weight: 600;
}

.ai-high {
  color: #f5222d;
  font-weight: 600;
}

.ai-medium {
  color: #faad14;
  font-weight: 600;
}

.ai-low {
  color: #52c41a;
  font-weight: 600;
}

.kg-container {
  position: relative;
  min-height: 70vh;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.kg-container .content-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

.kg-container .content-actions .el-button {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.kg-loading {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 14px;
}

.kg-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 14px;
}

.kg-empty .el-icon {
  font-size: 48px;
  color: #d9d9d9;
}

.kg-content {
  width: 100%;
  height: 560px;
  position: relative;
}

.kg-visualization {
  width: 100%;
  height: 100%;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background-color: #fafafa;
}

.preview-container {
  position: relative;
  width: 100%;
  min-height: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.preview-container .content-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
}

.preview-container .content-actions .el-button {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.preview-empty,
.preview-unsupported {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #999;
  font-size: 14px;
}

.preview-empty .el-icon,
.preview-unsupported .el-icon {
  font-size: 48px;
  color: #d9d9d9;
}

.preview-unsupported {
  gap: 16px;
}

.preview-content {
  width: 100%;
  height: 800px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
}
</style>
