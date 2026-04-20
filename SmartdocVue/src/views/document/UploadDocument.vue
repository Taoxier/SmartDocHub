<template>
  <div class="upload-document">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>高级上传</span>
        </div>
      </template>

      <el-form :model="uploadForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="文档标题" prop="title">
          <el-input v-model="uploadForm.title" placeholder="请输入文档标题" maxlength="100"></el-input>
        </el-form-item>

        <el-form-item label="文档描述" prop="description">
          <el-input v-model="uploadForm.description" type="textarea" :rows="4" placeholder="请输入文档描述"
            maxlength="500"></el-input>
        </el-form-item>

        <el-form-item label="文档分类">
          <el-select v-model="uploadForm.categoryId" placeholder="请选择文档分类" style="width: 100%">
            <el-option v-for="category in categories" :key="category.id" :label="category.name"
              :value="category.id"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="文档标签">
          <el-input v-model="uploadForm.tags" placeholder="请输入标签，多个标签用逗号分隔" maxlength="100"></el-input>
          <div class="el-form-item__help">例如：技术,前端,Vue</div>
        </el-form-item>

        <el-form-item label="是否公开">
          <el-switch v-model="uploadForm.isPublic" active-text="公开" inactive-text="私有"></el-switch>
        </el-form-item>

        <el-form-item label="上传文件">
          <el-upload class="upload-demo" ref="uploadRef" :auto-upload="false" :on-change="handleFileChange"
            :file-list="fileList" :limit="10" accept=".doc,.docx,.pdf,.txt,.ppt,.pptx" drag>
            <el-icon class="el-icon--upload">
              <UploadFilled />
            </el-icon>
            <div class="el-upload__text">
              拖放文件到此处，或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持上传 doc、docx、pdf、txt、ppt、pptx 文件，单个文件大小不超过 50MB，最多同时上传 10 个文件
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitUpload">开始上传</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <el-progress v-if="uploadProgress > 0 && uploadProgress < 100" :percentage="uploadProgress"
        :format="formatProgress"></el-progress>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadDocument } from '@/api/document'

// 上传表单
const uploadForm = reactive({
  title: '',
  description: '',
  categoryId: '',
  tags: '',
  isPublic: true,
  files: []
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入文档标题', trigger: 'blur' }
  ]
}

// 表单引用
const formRef = ref()
const uploadRef = ref()

// 文件列表
const fileList = ref([])

// 上传进度
const uploadProgress = ref(0)

// 分类列表
const categories = ref([
  { id: 1, name: '技术文档' },
  { id: 2, name: '学术论文' },
  { id: 3, name: '工作报告' },
  { id: 4, name: '其他文档' }
])

// emit
const emit = defineEmits(['refresh'])

// 处理文件选择
function handleFileChange(file, fileList) {
  uploadForm.files = fileList.map(item => item.raw)
  fileList.value = fileList
}

// 格式化进度条
function formatProgress(percentage) {
  return `${percentage}%`
}

// 提交上传
function submitUpload() {
  formRef.value.validate((valid) => {
    if (valid) {
      if (uploadForm.files.length === 0) {
        ElMessage.error('请选择文件')
        return
      }

      uploadProgress.value = 0
      const totalFiles = uploadForm.files.length
      let uploadedCount = 0

      uploadForm.files.forEach(file => {
        uploadDocument(file,
          (percent) => {
            // 单个文件进度
            uploadProgress.value = Math.round((uploadedCount * 100 + percent) / totalFiles)
          },
          (data) => {
            uploadedCount++
            if (uploadedCount === totalFiles) {
              uploadProgress.value = 100
              ElMessage.success('上传成功')
              resetForm()
              // 触发列表刷新
              emit('refresh')
            }
          },
          (error) => {
            ElMessage.error('上传失败: ' + (error.message || '未知错误'))
          }
        )
      })
    }
  })
}

// 重置表单
function resetForm() {
  formRef.value.resetFields()
  fileList.value = []
  uploadForm.files = []
  uploadProgress.value = 0
}
</script>

<style scoped>
.upload-document {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-demo {
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  padding: 40px;
  text-align: center;
  transition: border-color 0.3s;
}

.upload-demo:hover {
  border-color: #ff9800;
}

.el-upload__text em {
  color: #ff9800;
}

.el-upload__tip {
  color: #909399;
  margin-top: 10px;
}
</style>