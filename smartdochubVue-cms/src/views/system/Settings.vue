<template>
  <div class="settings">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基础配置" name="basic">
          <el-form :model="basicConfig" :rules="basicRules" ref="basicFormRef" label-width="120px">
            <el-form-item label="网站名称" prop="siteName">
              <el-input v-model="basicConfig.siteName" placeholder="请输入网站名称" maxlength="50"></el-input>
            </el-form-item>
            
            <el-form-item label="网站Logo">
              <el-upload
                class="upload-demo"
                action="/api/v1/upload"
                :on-success="handleLogoUpload"
                :on-error="handleUploadError"
                :show-file-list="false"
                accept=".jpg,.jpeg,.png,.gif"
              >
                <el-button type="primary">上传Logo</el-button>
                <template #tip>
                  <div class="el-upload__tip">
                    支持上传 jpg、jpeg、png、gif 格式的图片
                  </div>
                </template>
              </el-upload>
              <el-image v-if="basicConfig.logo" :src="basicConfig.logo" style="width: 100px; height: 100px; margin-top: 10px"></el-image>
            </el-form-item>
            
            <el-form-item label="是否开放注册" prop="enableRegister">
              <el-switch v-model="basicConfig.enableRegister" active-text="开放" inactive-text="关闭"></el-switch>
            </el-form-item>
            
            <el-form-item label="是否开启审核" prop="enableAudit">
              <el-switch v-model="basicConfig.enableAudit" active-text="开启" inactive-text="关闭"></el-switch>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveBasicConfig">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="上传限制" name="upload">
          <el-form :model="uploadConfig" :rules="uploadRules" ref="uploadFormRef" label-width="120px">
            <el-form-item label="最大文件大小" prop="maxFileSize">
              <el-input-number v-model="uploadConfig.maxFileSize" :min="1" :max="100" :step="1" style="width: 150px"></el-input-number>
              <span style="margin-left: 10px">MB</span>
            </el-form-item>
            
            <el-form-item label="允许的文件类型" prop="allowedFileTypes">
              <el-select v-model="uploadConfig.allowedFileTypes" multiple placeholder="请选择文件类型">
                <el-option label="PDF" value="pdf"></el-option>
                <el-option label="Word" value="docx"></el-option>
                <el-option label="Excel" value="xlsx"></el-option>
                <el-option label="PowerPoint" value="pptx"></el-option>
                <el-option label="TXT" value="txt"></el-option>
              </el-select>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveUploadConfig">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="检测阈值" name="threshold">
          <el-form :model="thresholdConfig" :rules="thresholdRules" ref="thresholdFormRef" label-width="120px">
            <el-form-item label="重复率阈值" prop="duplicateThreshold">
              <el-input-number v-model="thresholdConfig.duplicateThreshold" :min="0" :max="100" :step="1" style="width: 150px"></el-input-number>
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            
            <el-form-item label="AI率阈值" prop="aiThreshold">
              <el-input-number v-model="thresholdConfig.aiThreshold" :min="0" :max="100" :step="1" style="width: 150px"></el-input-number>
              <span style="margin-left: 10px">%</span>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveThresholdConfig">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="算法开关" name="algorithm">
          <el-form :model="algorithmConfig" ref="algorithmFormRef" label-width="120px">
            <el-form-item label="启用AI检测">
              <el-switch v-model="algorithmConfig.enableAiDetection" active-text="启用" inactive-text="禁用"></el-switch>
            </el-form-item>
            
            <el-form-item label="启用重复检测">
              <el-switch v-model="algorithmConfig.enableDuplicateDetection" active-text="启用" inactive-text="禁用"></el-switch>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveAlgorithmConfig">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')
const basicFormRef = ref()
const uploadFormRef = ref()
const thresholdFormRef = ref()
const algorithmFormRef = ref()

// 基础配置
const basicConfig = reactive({
  siteName: 'SmartDocHub',
  logo: '',
  enableRegister: true,
  enableAudit: true
})

// 上传配置
const uploadConfig = reactive({
  maxFileSize: 50,
  allowedFileTypes: ['pdf', 'docx', 'xlsx', 'pptx', 'txt']
})

// 检测阈值配置
const thresholdConfig = reactive({
  duplicateThreshold: 80,
  aiThreshold: 70
})

// 算法配置
const algorithmConfig = reactive({
  enableAiDetection: true,
  enableDuplicateDetection: true
})

// 表单验证规则
const basicRules = {
  siteName: [
    { required: true, message: '请输入网站名称', trigger: 'blur' }
  ]
}

const uploadRules = {
  maxFileSize: [
    { required: true, message: '请输入最大文件大小', trigger: 'blur' }
  ],
  allowedFileTypes: [
    { required: true, message: '请选择允许的文件类型', trigger: 'blur' }
  ]
}

const thresholdRules = {
  duplicateThreshold: [
    { required: true, message: '请输入重复率阈值', trigger: 'blur' }
  ],
  aiThreshold: [
    { required: true, message: '请输入AI率阈值', trigger: 'blur' }
  ]
}

// 处理Logo上传
function handleLogoUpload(response) {
  if (response.code === '00000') {
    basicConfig.logo = response.data.url
    ElMessage.success('Logo上传成功')
  } else {
    ElMessage.error('Logo上传失败')
  }
}

// 处理上传错误
function handleUploadError() {
  ElMessage.error('上传失败，请重试')
}

// 保存基础配置
function saveBasicConfig() {
  basicFormRef.value.validate((valid) => {
    if (valid) {
      console.log('基础配置:', basicConfig)
      // 这里可以添加保存配置的API调用
      ElMessage.success('保存成功')
    }
  })
}

// 保存上传配置
function saveUploadConfig() {
  uploadFormRef.value.validate((valid) => {
    if (valid) {
      console.log('上传配置:', uploadConfig)
      // 这里可以添加保存配置的API调用
      ElMessage.success('保存成功')
    }
  })
}

// 保存检测阈值配置
function saveThresholdConfig() {
  thresholdFormRef.value.validate((valid) => {
    if (valid) {
      console.log('检测阈值配置:', thresholdConfig)
      // 这里可以添加保存配置的API调用
      ElMessage.success('保存成功')
    }
  })
}

// 保存算法配置
function saveAlgorithmConfig() {
  console.log('算法配置:', algorithmConfig)
  // 这里可以添加保存配置的API调用
  ElMessage.success('保存成功')
}

// 初始化数据
function initData() {
  // 这里可以添加获取配置的API调用
  // 模拟数据已经在reactive中设置
}

onMounted(() => {
  initData()
})
</script>

<style scoped>
.settings {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-demo {
  margin-bottom: 10px;
}
</style>