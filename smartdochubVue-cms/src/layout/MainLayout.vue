<template>
  <div class="main-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <span v-show="!isCollapsed" class="logo-text">SmartDocHub 管理系统</span>
      </div>

      <el-menu :default-active="activeMenu" class="sidebar-menu" :collapse="isCollapsed" :collapse-transition="false"
        router>
        <el-menu-item index="/admin/dashboard">
          <el-icon>
            <DataAnalysis />
          </el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/admin/docs">
          <el-icon>
            <Document />
          </el-icon>
          <template #title>文档管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon>
            <FolderOpened />
          </el-icon>
          <template #title>分类管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/tags">
          <el-icon>
            <PriceTag />
          </el-icon>
          <template #title>标签管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/tasks">
          <el-icon>
            <Monitor />
          </el-icon>
          <template #title>任务管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon>
            <UserFilled />
          </el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/roles">
          <el-icon>
            <Position />
          </el-icon>
          <template #title>角色管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/comments">
          <el-icon>
            <ChatDotRound />
          </el-icon>
          <template #title>评论管理</template>
        </el-menu-item>
        <el-menu-item index="/admin/config">
          <el-icon>
            <Tools />
          </el-icon>
          <template #title>系统配置</template>
        </el-menu-item>
        <el-menu-item index="/admin/logs">
          <el-icon>
            <Reading />
          </el-icon>
          <template #title>日志管理</template>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <el-button :icon="isCollapsed ? Expand : Fold" text @click="isCollapsed = !isCollapsed" />
      </div>
    </aside>

    <div class="main-container">
      <header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">管理中心</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-popover placement="bottom" :width="360" trigger="click" @show="loadNotices">
            <template #reference>
              <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="notification-badge">
                <el-button :icon="Bell" circle />
              </el-badge>
            </template>
            <div class="notice-panel">
              <div class="notice-header">
                <span class="notice-title">通知公告</span>
                <el-button type="primary" link size="small" @click="handleReadAll">全部已读</el-button>
              </div>
              <div class="notice-list" v-loading="noticeLoading">
                <div v-if="notices.length === 0" class="notice-empty">暂无通知</div>
                <div
                  v-for="notice in notices"
                  :key="notice.id"
                  class="notice-item"
                  :class="{ unread: !notice.readStatus }"
                  @click="handleNoticeClick(notice)"
                >
                  <div class="notice-item-title">
                    <el-tag v-if="notice.level === 'H'" type="danger" size="small">紧急</el-tag>
                    <el-tag v-else-if="notice.level === 'M'" type="warning" size="small">重要</el-tag>
                    <el-tag v-else type="info" size="small">普通</el-tag>
                    <span class="notice-item-text">{{ notice.title }}</span>
                  </div>
                  <div class="notice-item-time">{{ notice.publishTime }}</div>
                </div>
              </div>
            </div>
          </el-popover>

          <el-dropdown trigger="click">
            <div class="user-info">
              <el-avatar :size="32" style="background-color: #409eff">
                {{ userInfo?.username?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="username">{{ userInfo?.username || '管理员' }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="User" @click="handlePersonalCenter">个人中心</el-dropdown-item>
                <el-dropdown-item :icon="Setting" @click="handleSystemSettings">系统设置</el-dropdown-item>
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

    <el-dialog v-model="showProfileDialog" title="个人中心" width="550px">
      <el-form :model="profileForm" ref="profileFormRef" label-width="80px" v-loading="profileLoading">
        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="profileForm.mobile" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="profileForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
            <el-radio :label="0">未知</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProfileDialog = false">取消</el-button>
        <el-button type="primary" @click="submitProfile" :loading="profileSaving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showNoticeDetailDialog" title="通知详情" width="600px">
      <div v-if="currentNotice" class="notice-detail">
        <h3>{{ currentNotice.title }}</h3>
        <div class="notice-meta">
          <el-tag v-if="currentNotice.level === 'H'" type="danger" size="small">紧急</el-tag>
          <el-tag v-else-if="currentNotice.level === 'M'" type="warning" size="small">重要</el-tag>
          <span class="notice-time">{{ currentNotice.publishTime }}</span>
        </div>
        <div class="notice-content" v-html="currentNotice.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Setting, DataAnalysis, Document, UserFilled, Position,
  ChatDotRound, Tools, Reading, Bell, User, SwitchButton,
  Fold, Expand, FolderOpened, PriceTag, Monitor
} from '@element-plus/icons-vue'
import { logout, getUserInfo } from '@/net'
import { getUserProfile, updateUserProfile, getMyNotices, getNoticeDetail, readAllNotices } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const isCollapsed = ref(false)

const userInfo = computed(() => {
  return getUserInfo()
})

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')

const showProfileDialog = ref(false)
const profileLoading = ref(false)
const profileSaving = ref(false)
const profileFormRef = ref()
const profileForm = ref({
  id: null,
  username: '',
  nickname: '',
  email: '',
  mobile: '',
  gender: 0,
  avatar: ''
})

const notices = ref([])
const noticeLoading = ref(false)
const unreadCount = ref(0)
const showNoticeDetailDialog = ref(false)
const currentNotice = ref(null)

function handlePersonalCenter() {
  profileLoading.value = true
  showProfileDialog.value = true
  getUserProfile((data) => {
    if (data) {
      profileForm.value = {
        id: data.id,
        username: data.username || '',
        nickname: data.nickname || '',
        email: data.email || '',
        mobile: data.mobile || '',
        gender: data.gender || 0,
        avatar: data.avatar || ''
      }
    }
    profileLoading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载个人信息失败')
    profileLoading.value = false
  })
}

function submitProfile() {
  profileSaving.value = true
  updateUserProfile(profileForm.value, () => {
    ElMessage.success('个人信息更新成功')
    profileSaving.value = false
    showProfileDialog.value = false
  }, (msg) => {
    ElMessage.error(msg || '更新失败')
    profileSaving.value = false
  })
}

function loadNotices() {
  noticeLoading.value = true
  getMyNotices({ pageNum: 1, pageSize: 10 }, (data) => {
    const records = data.records || data.list || []
    notices.value = records
    unreadCount.value = records.filter(n => !n.readStatus).length
    noticeLoading.value = false
  }, () => {
    noticeLoading.value = false
  })
}

function handleNoticeClick(notice) {
  getNoticeDetail(notice.id, (data) => {
    currentNotice.value = data
    showNoticeDetailDialog.value = true
    notice.readStatus = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }, (msg) => {
    ElMessage.error(msg || '加载通知详情失败')
  })
}

function handleReadAll() {
  readAllNotices(() => {
    notices.value.forEach(n => { n.readStatus = true })
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  }, (msg) => {
    ElMessage.error(msg || '操作失败')
  })
}

function handleSystemSettings() {
  router.push('/admin/config')
}

function handleLogout() {
  logout(() => router.push('/'))
}

onMounted(() => {
  loadNotices()
})
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
}

.sidebar {
  width: 220px;
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
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff9800, #ff6637);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

:deep(.el-menu-item) {
  margin: 4px 8px;
  border-radius: 8px;
  height: 44px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #FFB48A,#ff562290) !important;
  color: white !important;
}

:deep(.el-menu-item:hover:not(.is-active)) {
  background-color: #fff0e6;
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

.notice-panel {
  max-height: 400px;
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  margin-bottom: 8px;
}

.notice-title {
  font-weight: 600;
  font-size: 15px;
}

.notice-list {
  max-height: 340px;
  overflow-y: auto;
}

.notice-empty {
  text-align: center;
  color: #999;
  padding: 40px 0;
}

.notice-item {
  padding: 10px 8px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notice-item:hover {
  background-color: #f5f7fa;
}

.notice-item.unread {
  background-color: #ecf5ff;
}

.notice-item.unread:hover {
  background-color: #d9ecff;
}

.notice-item-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.notice-item-text {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.notice-item-time {
  font-size: 12px;
  color: #999;
  padding-left: 52px;
}

.notice-detail h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
}

.notice-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.notice-time {
  font-size: 13px;
  color: #999;
}

.notice-content {
  line-height: 1.8;
  font-size: 14px;
}
</style>
