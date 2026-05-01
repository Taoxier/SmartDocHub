<template>
  <div class="log-list">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>日志管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keywords" placeholder="日志内容/路径/操作人" style="width: 200px" clearable></el-input>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="reset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logs" style="width: 100%" border v-loading="loading">
        <el-table-column prop="id" label="日志ID" width="80"></el-table-column>
        <el-table-column prop="operator" label="操作人" width="120"></el-table-column>
        <el-table-column prop="module" label="日志模块" width="120">
          <template #default="scope">
            <el-tag size="small">{{ getModuleText(scope.row.module) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="日志内容" min-width="250" show-overflow-tooltip></el-table-column>
        <el-table-column prop="requestUri" label="请求路径" width="180" show-overflow-tooltip></el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140"></el-table-column>
        <el-table-column prop="region" label="地区" width="100"></el-table-column>
        <el-table-column prop="executionTime" label="耗时(ms)" width="90"></el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170"></el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetails(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        ></el-pagination>
      </div>
    </el-card>

    <el-dialog v-model="showDetailDialog" title="日志详情" width="650px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志ID" :span="2">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operator }}</el-descriptions-item>
        <el-descriptions-item label="日志模块">{{ getModuleText(currentLog.module) }}</el-descriptions-item>
        <el-descriptions-item label="日志内容" :span="2">{{ currentLog.content }}</el-descriptions-item>
        <el-descriptions-item label="请求路径" :span="2">{{ currentLog.requestUri }}</el-descriptions-item>
        <el-descriptions-item label="请求方法" :span="2">{{ currentLog.method }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="地区">{{ currentLog.region }}</el-descriptions-item>
        <el-descriptions-item label="浏览器">{{ currentLog.browser }}</el-descriptions-item>
        <el-descriptions-item label="终端系统">{{ currentLog.os }}</el-descriptions-item>
        <el-descriptions-item label="执行时间">{{ currentLog.executionTime }} ms</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getLogPage } from '@/api/admin'

const loading = ref(false)
const logs = ref([])
const showDetailDialog = ref(false)
const currentLog = ref(null)

const searchForm = reactive({
  keywords: '',
  dateRange: []
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const MODULE_MAP = {
  LOGIN: '登录', USER: '用户', ROLE: '角色', MENU: '菜单',
  DOCUMENT: '文档', COMMENT: '评论', CATEGORY: '分类', TAG: '标签',
  TASK: '任务', SETTING: '配置', LOG: '日志', DASHBOARD: '仪表盘'
}

function getModuleText(module) {
  if (!module) return '-'
  return MODULE_MAP[module] || module
}

function loadLogs() {
  loading.value = true
  const params = {
    pageNum: pagination.pageNum,
    pageSize: pagination.pageSize,
    keywords: searchForm.keywords
  }
  if (searchForm.dateRange && searchForm.dateRange.length === 2) {
    params.createTime = [searchForm.dateRange[0], searchForm.dateRange[1]]
  }
  getLogPage(params, (data) => {
    logs.value = data.records || data.list || []
    pagination.total = data.total || 0
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载日志列表失败')
    loading.value = false
  })
}

function search() {
  pagination.pageNum = 1
  loadLogs()
}

function reset() {
  searchForm.keywords = ''
  searchForm.dateRange = []
  pagination.pageNum = 1
  loadLogs()
}

function viewDetails(row) {
  currentLog.value = row
  showDetailDialog.value = true
}

function handleSizeChange(size) {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadLogs()
}

function handleCurrentChange(current) {
  pagination.pageNum = current
  loadLogs()
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.log-list {
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
