<template>
  <div class="dashboard">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>仪表盘</span>
          <el-button size="small" @click="loadData">刷新</el-button>
        </div>
      </template>

      <div class="stats-cards">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="28" color="#409eff">
              <Document />
            </el-icon>
            <div class="stat-number">{{ stats.totalDocuments || 0 }}</div>
            <div class="stat-label">总文档数</div>
          </div>
        </el-card>

        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="28" color="#67c23a">
              <View />
            </el-icon>
            <div class="stat-number">{{ stats.totalViews || 0 }}</div>
            <div class="stat-label">总浏览量</div>
          </div>
        </el-card>

        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="28" color="#f56c6c">
              <Download />
            </el-icon>
            <div class="stat-number">{{ stats.totalDownloads || 0 }}</div>
            <div class="stat-label">总下载量</div>
          </div>
        </el-card>

        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="28" color="#e6a23c">
              <Upload />
            </el-icon>
            <div class="stat-number">{{ stats.todayDocuments || 0 }}</div>
            <div class="stat-label">今日上传</div>
          </div>
        </el-card>
      </div>

      <div class="stats-cards">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="28" color="#909399">
              <DataAnalysis />
            </el-icon>
            <div class="stat-number">{{ visitStats.totalPvCount || 0 }}</div>
            <div class="stat-label">总浏览次数(PV)</div>
          </div>
        </el-card>

        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="28" color="#409eff">
              <User />
            </el-icon>
            <div class="stat-number">{{ visitStats.totalUvCount || 0 }}</div>
            <div class="stat-label">总访客数(UV)</div>
          </div>
        </el-card>

        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="28" color="#67c23a">
              <TrendCharts />
            </el-icon>
            <div class="stat-number">{{ visitStats.todayPvCount || 0 }}</div>
            <div class="stat-label">今日浏览次数</div>
          </div>
        </el-card>

        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon :size="28" color="#e6a23c">
              <UserFilled />
            </el-icon>
            <div class="stat-number">{{ visitStats.todayUvCount || 0 }}</div>
            <div class="stat-label">今日访客数</div>
          </div>
        </el-card>
      </div>

      <div class="charts-section">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>近一周上传趋势</span>
          </template>
          <div ref="uploadChartRef" class="chart-container"></div>
        </el-card>

        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span>各类型文档占比</span>
          </template>
          <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </div>

      <div class="todo-section">
        <el-card shadow="hover">
          <template #header>
            <span>待办事项</span>
          </template>
          <el-list>
            <el-list-item>
              <div class="todo-item">
                <span class="todo-text">待处理任务：{{ taskStats.pendingTasks || 0 }} 个</span>
                <el-button type="primary" size="small" @click="goToTasks">去处理</el-button>
              </div>
            </el-list-item>
            <el-list-item>
              <div class="todo-item">
                <span class="todo-text">运行中任务：{{ taskStats.runningTasks || 0 }} 个</span>
                <el-button type="primary" size="small" @click="goToTasks">查看</el-button>
              </div>
            </el-list-item>
            <el-list-item>
              <div class="todo-item">
                <span class="todo-text">失败任务：{{ taskStats.failedTasks || 0 }} 个</span>
                <el-button type="danger" size="small" @click="goToTasks">处理</el-button>
              </div>
            </el-list-item>
          </el-list>
        </el-card>
      </div>

      <div class="ai-analysis-section">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>AI 数据分析</span>
              <el-tag type="success" size="small">自动更新</el-tag>
            </div>
          </template>

          <div class="ai-summary" v-if="aiSummary">
            <el-alert :title="aiSummary.summary || 'AI分析摘要加载中...'" type="info" :closable="false" show-icon />
          </div>

          <div class="charts-section">
            <el-card shadow="hover" class="chart-card">
              <template #header>
                <span>文档增长趋势预测</span>
              </template>
              <div ref="trendChartRef" class="chart-container"></div>
            </el-card>

            <el-card shadow="hover" class="chart-card">
              <template #header>
                <span>AI率分布统计</span>
              </template>
              <div ref="aiRateChartRef" class="chart-container"></div>
            </el-card>
          </div>

          <div class="ai-insights" v-if="aiSummary?.insights?.length">
            <h4 class="insight-title">AI 数据洞察</h4>
            <el-timeline>
              <el-timeline-item v-for="(insight, idx) in aiSummary.insights" :key="idx"
                :type="getInsightType(insight.level)" placement="top">
                <el-card shadow="hover" class="insight-card">
                  <div class="insight-header">
                    <el-tag :type="getInsightType(insight.level)" size="small">{{ insight.category }}</el-tag>
                    <span class="insight-time">{{ insight.time || '刚刚' }}</span>
                  </div>
                  <p class="insight-text">{{ insight.content }}</p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>

          <div class="anomaly-section" v-if="anomalies?.anomalies?.length">
            <h4 class="insight-title">异常检测结果</h4>
            <el-table :data="anomalies.anomalies" style="width: 100%" size="small">
              <el-table-column prop="type" label="异常类型" width="120" />
              <el-table-column prop="description" label="描述" />
              <el-table-column prop="severity" label="严重程度" width="100">
                <template #default="scope">
                  <el-tag :type="getSeverityType(scope.row.severity)" size="small">
                    {{ scope.row.severity }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="detectedAt" label="检测时间" width="180" />
            </el-table>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  Document, UserFilled, Upload, Download, View, DataAnalysis, User, TrendCharts
} from '@element-plus/icons-vue'
import { getAdminDocumentStats, getVisitStats, getTaskStats, getUploadTrend, getDocumentTypeRatio, getAiAnalysisSummary, getAiAnalysisTrend, getAiAnalysisAnomaly } from '@/api/admin'

const router = useRouter()
const uploadChartRef = ref()
const typeChartRef = ref()
const trendChartRef = ref()
const aiRateChartRef = ref()

const stats = ref({})
const visitStats = ref({})
const taskStats = ref({})
const uploadTrend = ref([])
const documentTypeRatio = ref([])
const aiSummary = ref(null)
const aiTrend = ref(null)
const anomalies = ref(null)
let refreshTimer = null

function loadDocumentStats() {
  getAdminDocumentStats((data) => {
    stats.value = data || {}
  }, (error) => {
    console.error('加载文档统计失败:', error)
  })
}

function loadVisitStats() {
  getVisitStats((data) => {
    visitStats.value = data || {}
  }, (error) => {
    console.error('加载访问统计失败:', error)
  })
}

function loadTaskStats() {
  getTaskStats((data) => {
    taskStats.value = data || {}
  }, (error) => {
    console.error('加载任务统计失败:', error)
  })
}

function loadUploadTrend() {
  getUploadTrend((data) => {
    uploadTrend.value = data || []
    initUploadChart()
  }, (error) => {
    console.error('加载上传趋势失败:', error)
  })
}

function loadDocumentTypeRatio() {
  getDocumentTypeRatio((data) => {
    documentTypeRatio.value = data || []
    initTypeChart()
  }, (error) => {
    console.error('加载文档类型占比失败:', error)
  })
}

function loadData() {
  loadDocumentStats()
  loadVisitStats()
  loadTaskStats()
  loadUploadTrend()
  loadDocumentTypeRatio()
  loadAiAnalysisData()
  ElMessage.success('数据已刷新')
}

function loadAiAnalysisData() {
  getAiAnalysisSummary((data) => {
    aiSummary.value = data || null
    nextTick(() => initAiRateChart())
  }, () => {
    aiSummary.value = null
  })
  getAiAnalysisTrend((data) => {
    aiTrend.value = data || null
    nextTick(() => initTrendChart())
  }, () => {
    aiTrend.value = null
  })
  getAiAnalysisAnomaly((data) => {
    anomalies.value = data || null
  }, () => {
    anomalies.value = null
  })
}

function goToTasks() {
  router.push('/admin/tasks')
}

function getInsightType(level) {
  const map = { 'high': 'danger', 'medium': 'warning', 'low': 'success', 'info': 'info' }
  return map[level] || 'info'
}

function getSeverityType(severity) {
  const map = { '高': 'danger', '中': 'warning', '低': 'info', 'high': 'danger', 'medium': 'warning', 'low': 'info' }
  return map[severity] || 'info'
}

function initTrendChart() {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  const trendData = aiTrend.value || {}
  const dates = trendData.dates || []
  const actual = trendData.actual || []
  const predicted = trendData.predicted || []

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['实际', '预测'] },
    xAxis: { type: 'category', data: dates },
    yAxis: { type: 'value' },
    series: [
      {
        name: '实际',
        type: 'line',
        data: actual,
        smooth: true,
        lineStyle: { color: '#409eff' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      },
      {
        name: '预测',
        type: 'line',
        data: predicted,
        smooth: true,
        lineStyle: { color: '#fa8c16', type: 'dashed' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(250, 140, 22, 0.3)' },
            { offset: 1, color: 'rgba(250, 140, 22, 0.05)' }
          ])
        }
      }
    ]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

function initAiRateChart() {
  if (!aiRateChartRef.value) return
  const chart = echarts.init(aiRateChartRef.value)
  const rateData = aiSummary.value?.aiRateDistribution || [
    { range: '0-20%', count: 0 },
    { range: '20-40%', count: 0 },
    { range: '40-60%', count: 0 },
    { range: '60-80%', count: 0 },
    { range: '80-100%', count: 0 }
  ]

  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: rateData.map(d => d.range)
    },
    yAxis: { type: 'value' },
    series: [{
      type: 'bar',
      data: rateData.map(d => d.count),
      itemStyle: {
        color: (params) => {
          const colors = ['#67c23a', '#95d475', '#e6a23c', '#f56c6c', '#c45656']
          return colors[params.dataIndex] || '#409eff'
        }
      }
    }]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

function initUploadChart() {
  if (!uploadChartRef.value) return

  const chart = echarts.init(uploadChartRef.value)

  // 使用真实数据
  const weekDays = uploadTrend.value.map(item => item.date)
  const uploadData = uploadTrend.value.map(item => item.count)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: weekDays
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: uploadData,
      type: 'line',
      smooth: true,
      lineStyle: {
        color: '#409eff'
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ])
      }
    }]
  }
  chart.setOption(option)

  window.addEventListener('resize', () => {
    chart.resize()
  })
}

function initTypeChart() {
  if (!typeChartRef.value) return

  const chart = echarts.init(typeChartRef.value)

  // 使用真实数据
  const typeData = documentTypeRatio.value.length > 0 ? documentTypeRatio.value : [
    { value: 0, name: '暂无数据' }
  ]

  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      data: typeData,
      type: 'pie',
      radius: '50%',
      color: ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
    }]
  }
  chart.setOption(option)

  window.addEventListener('resize', () => {
    chart.resize()
  })
}

onMounted(() => {
  loadData()
  refreshTimer = setInterval(() => {
    loadAiAnalysisData()
  }, 5 * 60 * 1000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.dashboard {
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

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  padding: 20px;
  border-radius: 8px;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.chart-card {
  padding: 20px;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.todo-section {
  margin-top: 20px;
}

.todo-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.todo-text {
  font-size: 14px;
  color: #303133;
}

.ai-analysis-section {
  margin-top: 20px;
}

.ai-summary {
  margin-bottom: 16px;
}

.insight-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 16px 0 12px;
}

.insight-card {
  margin-bottom: 0;
}

.insight-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.insight-time {
  font-size: 12px;
  color: #909399;
}

.insight-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.anomaly-section {
  margin-top: 20px;
}
</style>
