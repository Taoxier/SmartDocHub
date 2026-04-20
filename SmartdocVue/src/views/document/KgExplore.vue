<template>
  <div class="kg-explore" v-loading="loading">
    <div class="kg-container">
      <div class="kg-sidebar">
        <div class="sidebar-header">
          <el-input v-model="searchKeyword" placeholder="搜索节点..." :prefix-icon="Search" class="search-input" clearable
            @change="handleSearch" />
          <el-button type="primary" class="refresh-btn" @click="loadFullGraph">
            <el-icon>
              <Refresh />
            </el-icon>
            刷新
          </el-button>
        </div>

        <div class="sidebar-section">
          <h3 class="sidebar-title">节点类型</h3>
          <el-checkbox-group v-model="selectedCategories" @change="handleCategoryChange">
            <el-checkbox label="CONCEPT">概念</el-checkbox>
            <el-checkbox label="ENTITY">实体</el-checkbox>
            <el-checkbox label="TECHNOLOGY">技术</el-checkbox>
          </el-checkbox-group>
        </div>

        <div class="sidebar-section">
          <h3 class="sidebar-title">关系类型</h3>
          <div class="relation-types">
            <div v-for="relation in relationTypes" :key="relation" class="relation-item">
              <span class="relation-dot" :style="{ backgroundColor: getRelationColor(relation) }"></span>
              <span class="relation-name">{{ relation }}</span>
            </div>
          </div>
        </div>

        <div class="sidebar-section">
          <h3 class="sidebar-title">节点统计</h3>
          <div class="stats-info">
            <div class="stat-item">
              <span class="stat-label">节点总数:</span>
              <span class="stat-value">{{ allNodes.length }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">关系总数:</span>
              <span class="stat-value">{{ allLinks.length }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="kg-main" ref="kgMainRef">
        <div class="main-header">
          <el-button :icon="ArrowLeft" @click="goBack">
            <template #default>
              <span class="button-text">返回首页</span>
            </template>
          </el-button>
          <h1 class="page-title">🧠 知识图谱探索</h1>
          <el-button type="primary" circle @click="toggleFullscreen">
            <el-icon>
              <FullScreen />
            </el-icon>
          </el-button>
        </div>
        <div ref="kgChartRef" class="kg-chart"></div>
        <el-empty v-if="displayNodes.length === 0 && !loading" description="暂无知识图谱数据" />
      </div>
    </div>

    <el-dialog v-model="nodeDialogVisible" title="节点详情" width="500px">
      <div v-if="selectedNode" class="node-detail">
        <h3>{{ selectedNode.name }}</h3>
        <div class="detail-info">
          <p><strong>类型:</strong> {{ selectedNode.category }}</p>
          <p><strong>出现次数:</strong> {{ selectedNode.value }}</p>
        </div>
        <div class="related-nodes">
          <h4>相关节点</h4>
          <div v-if="relatedNodes.length > 0" class="related-list">
            <div v-for="node in relatedNodes" :key="node.id" class="related-item" @click="handleNodeClick(node)">
              {{ node.name }}
            </div>
          </div>
          <el-empty v-else description="暂无相关节点" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Search, Refresh, FullScreen } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { get } from '@/net'

const router = useRouter()

const loading = ref(false)
const kgChartRef = ref(null)
const kgMainRef = ref(null)
let kgChart = null

const allNodes = ref([])
const allLinks = ref([])
const displayNodes = ref([])
const displayLinks = ref([])
const searchKeyword = ref('')
const selectedCategories = ref(['CONCEPT', 'ENTITY', 'TECHNOLOGY'])

const nodeDialogVisible = ref(false)
const selectedNode = ref(null)
const relatedNodes = ref([])

const relationTypes = computed(() => {
  const types = new Set()
  allLinks.value.forEach(link => {
    if (link.label) {
      types.add(link.label)
    }
  })
  return Array.from(types)
})

const categoryColors = {
  'CONCEPT': '#ff9800',
  'ENTITY': '#4caf50',
  'TECH': '#2196f3'
}

const relationColors = [
  '#ff9800', '#4caf50', '#2196f3', '#9c27b0',
  '#ff5722', '#00bcd4', '#e91e63', '#8bc34a'
]

function getRelationColor(relation) {
  const index = relationTypes.value.indexOf(relation)
  return relationColors[index % relationColors.length]
}

function goBack() {
  router.push('/home')
}

function toggleFullscreen() {
  if (!kgMainRef.value) return
  if (!document.fullscreenElement) {
    kgMainRef.value.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

function loadFullGraph() {
  loading.value = true
  get('/api/kg/preview?nodeLimit=100&relationLimit=200', (data) => {
    if (data && data.nodes) {
      allNodes.value = data.nodes
      allLinks.value = data.links || []
      filterGraph()
    } else {
      allNodes.value = []
      allLinks.value = []
      displayNodes.value = []
      displayLinks.value = []
    }
    loading.value = false
    nextTick(() => {
      initKgChart()
    })
  }, () => {
    loading.value = false
  })
}

function filterGraph() {
  const keyword = searchKeyword.value.toLowerCase()

  displayNodes.value = allNodes.value.filter(node => {
    const categoryMatch = selectedCategories.value.includes(node.category)
    const keywordMatch = !keyword || node.name.toLowerCase().includes(keyword)
    return categoryMatch && keywordMatch
  })

  const displayNodeIds = new Set(displayNodes.value.map(n => n.id))
  displayLinks.value = allLinks.value.filter(link => {
    return displayNodeIds.has(link.source) && displayNodeIds.has(link.target)
  })
}

function handleSearch() {
  filterGraph()
  updateChart()
}

function handleCategoryChange() {
  filterGraph()
  updateChart()
}

function initKgChart() {
  if (!kgChartRef.value) {
    return
  }

  if (kgChart) {
    kgChart.dispose()
  }

  kgChart = echarts.init(kgChartRef.value)

  updateChart()

  window.addEventListener('resize', () => {
    if (kgChart) {
      kgChart.resize()
    }
  })
}

function updateChart() {
  if (!kgChart) {
    return
  }

  const categories = [
    { name: '概念', color: '#ff9800' },
    { name: '实体', color: '#4caf50' },
    { name: '技术', color: '#2196f3' }
  ]

  const categoryMap = {
    'CONCEPT': '概念',
    'ENTITY': '实体',
    'TECHNOLOGY': '技术'
  }

  const nodes = displayNodes.value.map(node => ({
    id: node.id,
    name: node.name,
    category: categories.findIndex(c => c.name === categoryMap[node.category]) || 0,
    value: node.value || 1,
    itemStyle: {
      color: categories.find(c => c.name === categoryMap[node.category])?.color || '#999'
    }
  }))

  const links = displayLinks.value.map(link => ({
    source: link.source,
    target: link.target,
    label: { show: true, formatter: link.label || '' }
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (params) => {
        if (params.dataType === 'node') {
          return `节点: ${params.data.name}<br/>类型: ${categories[params.data.category]?.name || '未知'}`
        }
        return `关系: ${params.data.label?.formatter || ''}<br/>${params.data.source} → ${params.data.target}`
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
        fontSize: 12
      },
      lineStyle: {
        color: '#999',
        width: 2
      },
      edgeLabel: {
        show: true,
        fontSize: 10,
        formatter: (params) => {
          return params.data.label?.formatter || ''
        }
      },
      data: nodes,
      links: links,
      categories: categories,
      force: {
        repulsion: 150,
        edgeLength: 100,
        layoutAnimation: true
      }
    }]
  }

  kgChart.setOption(option)

  kgChart.on('click', (params) => {
    if (params.dataType === 'node') {
      handleNodeClick(params.data)
    }
  })
}

function handleNodeClick(nodeData) {
  const node = allNodes.value.find(n => n.id === nodeData.id)
  if (node) {
    selectedNode.value = node
    relatedNodes.value = []

    const relatedLinks = allLinks.value.filter(
      link => link.source === node.id || link.target === node.id
    )

    const relatedIds = new Set()
    relatedLinks.forEach(link => {
      if (link.source === node.id) {
        relatedIds.add(link.target)
      } else {
        relatedIds.add(link.source)
      }
    })

    relatedNodes.value = allNodes.value.filter(n => relatedIds.has(n.id))
    nodeDialogVisible.value = true
  }
}

onMounted(() => {
  loadFullGraph()
})

onUnmounted(() => {
  if (kgChart) {
    kgChart.dispose()
  }
})
</script>

<style scoped>
.kg-explore {
  height: 100%;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  padding: 20px;
}

.kg-container {
  flex: 1;
  display: flex;
  gap: 20px;
  min-height: 0;
}

.kg-sidebar {
  width: 220px;
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow-y: auto;
}

.sidebar-header {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-input {
  width: 100%;
}

.refresh-btn {
  width: 100%;
}

.sidebar-section {
  margin-bottom: 20px;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.relation-types {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.relation-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.relation-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.relation-name {
  font-size: 12px;
  color: #666;
}

.stats-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.stat-label {
  color: #666;
}

.stat-value {
  font-weight: 600;
  color: #333;
}

.kg-main {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.main-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
}

.kg-chart {
  flex: 1;
  min-height: 500px;
}

.node-detail h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #333;
}

.detail-info {
  margin-bottom: 16px;
}

.detail-info p {
  margin: 8px 0;
  font-size: 14px;
  color: #666;
}

.related-nodes h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #333;
}

.related-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.related-item {
  padding: 4px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.related-item:hover {
  background: #e3f2fd;
  color: #2196f3;
}

.button-text {
  margin-left: 4px;
}
</style>
