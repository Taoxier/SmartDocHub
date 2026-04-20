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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  Document, UserFilled, Upload, Download, View, DataAnalysis, User, TrendCharts
} from '@element-plus/icons-vue'
import { getAdminDocumentStats, getVisitStats, getTaskStats, getUploadTrend, getDocumentTypeRatio } from '@/api/admin'

const router = useRouter()
const uploadChartRef = ref()
const typeChartRef = ref()

const stats = ref({})
const visitStats = ref({})
const taskStats = ref({})
const uploadTrend = ref([])
const documentTypeRatio = ref([])

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
  ElMessage.success('数据已刷新')
}

function goToTasks() {
  router.push('/admin/tasks')
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
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
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
</style>
