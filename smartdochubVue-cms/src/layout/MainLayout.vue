<template>
  <div class="main-layout">
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <!-- <el-icon :size="28" color="#409eff">
          <Setting />
        </el-icon> -->
        <span v-show="!isCollapsed" class="logo-text">SmartDocHub 管理系统</span>
      </div>

      <el-menu :default-active="activeMenu" class="sidebar-menu" :collapse="isCollapsed" :collapse-transition="false"
        router>
        <!-- 管理端菜单 -->
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
          <el-badge :value="3" :max="99" class="notification-badge">
            <el-button :icon="Bell" circle />
          </el-badge>

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
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Setting, DataAnalysis, Document, UserFilled, Position,
  ChatDotRound, Tools, Reading, Bell, User, SwitchButton,
  Fold, Expand, FolderOpened, PriceTag, Monitor
} from '@element-plus/icons-vue'
import { logout, getUserInfo } from '@/net'

const route = useRoute()
const router = useRouter()

const isCollapsed = ref(false)

// 获取用户信息
const userInfo = computed(() => {
  return getUserInfo()
})

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')

function handlePersonalCenter() {
  ElMessage.info('个人中心功能正在开发中')
}

function handleSystemSettings() {
  router.push('/admin/config')
}

function handleLogout() {
  logout(() => router.push('/'))
}
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
  background: linear-gradient(135deg, #ff9800, #ff5722);
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
  background: linear-gradient(135deg, #ff9800, #ff5722) !important;
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
</style>