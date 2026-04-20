<template>
  <div class="profile-page">
    <div class="profile-header">
      <h2 class="page-title">个人信息</h2>
    </div>

    <div class="profile-content">
      <div class="profile-card">
        <div class="profile-avatar">
          <el-avatar :size="80" style="background-color: #ff9800">
            {{ userInfo?.username?.charAt(0) || 'U' }}
          </el-avatar>
          <h3 class="username">{{ userInfo?.username || '用户' }}</h3>
        </div>

        <el-form :model="profileForm" label-width="120px" class="profile-form">
          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" disabled />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="profileForm.phone" />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" />
          </el-form-item>
          <el-form-item label="个人简介">
            <el-input v-model="profileForm.bio" type="textarea" :rows="4" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="saveProfile">保存修改</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '@/net'

const userInfo = ref(null)
const profileForm = reactive({
  username: '',
  email: '',
  phone: '',
  nickname: '',
  bio: ''
})

onMounted(() => {
  loadUserInfo()
})

function loadUserInfo() {
  const info = getUserInfo()
  userInfo.value = info
  if (info) {
    profileForm.username = info.username || ''
    profileForm.email = info.email || ''
    profileForm.phone = info.phone || ''
    profileForm.nickname = info.nickname || ''
    profileForm.bio = info.bio || ''
  }
}

function saveProfile() {
  // 模拟保存操作
  ElMessage.success('个人信息保存成功')
}

function resetForm() {
  loadUserInfo()
}
</script>

<style scoped>
.profile-page {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.profile-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.profile-content {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.profile-card {
  width: 100%;
  max-width: 600px;
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.profile-avatar {
  text-align: center;
  margin-bottom: 32px;
}

.username {
  margin-top: 16px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.profile-form {
  margin-top: 24px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>