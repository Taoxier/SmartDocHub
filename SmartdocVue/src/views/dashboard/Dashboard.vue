<template>
  <div class="dashboard">
    <div class="welcome-section">
      <div class="welcome-text">
        <h1>欢迎回来，{{ userName }}！</h1>
        <p>今天也要加油学习哦~</p>
      </div>
      <div class="quick-actions">
        <el-button type="primary" :icon="Upload" @click="goToUpload">上传文档</el-button>
        <el-button type="warning" :icon="Search" @click="goToSearch">搜索文档</el-button>
      </div>
    </div>

    <!-- 为你推荐 -->
    <div class="section-container">
      <div class="section-header">
        <h2>为你推荐</h2>
        <el-button type="primary" plain size="small" :icon="Refresh" @click="refreshRecommended"
          :loading="loadingRecommended">换一批</el-button>
      </div>
      <div class="recommend-grid" v-loading="loadingRecommended">
        <div v-for="doc in recommendedDocs" :key="doc.id" class="recommend-card" @click="goToDetail(doc.id)">
          <div class="card-badge recommend-badge">推荐</div>
          <div class="card-left">
            <div class="card-top">
              <div class="card-icon" :style="{ backgroundColor: getFileIcon(doc.fileType).color + '20' }">
                <el-icon :size="32" :color="getFileIcon(doc.fileType).color">
                  <Document />
                </el-icon>
              </div>
              <h4 class="card-title">{{ doc.title }}</h4>
            </div>
            <p class="card-desc">{{ doc.description || '暂无描述' }}</p>
            <div class="card-rating">
              <el-rate :model-value="doc.avgRating || 0" disabled size="small" />
              <div class="card-views">
                <el-icon>
                  <View />
                </el-icon>
                <span>{{ doc.viewCount || 0 }}</span>
              </div>
              <div class="card-downloads">
                <el-icon>
                  <Download />
                </el-icon>
                <span>{{ doc.downloadCount || 0 }}</span>
              </div>
            </div>
          </div>
          <div class="card-stats-right" v-if="doc.overallSimilarity !== undefined || doc.aiProbability !== undefined">
            <div class="stat-item" v-if="doc.overallSimilarity !== undefined">
              <span class="stat-label">相似度</span>
              <span class="stat-value" :class="getSimilarityClass(doc.overallSimilarity)">{{ (doc.overallSimilarity *
                100).toFixed(1) }}%</span>
            </div>
            <div class="stat-item" v-if="doc.aiProbability !== undefined">
              <span class="stat-label">AI率</span>
              <span class="stat-value" :class="getAIProbabilityClass(doc.aiProbability)">{{ (doc.aiProbability *
                100).toFixed(1) }}%</span>
            </div>
          </div>
        </div>
        <el-empty v-if="recommendedDocs.length === 0 && !loadingRecommended" description="暂无推荐内容"
          class="empty-wrapper" />
      </div>
    </div>

    <!-- 三个并列卡片区 -->
    <div class="three-columns">
      <!-- 原创力荐 -->
      <div class="column-card original-section">
        <div class="column-header">
          <span class="column-icon">🌟</span>
          <h3>原创力荐</h3>
        </div>
        <div class="column-content" v-loading="loadingOriginal">
          <div v-for="doc in originalDocs" :key="doc.id" class="doc-row-item" @click="goToDetail(doc.id)">
            <div class="doc-row-left">
              <div class="doc-row-icon" :style="{ backgroundColor: getFileIcon(doc.fileType).color + '20' }">
                <el-icon :color="getFileIcon(doc.fileType).color">
                  <Document />
                </el-icon>
              </div>
              <div class="doc-row-info">
                <div class="doc-row-title">{{ doc.title }}</div>
                <div class="doc-row-meta">
                  <span class="original-badge">🌟 原创</span>
                  <el-rate :model-value="getOriginalScore(doc)" disabled size="small" />
                </div>
              </div>
            </div>
            <div class="doc-row-stats-right"
              v-if="doc.overallSimilarity !== undefined || doc.aiProbability !== undefined">
              <div class="stat-item" v-if="doc.overallSimilarity !== undefined">
                <span class="stat-label">相似度</span>
                <span class="stat-value" :class="getSimilarityClass(doc.overallSimilarity)">{{ (doc.overallSimilarity *
                  100).toFixed(1) }}%</span>
              </div>
              <div class="stat-item" v-if="doc.aiProbability !== undefined">
                <span class="stat-label">AI率</span>
                <span class="stat-value" :class="getAIProbabilityClass(doc.aiProbability)">{{ (doc.aiProbability *
                  100).toFixed(1) }}%</span>
              </div>
            </div>
          </div>
          <el-empty v-if="originalDocs.length === 0 && !loadingOriginal" description="暂无原创文档" />
        </div>
      </div>

      <!-- 热门文档 -->
      <div class="column-card">
        <div class="column-header">
          <span class="column-icon">🔥</span>
          <h3>热门文档</h3>
          <el-button text type="primary" @click="goToDocuments('viewCount')" class="more-btn">查看更多</el-button>
        </div>
        <div class="column-content">
          <div v-for="doc in hotDocs" :key="doc.id" class="doc-row-item" @click="goToDetail(doc.id)">
            <div class="doc-row-left">
              <div class="doc-row-icon" :style="{ backgroundColor: getFileIcon(doc.fileType).color + '20' }">
                <el-icon :color="getFileIcon(doc.fileType).color">
                  <Document />
                </el-icon>
              </div>
              <div class="doc-row-info">
                <div class="doc-row-title">{{ doc.title }}</div>
                <div class="doc-row-meta">
                  <span>{{ doc.viewCount || 0 }} 次浏览</span>
                </div>
              </div>
            </div>
            <div class="doc-row-stats-right"
              v-if="doc.overallSimilarity !== undefined || doc.aiProbability !== undefined">
              <div class="stat-item" v-if="doc.overallSimilarity !== undefined">
                <span class="stat-label">相似度</span>
                <span class="stat-value" :class="getSimilarityClass(doc.overallSimilarity)">{{ (doc.overallSimilarity *
                  100).toFixed(1) }}%</span>
              </div>
              <div class="stat-item" v-if="doc.aiProbability !== undefined">
                <span class="stat-label">AI率</span>
                <span class="stat-value" :class="getAIProbabilityClass(doc.aiProbability)">{{ (doc.aiProbability *
                  100).toFixed(1) }}%</span>
              </div>
            </div>
          </div>
          <el-empty v-if="hotDocs.length === 0" description="暂无热门文档" />
        </div>
      </div>

      <!-- 最新文档 -->
      <div class="column-card">
        <div class="column-header">
          <span class="column-icon">🕒</span>
          <h3>最新文档</h3>
          <el-button text type="primary" @click="goToDocuments" class="more-btn">查看更多</el-button>
        </div>
        <div class="column-content">
          <div v-for="doc in recentDocs" :key="doc.id" class="doc-row-item" @click="goToDetail(doc.id)">
            <div class="doc-row-left">
              <div class="doc-row-icon" :style="{ backgroundColor: getFileIcon(doc.fileType).color + '20' }">
                <el-icon :color="getFileIcon(doc.fileType).color">
                  <Document />
                </el-icon>
              </div>
              <div class="doc-row-info">
                <div class="doc-row-title">{{ doc.title }}</div>
                <div class="doc-row-meta">
                  <span>{{ formatDate(doc.createTime) }}</span>
                </div>
              </div>
            </div>
            <div class="doc-row-stats-right"
              v-if="doc.overallSimilarity !== undefined || doc.aiProbability !== undefined">
              <div class="stat-item" v-if="doc.overallSimilarity !== undefined">
                <span class="stat-label">相似度</span>
                <span class="stat-value" :class="getSimilarityClass(doc.overallSimilarity)">{{ (doc.overallSimilarity *
                  100).toFixed(1) }}%</span>
              </div>
              <div class="stat-item" v-if="doc.aiProbability !== undefined">
                <span class="stat-label">AI率</span>
                <span class="stat-value" :class="getAIProbabilityClass(doc.aiProbability)">{{ (doc.aiProbability *
                  100).toFixed(1) }}%</span>
              </div>
            </div>
          </div>
          <el-empty v-if="recentDocs.length === 0" description="暂无最新文档" />
        </div>
      </div>
    </div>

    <!-- 知识图谱探索 -->
    <div class="section-container kg-section">
      <div class="section-header">
        <h2>🧠 知识图谱探索</h2>
        <el-button type="primary" @click="goToKgExplore">探索完整图谱</el-button>
      </div>
      <div class="kg-preview" v-loading="loadingKg">
        <div ref="kgChartRef" class="kg-chart"></div>
        <el-empty v-if="kgData.nodes.length === 0 && !loadingKg" description="暂无知识图谱数据" />
      </div>
    </div>

    <!-- 热门标签云 -->
    <div class="section-container tags-section">
      <div class="section-header">
        <h2>🏷️ 热门标签</h2>
      </div>
      <div class="tag-cloud" v-loading="loadingTags">
        <el-tag v-for="(tag, index) in hotTags" :key="tag.topicValue" :size="getTagSize(index)"
          :color="tagColors[index % tagColors.length]" class="hot-tag" @click="searchByTag(tag.topicValue)">
          {{ tag.topicValue }}
        </el-tag>
        <el-empty v-if="hotTags.length === 0 && !loadingTags" description="暂无标签数据" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Document, Upload, Search, Refresh, View, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { get } from '@/net'
import { formatDate, getFileIcon, getRecommendedDocuments, getDocumentsByIds } from '@/api/document'

const router = useRouter()

const recentDocs = ref([])
const hotDocs = ref([])
const recommendedDocs = ref([])
const recommendOffset = ref(0)
const originalDocs = ref([])
const hotTags = ref([])
const kgData = ref({ nodes: [], links: [] })

const loadingRecommended = ref(false)
const loadingOriginal = ref(false)
const loadingKg = ref(false)
const loadingTags = ref(false)

const kgChartRef = ref(null)
let kgChart = null

const tagColors = ['#ff9800', '#4caf50', '#2196f3', '#9c27b0', '#ff5722', '#00bcd4', '#e91e63', '#8bc34a']

const userName = computed(() => {
  const userInfo = localStorage.getItem('authorize') || sessionStorage.getItem('authorize')
  if (userInfo) {
    const user = JSON.parse(userInfo)
    return user.username || '用户'
  }
  return '用户'
})

onMounted(() => {
  loadRecommendedDocs()
  loadOriginalDocs()
  loadHotDocs()
  loadRecentDocs()
  loadHotTags()
  loadKgPreview()
})

onUnmounted(() => {
  if (kgChart) {
    kgChart.dispose()
  }
})

function loadRecommendedDocs() {
  const userInfo = localStorage.getItem('authorize') || sessionStorage.getItem('authorize')
  if (!userInfo) {
    return
  }

  loadingRecommended.value = true
  const user = JSON.parse(userInfo)
  const userId = user.userId

  getRecommendedDocuments(userId, 4, 0, false, (data) => {
    if (data && data.length > 0) {
      getDocumentsByIds(data, (docData) => {
        recommendedDocs.value = docData || []
        loadingRecommended.value = false
      }, () => {
        loadingRecommended.value = false
      })
    } else {
      loadingRecommended.value = false
    }
  }, () => {
    loadingRecommended.value = false
  })
}

function refreshRecommended() {
  const userInfo = localStorage.getItem('authorize') || sessionStorage.getItem('authorize')
  if (!userInfo) {
    return
  }

  loadingRecommended.value = true
  const user = JSON.parse(userInfo)
  const userId = user.userId
  recommendOffset.value += 4

  getRecommendedDocuments(userId, 4, recommendOffset.value, false, (data) => {
    if (data && data.length > 0) {
      getDocumentsByIds(data, (docData) => {
        recommendedDocs.value = docData || []
        loadingRecommended.value = false
      }, () => {
        loadingRecommended.value = false
      })
    } else {
      recommendOffset.value = 0
      getRecommendedDocuments(userId, 4, 0, true, (newData) => {
        if (newData && newData.length > 0) {
          getDocumentsByIds(newData, (docData) => {
            recommendedDocs.value = docData || []
            loadingRecommended.value = false
          }, () => {
            loadingRecommended.value = false
          })
        } else {
          loadingRecommended.value = false
        }
      }, () => {
        loadingRecommended.value = false
      })
    }
  }, () => {
    loadingRecommended.value = false
  })
}

function loadOriginalDocs() {
  loadingOriginal.value = true
  get('/api/document/original?limit=6', (data) => {
    originalDocs.value = data || []
    loadingOriginal.value = false
  }, () => {
    loadingOriginal.value = false
  })
}

function loadHotDocs() {
  get('/api/document/page?pageNum=1&pageSize=6&sortBy=viewCount&sortOrder=desc', (data) => {
    hotDocs.value = data.list || []
  }, (err) => {
    console.error('加载热门文档失败:', err)
  })
}

function loadRecentDocs() {
  get('/api/document/page?pageNum=1&pageSize=6&sortBy=createTime&sortOrder=desc', (data) => {
    recentDocs.value = data.list || []
  }, (err) => {
    console.error('加载最新文档失败:', err)
  })
}

function loadHotTags() {
  loadingTags.value = true
  get('/api/topic/hot?limit=30', (data) => {
    if (data && data.length > 0) {
      hotTags.value = data.map(item => ({
        topicValue: item.topic_value,
        count: item.cnt
      }))
    } else {
      hotTags.value = []
    }
    loadingTags.value = false
  }, () => {
    loadingTags.value = false
  })
}

function loadKgPreview() {
  loadingKg.value = true
  get('/api/kg/preview?nodeLimit=15&relationLimit=30', (data) => {
    kgData.value = data || { nodes: [], links: [] }
    nextTick(() => {
      initKgChart()
    })
    loadingKg.value = false
  }, () => {
    loadingKg.value = false
  })
}

function initKgChart() {
  if (!kgChartRef.value || kgData.value.nodes.length === 0) {
    return
  }

  if (kgChart) {
    kgChart.dispose()
  }

  kgChart = echarts.init(kgChartRef.value)

  const categories = [
    { name: '概念', color: '#ff9800' },
    { name: '实体', color: '#4caf50' },
    { name: '技术', color: '#2196f3' }
  ]

  const categoryMap = {
    'CONCEPT': 0,
    'ENTITY': 1,
    'TECH': 2,
    'TECHNOLOGY': 2
  }

  const nodes = kgData.value.nodes.map(node => ({
    id: node.id,
    name: node.name,
    category: categoryMap[node.category] ?? 0,
    value: node.value || 10,
    symbolSize: 30,
    itemStyle: {
      color: categories[categoryMap[node.category] ?? 0]?.color || '#999'
    }
  }))

  const links = kgData.value.links.map(link => ({
    source: link.source,
    target: link.target,
    label: { show: true, formatter: link.label || '' }
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (params.dataType === 'node') {
          return `节点: ${params.data.name}`
        }
        return `${params.data.source} → ${params.data.target}`
      }
    },
    legend: {
      data: categories.map(c => ({ name: c.name, itemStyle: { color: c.color } })),
      bottom: 10
    },
    series: [{
      type: 'graph',
      layout: 'force',
      animation: true,
      roam: true,
      draggable: true,
      label: {
        show: true,
        position: 'right',
        fontSize: 12,
        formatter: '{b}'
      },
      lineStyle: {
        color: '#999',
        width: 2
      },
      edgeLabel: {
        show: true,
        fontSize: 10,
        formatter: '{c}'
      },
      data: nodes,
      links: links,
      categories: categories,
      force: {
        repulsion: 200,
        edgeLength: 120,
        layoutAnimation: true
      }
    }]
  }

  kgChart.setOption(option)

  window.addEventListener('resize', () => {
    if (kgChart) {
      kgChart.resize()
    }
  })
}

function getOriginalScore(doc) {
  if (!doc.overallSimilarity || !doc.aiProbability) {
    return 0
  }
  const score = (1 - doc.overallSimilarity) * 0.6 + (1 - doc.aiProbability) * 0.4
  return Math.round(score * 5)
}

function getTagSize(index) {
  if (index < 5) return 'large'
  if (index < 15) return 'default'
  return 'small'
}

function searchByTag(tagValue) {
  router.push(`/search?tag=${encodeURIComponent(tagValue)}`)
}

function goToUpload() {
  router.push('/upload')
}

function goToSearch() {
  router.push('/search')
}

function goToDocuments(sortBy = 'createTime') {
  router.push(`/doc/list?sortBy=${sortBy}`)
}

function goToKgExplore() {
  router.push('/doc/kg-explore')
}

function goToDetail(id) {
  router.push(`/doc/detail/${id}`)
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
.dashboard {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
  background-color: #f5f7fa;
}

.welcome-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  background: linear-gradient(135deg, #ff9800, #ff5722);
  border-radius: 16px;
  margin-bottom: 24px;
  color: white;
}

.welcome-text h1 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.welcome-text p {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.quick-actions {
  display: flex;
  gap: 12px;
}

.section-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.recommend-card {
  position: relative;
  background: white;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #f0f0f0;
  display: flex;
  gap: 12px;
  min-height: 120px;
}

.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(255, 152, 0, 0.15);
  border-color: #ffcc80;
}

.card-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  min-height: 90px;
}

.card-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.card-stats-right {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
  padding-left: 12px;
  border-left: 1px solid #eee;
  margin-top: 30px;
}

.card-stats-right .stat-item {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.card-stats-right .stat-label {
  font-size: 11px;
  color: #999;
}

.card-stats-right .stat-value {
  font-size: 14px;
  font-weight: 600;
}

.card-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.recommend-badge {
  background: linear-gradient(135deg, #ff9800, #ff5722);
  color: white;
}

.card-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-all;
  flex: 1;
  min-width: 0;
}

.card-desc {
  font-size: 12px;
  color: #999;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex-shrink: 0;
  max-height: 33px;
}

.card-rating {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-views,
.card-downloads {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: #999;
}

.card-views .el-icon,
.card-downloads .el-icon {
  font-size: 12px;
}

.empty-wrapper {
  grid-column: 1 / -1;
}

.three-columns {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.column-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.original-section {
  border: 2px solid #4caf50;
}

.column-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.column-icon {
  font-size: 20px;
  margin-right: 8px;
}

.column-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
}

.more-btn {
  margin-left: auto;
}

.column-content {
  min-height: 280px;
}

.doc-row-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-bottom: 8px;
  border: 1px solid #f0f0f0;
}

.doc-row-item:hover {
  /* background-color: #fff7e6; */
  border-color: #ffcc80;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.1);
}

.doc-row-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.doc-row-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 14px;
  flex-shrink: 0;
}

.doc-row-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.doc-row-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
}

.doc-row-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
  font-size: 12px;
  color: #999;
}

.doc-row-stats-right {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-end;
  gap: 6px;
  padding-left: 14px;
  border-left: 1px solid #eee;
  min-width: 70px;
}

.doc-row-stats-right .stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.doc-row-stats-right .stat-label {
  font-size: 11px;
  color: #999;
}

.doc-row-stats-right .stat-value {
  font-size: 13px;
  font-weight: 600;
}

.original-badge {
  background: linear-gradient(135deg, #4caf50, #8bc34a);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
}

.kg-section {
  min-height: 400px;
}

.kg-preview {
  height: 350px;
  border-radius: 8px;
  overflow: hidden;
}

.kg-chart {
  width: 100%;
  height: 100%;
}

.tags-section {
  min-height: 150px;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: center;
  padding: 10px 0;
}

.hot-tag {
  cursor: pointer;
  transition: all 0.3s ease;
  color: white;
  border: none;
}

.hot-tag:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 统计信息样式 */
.card-stats {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}

.doc-row-stats {
  margin-top: 4px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 11px;
}

.stat-item {
  display: block;
}

/* 相似度颜色 */
.similarity-low {
  color: #4caf50;
  font-weight: 500;
}

.similarity-medium {
  color: #ff9800;
  font-weight: 500;
}

.similarity-high {
  color: #f44336;
  font-weight: 500;
}

/* AI率颜色 */
.ai-low {
  color: #4caf50;
  font-weight: 500;
}

.ai-medium {
  color: #ff9800;
  font-weight: 500;
}

.ai-high {
  color: #f44336;
  font-weight: 500;
}
</style>
