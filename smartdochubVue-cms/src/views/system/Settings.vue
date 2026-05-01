<template>
  <div class="settings">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
          <el-button type="warning" size="small" @click="handleRefreshCache">刷新缓存</el-button>
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
              <el-button type="primary" @click="saveBasicConfig" :loading="saving">保存</el-button>
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
              <el-button type="primary" @click="saveUploadConfig" :loading="saving">保存</el-button>
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
              <el-button type="primary" @click="saveThresholdConfig" :loading="saving">保存</el-button>
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
              <el-button type="primary" @click="saveAlgorithmConfig" :loading="saving">保存</el-button>
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
import { getConfigPage, saveConfig, updateConfig, refreshConfigCache } from '@/api/admin'

const activeTab = ref('basic')
const basicFormRef = ref()
const uploadFormRef = ref()
const thresholdFormRef = ref()
const algorithmFormRef = ref()
const saving = ref(false)

const configMap = ref({})

const basicConfig = reactive({
  siteName: 'SmartDocHub',
  logo: '',
  enableRegister: true,
  enableAudit: true
})

const uploadConfig = reactive({
  maxFileSize: 50,
  allowedFileTypes: ['pdf', 'docx', 'xlsx', 'pptx', 'txt']
})

const thresholdConfig = reactive({
  duplicateThreshold: 80,
  aiThreshold: 70
})

const algorithmConfig = reactive({
  enableAiDetection: true,
  enableDuplicateDetection: true
})

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

const CONFIG_KEY_MAP = {
  site_name: { group: 'basic', field: 'siteName' },
  site_logo: { group: 'basic', field: 'logo' },
  site_enable_register: { group: 'basic', field: 'enableRegister', type: 'boolean' },
  site_enable_audit: { group: 'basic', field: 'enableAudit', type: 'boolean' },
  upload_max_file_size: { group: 'upload', field: 'maxFileSize', type: 'number' },
  upload_allowed_file_types: { group: 'upload', field: 'allowedFileTypes', type: 'json' },
  threshold_duplicate: { group: 'threshold', field: 'duplicateThreshold', type: 'number' },
  threshold_ai: { group: 'threshold', field: 'aiThreshold', type: 'number' },
  algorithm_enable_ai_detection: { group: 'algorithm', field: 'enableAiDetection', type: 'boolean' },
  algorithm_enable_duplicate_detection: { group: 'algorithm', field: 'enableDuplicateDetection', type: 'boolean' }
}

const GROUP_CONFIG = {
  basic: basicConfig,
  upload: uploadConfig,
  threshold: thresholdConfig,
  algorithm: algorithmConfig
}

function parseConfigValue(value, type) {
  if (value === null || value === undefined) return value
  if (type === 'boolean') return value === 'true' || value === true
  if (type === 'number') return Number(value)
  if (type === 'json') {
    try { return JSON.parse(value) } catch { return value }
  }
  return value
}

function serializeConfigValue(value, type) {
  if (type === 'boolean') return value ? 'true' : 'false'
  if (type === 'number') return String(value)
  if (type === 'json') return JSON.stringify(value)
  return String(value || '')
}

function handleLogoUpload(response) {
  if (response.code === '00000') {
    basicConfig.logo = response.data.url
    ElMessage.success('Logo上传成功')
  } else {
    ElMessage.error('Logo上传失败')
  }
}

function handleUploadError() {
  ElMessage.error('上传失败，请重试')
}

function saveGroupConfigs(group) {
  const promises = []
  Object.entries(CONFIG_KEY_MAP).forEach(([key, mapping]) => {
    if (mapping.group !== group) return
    const configData = GROUP_CONFIG[group]
    const value = configData[mapping.field]
    const serialized = serializeConfigValue(value, mapping.type)
    const existing = configMap.value[key]

    if (existing && existing.id) {
      promises.push(new Promise((resolve, reject) => {
        updateConfig(existing.id, {
          id: existing.id,
          configName: existing.configName,
          configKey: key,
          configValue: serialized,
          remark: existing.remark
        }, () => resolve(), (msg) => reject(msg))
      }))
    } else {
      const nameMap = {
        site_name: '网站名称', site_logo: '网站Logo',
        site_enable_register: '是否开放注册', site_enable_audit: '是否开启审核',
        upload_max_file_size: '最大文件大小(MB)', upload_allowed_file_types: '允许的文件类型',
        threshold_duplicate: '重复率阈值(%)', threshold_ai: 'AI率阈值(%)',
        algorithm_enable_ai_detection: '启用AI检测', algorithm_enable_duplicate_detection: '启用重复检测'
      }
      promises.push(new Promise((resolve, reject) => {
        saveConfig({
          configName: nameMap[key] || key,
          configKey: key,
          configValue: serialized
        }, (data) => {
          configMap.value[key] = { id: data, configKey: key, configName: nameMap[key] || key, remark: '' }
          resolve()
        }, (msg) => reject(msg))
      }))
    }
  })
  return promises
}

function saveBasicConfig() {
  basicFormRef.value.validate((valid) => {
    if (!valid) return
    saving.value = true
    Promise.all(saveGroupConfigs('basic'))
      .then(() => ElMessage.success('保存成功'))
      .catch((msg) => ElMessage.error(msg || '保存失败'))
      .finally(() => { saving.value = false })
  })
}

function saveUploadConfig() {
  uploadFormRef.value.validate((valid) => {
    if (!valid) return
    saving.value = true
    Promise.all(saveGroupConfigs('upload'))
      .then(() => ElMessage.success('保存成功'))
      .catch((msg) => ElMessage.error(msg || '保存失败'))
      .finally(() => { saving.value = false })
  })
}

function saveThresholdConfig() {
  thresholdFormRef.value.validate((valid) => {
    if (!valid) return
    saving.value = true
    Promise.all(saveGroupConfigs('threshold'))
      .then(() => ElMessage.success('保存成功'))
      .catch((msg) => ElMessage.error(msg || '保存失败'))
      .finally(() => { saving.value = false })
  })
}

function saveAlgorithmConfig() {
  saving.value = true
  Promise.all(saveGroupConfigs('algorithm'))
    .then(() => ElMessage.success('保存成功'))
    .catch((msg) => ElMessage.error(msg || '保存失败'))
    .finally(() => { saving.value = false })
}

function handleRefreshCache() {
  refreshConfigCache(() => {
    ElMessage.success('缓存刷新成功')
  }, (msg) => {
    ElMessage.error(msg || '缓存刷新失败')
  })
}

function loadConfigs() {
  getConfigPage({ pageNum: 1, pageSize: 100 }, (data) => {
    const records = data.records || data.list || []
    const map = {}
    records.forEach(item => {
      map[item.configKey] = item
      const mapping = CONFIG_KEY_MAP[item.configKey]
      if (mapping) {
        const configData = GROUP_CONFIG[mapping.group]
        if (configData) {
          configData[mapping.field] = parseConfigValue(item.configValue, mapping.type)
        }
      }
    })
    configMap.value = map
  }, (msg) => {
    ElMessage.error(msg || '加载配置失败')
  })
}

onMounted(() => {
  loadConfigs()
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
