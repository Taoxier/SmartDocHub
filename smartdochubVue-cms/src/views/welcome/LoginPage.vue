<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 100px">
      <div style="font-size: 28px;font-weight: bold;color: #ff9800">SmartDocHub</div>
      <div style="font-size: 14px;color: #999;margin-top: 8px">智能原创文档文库系统</div>
    </div>
    <div style="margin-top: 40px">
      <el-form :model="loginForm" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入管理员用户名" size="large">
            <template #prefix>
              <el-icon>
                <UserFilled />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" size="large" placeholder="请输入密码" show-password>
            <template #prefix>
              <el-icon>
                <Lock />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-row style="margin-top: 5px">
          <el-col :span="24" style="text-align: left">
            <el-form-item prop="remember">
              <el-checkbox v-model="loginForm.remember" label="记住我" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div style="margin-top: 30px">
      <el-button @click="handleLogin" style="width: 100%" type="warning" size="large" :loading="loading">登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, Lock } from '@element-plus/icons-vue'
import { login } from '@/net'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

function handleLogin() {
  formRef.value.validate((valid) => {
    if (valid) {
      loading.value = true
      login(loginForm.username, loginForm.password, loginForm.remember, (data) => {
        loading.value = false
        router.push('/admin/dashboard')
      }, (message) => {
        loading.value = false
        ElMessage.error(message || '登录失败，请检查用户名和密码')
      })
    }
  })
}
</script>

<style scoped>
:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-button--warning) {
  background: linear-gradient(135deg, #ff9800, #ff5722);
  border: none;
}

:deep(.el-button--warning:hover) {
  background: linear-gradient(135deg, #ffb74d, #ff7043);
}
</style>