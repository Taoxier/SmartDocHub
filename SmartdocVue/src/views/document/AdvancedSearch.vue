<template>
  <div class="advanced-search">
    <!-- <div class="search-header">
      <h2 class="page-title">高级搜索</h2>
    </div> -->

    <div class="search-container">
      <!-- 左侧筛选区域 -->
      <div class="filter-sidebar">
        <!-- 分类筛选 -->
        <div class="filter-section">
          <h3 class="filter-title">分类筛选</h3>
          <el-tree :data="categories" node-key="id" :props="categoryProps" @node-click="handleCategoryClick"
            default-expand-all class="category-tree" />
        </div>

        <!-- 热门标签 -->
        <div class="filter-section">
          <h3 class="filter-title">热门标签</h3>
          <div class="tag-cloud">
            <el-tag v-for="tag in hotTags" :key="tag.id" :size="getTagSize(tag.count)"
              :class="['hot-tag', { active: selectedTags.includes(tag.topicValue) }]"
              @click="toggleTag(tag.topicValue)">
              {{ tag.topicValue }}
            </el-tag>
          </div>
        </div>

        <!-- 其他筛选条件 -->
        <div class="filter-section">
          <h3 class="filter-title">文件类型</h3>
          <el-checkbox-group v-model="selectedFileTypes">
            <el-checkbox label="pdf">PDF</el-checkbox>
            <el-checkbox label="docx">Word</el-checkbox>
            <el-checkbox label="txt">TXT</el-checkbox>
          </el-checkbox-group>
        </div>

        <div class="filter-section">
          <h3 class="filter-title">排序方式</h3>
          <el-radio-group v-model="sortBy">
            <el-radio label="createTime">最新上传</el-radio>
            <el-radio label="viewCount">最多浏览</el-radio>
            <el-radio label="downloadCount">最多下载</el-radio>
          </el-radio-group>
        </div>

        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </div>
      </div>

      <!-- 右侧结果区域 -->
      <div class="result-area">
        <div class="search-bar">
          <el-input v-model="keyword" placeholder="搜索文档标题、描述..." :prefix-icon="Search" clearable style="width: 400px"
            @keyup.enter="handleSearch" />
        </div>

        <div class="document-grid" v-loading="loading">
          <el-empty v-if="documentList.length === 0 && !loading" description="暂无相关文档" />

          <div v-for="doc in documentList" :key="doc.id" class="document-card" @click="goToDetail(doc.id)">
            <div class="card-header">
              <div class="file-icon" :style="{ backgroundColor: getFileIcon(doc.fileType).color + '20' }">
                <el-icon :size="24" :color="getFileIcon(doc.fileType).color">
                  <Document />
                </el-icon>
              </div>
              <div class="file-type-tag">{{ doc.fileType?.toUpperCase() }}</div>
              <el-dropdown trigger="hover" @click.stop>
                <el-button circle text>
                  <el-icon>
                    <More />
                  </el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click.stop="handleDownload(doc)">
                      <el-icon>
                        <Download />
                      </el-icon> 下载
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

            <div class="card-body">
              <h3 class="doc-title" :title="doc.title">{{ doc.title }}</h3>
              <p class="doc-desc" :title="doc.description">{{ doc.description || '暂无描述' }}</p>
              <div class="doc-category" v-if="doc.category">
                <el-tag size="small" class="doc-category-tag">{{ doc.category }}</el-tag>
              </div>
              <div class="doc-tags" v-if="doc.topics && doc.topics.length > 0">
                <el-tag v-for="topic in doc.topics.slice(0, 3)" :key="topic.id" size="small" class="doc-topic-tag">
                  {{ topic.topicValue }}
                </el-tag>
                <el-tag v-if="doc.topics.length > 3" size="small" class="doc-topic-tag">+{{ doc.topics.length - 3
                }}</el-tag>
              </div>
            </div>

            <div class="card-footer">
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

        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[20, 50, 100]"
            :total="total" layout="sizes, prev, pager, next, jumper" @size-change="loadDocuments"
            @current-change="loadDocuments" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Search, Document, View, Download, More, Share
} from '@element-plus/icons-vue'
import {
  getDocumentList, downloadDocument, formatFileSize, getFileIcon,
  getAllCategories, getHotTopics
} from '@/api/document'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const documentList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const keyword = ref('')

// 分类数据 - 从数据库加载
const categories = ref([])

const categoryProps = {
  children: 'children',
  label: 'label'
}

// 热门标签 - 从数据库加载
const hotTags = ref([])

// 筛选条件
const selectedCategory = ref(null)
const selectedTags = ref([])
const selectedFileTypes = ref([])
const sortBy = ref('createTime')

onMounted(() => {
  if (route.query.tag) {
    selectedTags.value = [route.query.tag]
  }
  if (route.query.sortBy) {
    sortBy.value = route.query.sortBy
  }
  loadDocuments()
  loadCategories()
  loadHotTags()
})

function loadCategories() {
  getAllCategories((data) => {
    // 将数据库分类转换为树形结构
    // parentId可能是字符串'0'或数字0
    const parentCategories = data.filter(c => c.parentId === 0 || c.parentId === '0' || c.parentId === null)
    categories.value = parentCategories.map(parent => {
      const children = data.filter(c => String(c.parentId) === String(parent.id))
      return {
        id: parent.id,
        label: parent.name,
        children: children.map(child => ({
          id: child.id,
          label: child.name
        }))
      }
    })
  }, (msg) => {
    console.error('加载分类失败:', msg)
  })
}

function loadHotTags() {
  getHotTopics(30, (data) => {
    hotTags.value = data.map(item => ({
      id: item.topic_value,
      topicValue: item.topic_value,
      count: parseInt(item.cnt) || 0
    }))
  }, (msg) => {
    console.error('加载热门标签失败:', msg)
  })
}

function loadDocuments() {
  loading.value = true
  const params = {
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: keyword.value,
    sortBy: sortBy.value,
    sortOrder: 'desc'
  }

  // 添加分类筛选
  if (selectedCategory.value) {
    params.category = selectedCategory.value
  }

  // 添加标签筛选
  if (selectedTags.value.length > 0) {
    params.tags = selectedTags.value.join(',')
  }

  // 添加文件类型筛选
  if (selectedFileTypes.value.length > 0) {
    params.fileType = selectedFileTypes.value.join(',')
  }

  getDocumentList(params, (data) => {
    documentList.value = data.list || []
    total.value = data.total || 0
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载失败')
    loading.value = false
  })
}

function handleSearch() {
  pageNum.value = 1
  loadDocuments()
}

function resetFilters() {
  keyword.value = ''
  selectedCategory.value = null
  selectedTags.value = []
  selectedFileTypes.value = []
  sortBy.value = 'createTime'
  pageNum.value = 1
  loadDocuments()
}

function handleCategoryClick(data) {
  selectedCategory.value = data.label
  pageNum.value = 1
  loadDocuments()
}

function toggleTag(tag) {
  const index = selectedTags.value.indexOf(tag)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tag)
  }
  pageNum.value = 1
  loadDocuments()
}

function getTagSize(count) {
  if (count > 100) return 'large'
  if (count > 50) return 'default'
  return 'small'
}

function goToDetail(id) {
  router.push(`/doc/detail/${id}`)
}

function handleDownload(doc) {
  downloadDocument(doc.id)
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
</script>

<style scoped>
.advanced-search {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.search-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.search-container {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
}

.filter-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow-y: auto;
}

.filter-section {
  margin-bottom: 24px;
}

.filter-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.category-tree {
  max-height: 200px;
  overflow-y: auto;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hot-tag {
  cursor: pointer;
  transition: all 0.3s ease;
}

.hot-tag:hover {
  transform: translateY(-2px);
}

.hot-tag.active {
  background-color: #fa8c16;
  color: white;
  border-color: #fa8c16;
}

.filter-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.result-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.search-bar {
  margin-bottom: 20px;
}

.document-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  overflow-y: auto;
  margin-bottom: 20px;
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
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f5f5f5;
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

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  background: white;
  border-radius: 12px;
  margin-top: auto;
}
</style>