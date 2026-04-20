<template>
  <div class="main-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <el-icon :size="24" color="#ff9800">
          <Document />
        </el-icon>
        <span v-show="!isCollapsed" class="logo-text">SmartDocHub</span>
      </div>

      <el-menu :default-active="activeMenu" class="sidebar-menu" :collapse="isCollapsed" :collapse-transition="false"
        router>
        <!-- 客户端菜单 -->
        <el-menu-item index="/home">
          <el-icon>
            <HomeFilled />
          </el-icon>
          <template #title>首页推荐</template>
        </el-menu-item>

        <el-menu-item index="/doc/list">
          <el-icon>
            <Folder />
          </el-icon>
          <template #title>文档列表</template>
        </el-menu-item>

        <el-menu-item index="/search">
          <el-icon>
            <Search />
          </el-icon>
          <template #title>高级搜索</template>
        </el-menu-item>

        <el-menu-item class="upload-menu-item" @click.stop>
          <el-popover trigger="hover" placement="right" :width="160" popper-class="upload-popover">
            <template #reference>
              <div class="upload-menu-content">
                <el-icon>
                  <Upload />
                </el-icon>
                <span>上传文档</span>
              </div>
            </template>
            <div class="upload-menu-options">
              <div class="upload-menu-option" @click="handleQuickUpload">快速上传</div>
              <div class="upload-menu-option" @click="handleAdvancedUpload">高级上传</div>
            </div>
          </el-popover>
        </el-menu-item>

        <el-sub-menu index="/user">
          <template #title>
            <el-icon>
              <User />
            </el-icon>
            <span>我的文档</span>
          </template>
          <el-menu-item index="/user/uploaded">我的上传</el-menu-item>
          <el-menu-item index="/user/favorites">我的收藏</el-menu-item>
          <el-menu-item index="/user/history">浏览历史</el-menu-item>
        </el-sub-menu>
      </el-menu>

      <div class="sidebar-footer">
        <el-button :icon="isCollapsed ? Expand : Fold" text @click="isCollapsed = !isCollapsed" />
      </div>
    </aside>

    <div class="main-container">
      <header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-input v-model="searchKeyword" placeholder="搜索文档..." :prefix-icon="Search" style="width: 240px" clearable
            @keyup.enter="handleSearch" />

          <el-badge :value="3" :max="99" class="notification-badge">
            <el-button :icon="Bell" circle />
          </el-badge>

          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" style="background-color: #ff9800">
                {{ userInfo?.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo?.username || '用户' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="User" @click="handlePersonalCenter">个人信息</el-dropdown-item>
                <el-dropdown-item divided :icon="SwitchButton" @click="handleLogout">
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 快速上传对话框 -->
    <el-dialog v-model="showQuickUploadDialog" title="快速上传" width="500px" :close-on-click-modal="false">
      <el-upload class="upload-area" drag :auto-upload="false" :limit="10" :on-change="handleFileChange"
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
        <el-button @click="showQuickUploadDialog = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          确认上传
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document, HomeFilled, Folder, User, Star, Search, Bell,
  SwitchButton, Fold, Expand, Upload, Timer, Grid, Stamp, UploadFilled, StarFilled
} from '@element-plus/icons-vue'
import { logout, getUserInfo } from '@/net'
import { uploadDocument, uploadMultipleDocuments } from '@/api/document'

const route = useRoute()
const router = useRouter()

const isCollapsed = ref(false)
const searchKeyword = ref('')
const showQuickUploadDialog = ref(false)
const selectedFiles = ref([])
const uploading = ref(false)

// 获取用户信息
const userInfo = computed(() => {
  return getUserInfo()
})

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { keyword: searchKeyword.value } })
  }
}

function handlePersonalCenter() {
  router.push('/user/center')
}

function handleMyUploaded() {
  router.push('/user/uploaded')
}

function handleMyFavorites() {
  router.push('/user/favorites')
}

function handleMyHistory() {
  router.push('/user/history')
}

function handleLogout() {
  logout(() => router.push('/'))
}

function handleQuickUpload() {
  showQuickUploadDialog.value = true
}

function handleAdvancedUpload() {
  router.push('/upload')
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
      showQuickUploadDialog.value = false
      uploading.value = false
      selectedFiles.value = []
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
      showQuickUploadDialog.value = false
      uploading.value = false
      selectedFiles.value = []
    }, (msg) => {
      ElMessage.error(msg || '上传失败')
      uploading.value = false
    })
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
}

.sidebar {
  width: 146px;
  background: linear-gradient(180deg, #fff5eb 0%, #ffffff 100%);
  border-right: 1px solid #ffe4d4;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 0 16px;
  border-bottom: 1px solid #ffe4d4;
}

.logo-text {
  font-size: 14px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff9800, #ff5722);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
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

.upload-menu-item {
  position: relative;
}

.upload-menu-item:hover {
  background-color: #fff0e6 !important;
  color: #333 !important;
}

.upload-menu-item:hover .upload-menu-content span {
  color: #333;
}

.upload-menu-item:hover .upload-menu-content el-icon {
  color: #333;
}

.upload-menu-content {
  display: flex;
  align-items: center;
  /* gap: 10px; */
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.upload-menu-content span {
  font-size: 14px;
  color: #333;
  transition: color 0.3s ease;
}

:deep(.upload-popover) {
  background-color: #fff5eb;
  border: 1px solid #ffe4d4;
  border-radius: 8px;
  padding: 8px 0;
  min-width: 160px;
  margin-left: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1) !important;
}

.upload-menu-options {
  display: flex;
  flex-direction: column;
}

.upload-menu-option {
  height: 50px;
  line-height: 50px;
  padding: 0 20px;
  margin: 0;
  border-radius: 0;
  transition: all 0.3s ease;
  color: #333;
  font-size: 14px;
  cursor: pointer;
}

.upload-menu-option:hover {
  background-color: #fff0e6;
  color: #333;
}

/* .sidebar-menu {
  flex: 1;
  border-right: none;
  background: linear-gradient(180deg, #fff5eb 0%, #ffffff 100%);

  :deep(.el-menu.el-menu--inline) {
    background: transparent;
    border: none;
  }
} */

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: linear-gradient(180deg, #fff5eb 0%, #ffffff 100%);
}

/* 清除子菜单白色背景 */
:deep(.sidebar-menu .el-menu.el-menu--inline) {
  background: transparent !important;
  border: none !important;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 146px;
}

:deep(.el-menu-item) {
  margin: 4px 8px;
  border-radius: 8px;
  height: 44px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #ff9800, #ff5722) !important;
  color: white !important;
}

:deep(.el-menu-item:hover:not(.is-active)) {
  background-color: #fff0e6;
}

/* 个人中心子菜单样式 */
:deep(.el-sub-menu) .el-menu-item {
  margin: 4px 8px;
  border-radius: 8px;
  height: 44px;
  line-height: 44px;
  font-size: 13px;
  color: #666;
  /* background-color: #fff5eb; */
}

:deep(.el-sub-menu) .el-menu-item:hover {
  /* color: #ff9800; */
  background-color: #fff0e6;
}

:deep(.el-sub-menu) .el-menu-item.is-active {
  background: linear-gradient(135deg, #ff9800, #ff5722);
  color: #fff;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid #ffe4d4;
  display: flex;
  justify-content: center;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background: white;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-badge {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f5f5f5;
}

.username {
  font-size: 14px;
  color: #333;
}

.content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
