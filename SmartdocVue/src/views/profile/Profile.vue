<template>
  <div class="profile-page" v-loading="loading">
    <div class="profile-container">
      <!-- 左侧边栏 -->
      <div class="profile-sidebar">
        <!-- 用户信息 -->
        <div class="user-info-section">
          <div class="avatar-section">
            <el-avatar :size="80" :src="userInfo.avatar || defaultAvatar" style="margin-bottom: 12px">
              {{ userInfo.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <h3 class="nickname">{{ userInfo.nickname || '未设置昵称' }}</h3>
            <p class="username">{{ userInfo.username }}</p>
          </div>

          <!-- 统计信息 -->
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-value">{{ userStats.docCount }}</span>
              <span class="stat-label">上传</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userStats.favoriteCount }}</span>
              <span class="stat-label">收藏</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userStats.viewCount }}</span>
              <span class="stat-label">浏览</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ userStats.downloadCount }}</span>
              <span class="stat-label">下载</span>
            </div>
          </div>
        </div>

        <!-- 菜单 -->
        <div class="sidebar-menu">
          <el-menu :default-active="activeMenu" class="profile-menu" router>
            <el-menu-item index="/user/center?tab=info" @click="activeMenu = 'info'">
              <el-icon>
                <User />
              </el-icon>
              <template #title>个人信息</template>
            </el-menu-item>
          </el-menu>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="profile-content">
        <!-- 我的文档 -->
        <div v-if="activeMenu === 'docs'" class="content-section">
          <h3 class="section-title">我的文档</h3>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="上传的文档" name="uploaded">
              <DocumentList :query-params="{ uploadUserId: userInfo.id }" :is-my-docs="true" />
            </el-tab-pane>
            <el-tab-pane label="收藏的文档" name="favorites">
              <DocumentList :query-params="{ favorite: true }" :is-my-docs="true" />
            </el-tab-pane>
            <el-tab-pane label="浏览历史" name="history">
              <DocumentList :query-params="{ history: true }" :is-my-docs="false" />
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 个人信息 -->
        <div v-else-if="activeMenu === 'info'" class="content-section">
          <h3 class="section-title">个人信息</h3>
          <el-form :model="userForm" label-width="80px" class="info-form">
            <div class="form-row">
              <el-form-item label="用户名">
                <el-input v-model="userForm.username" disabled style="width: 200px" />
              </el-form-item>
              <el-form-item label="昵称">
                <el-input v-model="userForm.nickname" style="width: 200px" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="userForm.email" style="width: 200px" />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="手机号">
                <el-input v-model="userForm.mobile" style="width: 200px" />
              </el-form-item>
              <el-form-item label="性别">
                <el-select v-model="userForm.gender" placeholder="请选择" style="width: 200px">
                  <el-option label="男" value="1" />
                  <el-option label="女" value="2" />
                  <el-option label="保密" value="0" />
                </el-select>
              </el-form-item>
              <el-form-item label="部门">
                <el-input v-model="userForm.deptName" disabled style="width: 200px" />
              </el-form-item>
            </div>
            <div class="form-row">
              <el-form-item label="角色">
                <el-input v-model="userForm.roleNames" disabled style="width: 200px" />
              </el-form-item>
              <el-form-item label="注册时间">
                <el-input v-model="userForm.createTime" disabled style="width: 200px" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateUserInfo">保存修改</el-button>
              </el-form-item>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, User } from '@element-plus/icons-vue'
import DocumentList from '../document/DocumentList.vue'
import { getUserProfile, updateUserProfile } from '@/api/user'

const loading = ref(false)
const activeTab = ref('uploaded')
const activeMenu = ref('info') // 默认显示个人信息
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = reactive({
  id: '',
  username: '',
  nickname: '',
  email: '',
  mobile: '',
  gender: '',
  avatar: '',
  deptName: '',
  roleNames: '',
  createTime: ''
})

const userForm = reactive({
  username: '',
  nickname: '',
  email: '',
  mobile: '',
  gender: ''
})

const userStats = reactive({
  docCount: 0,
  favoriteCount: 0,
  viewCount: 0,
  downloadCount: 0
})

onMounted(() => {
  loadUserInfo()
  loadUserStats()
})

function loadUserInfo() {
  loading.value = true
  getUserProfile((data) => {
    userInfo.id = data.id
    userInfo.username = data.username
    userInfo.nickname = data.nickname
    userInfo.email = data.email
    userInfo.mobile = data.mobile
    userInfo.gender = data.gender
    userInfo.avatar = data.avatar
    userInfo.deptName = data.deptName
    userInfo.roleNames = data.roleNames
    userInfo.createTime = data.createTime

    // 填充表单
    userForm.username = data.username
    userForm.nickname = data.nickname
    userForm.email = data.email
    userForm.mobile = data.mobile
    userForm.gender = data.gender

    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载用户信息失败')
    loading.value = false
  })
}

function loadUserStats() {
  // 模拟加载用户统计数据
  userStats.docCount = 12
  userStats.favoriteCount = 5
  userStats.viewCount = 128
  userStats.downloadCount = 36
}

function updateUserInfo() {
  loading.value = true
  updateUserProfile(userForm, () => {
    ElMessage.success('个人信息更新成功')
    loadUserInfo()
    loading.value = false
  }, (msg) => {
    ElMessage.error(msg || '更新失败')
    loading.value = false
  })
}
</script>

<style scoped>
.profile-page {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.profile-container {
  display: flex;
  gap: 20px;
  height: calc(100vh - 40px);
}

/* 左侧边栏 */
.profile-sidebar {
  width: 280px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.user-info-section {
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.avatar-section {
  text-align: center;
  margin-bottom: 16px;
}

.nickname {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.username {
  font-size: 12px;
  color: #999;
  margin: 0 0 16px 0;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.sidebar-menu {
  flex: 1;
  padding: 16px 8px;
}

.profile-menu {
  border-right: none;
  background: transparent;
}

:deep(.el-menu-item) {
  margin: 4px 8px;
  border-radius: 8px;
  height: 40px;
}

:deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #ff9800, #ff5722) !important;
  color: white !important;
}

:deep(.el-menu-item:hover:not(.is-active)) {
  background-color: #fff0e6;
}

/* 右侧内容 */
.profile-content {
  flex: 1;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.content-section {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-form {
  max-width: 100%;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  align-items: center;
}

.form-row .el-form-item {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .profile-container {
    flex-direction: column;
    height: auto;
  }

  .profile-sidebar {
    width: 100%;
    margin-bottom: 20px;
  }

  .user-stats {
    justify-content: center;
    gap: 20px;
  }

  .form-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
