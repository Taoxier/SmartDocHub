<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 100px">
      <div style="font-size: 28px;font-weight: bold;color: #ff9800">SmartDocHub</div>
      <div style="font-size: 14px;color: #999;margin-top: 8px">智能原创文档文库系统</div>
    </div>
    <div style="margin-top: 40px">
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" maxlength="10" type="text" placeholder="用户名/邮箱" size="large">
            <template #prefix>
              <el-icon>
                <User />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" maxlength="20" size="large" placeholder="密码" show-password>
            <template #prefix>
              <el-icon>
                <Lock />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-row style="margin-top: 5px">
          <el-col :span="12" style="text-align: left">
            <el-form-item prop="remember">
              <el-checkbox v-model="form.remember" label="记住我" />
            </el-form-item>
          </el-col>
          <el-col :span="12" style="text-align: right">
            <el-link type="warning" @click="router.push('/forget')">忘记密码？</el-link>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div style="margin-top: 30px">
      <el-button @click="userLogin()" style="width: 100%" type="warning" size="large">立即登录</el-button>
    </div>
    <el-divider>
      <span style="color: #999;font-size: 13px">没有账号？</span>
    </el-divider>
    <div>
      <el-button style="width: 100%" size="large" @click="router.push('/register')" plain>注册账号</el-button>
    </div>
  </div>
</template>

<script setup>
import { User, Lock } from '@element-plus/icons-vue'
import router from "@/router";
import { reactive, ref } from "vue";
import { login } from '@/net'

const formRef = ref()
const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [
    { required: true, message: '请输入用户名' }
  ],
  password: [
    { required: true, message: '请输入密码' }
  ]
}

function userLogin() {
  formRef.value.validate((isValid) => {
    if (isValid) {
      login(form.username, form.password, form.remember, () => router.push("/home"))
    }
  });
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
