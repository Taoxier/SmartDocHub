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
          <el-button type="primary" @click="submitUpload" :loading="isUploading">开始上传</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Check, Close, Loading } from '@element-plus/icons-vue'
import { uploadDocument } from '@/api/document'

const uploadForm = reactive({
  title: '',
  description: '',
  categoryId: '',
  tags: '',
  isPublic: true,
  files: []
})

const rules = {
  title: [
    { required: true, message: '请输入文档标题', trigger: 'blur' }
  ]
}

const formRef = ref()
const uploadRef = ref()
const fileList = ref([])
const isUploading = ref(false)

const categories = ref([
  { id: 1, name: '技术文档' },
  { id: 2, name: '学术论文' },
  { id: 3, name: '工作报告' },
  { id: 4, name: '其他文档' }
])

const emit = defineEmits(['refresh'])

function handleFileChange(file, files) {
  uploadForm.files = files.map(item => item.raw)
}

function submitUpload() {
  formRef.value.validate((valid) => {
    if (valid) {
      if (uploadForm.files.length === 0) {
        ElMessage.error('请选择文件')
        return
      }

      isUploading.value = true
      ElMessage({ message: '提交成功，正在分析和审核文件', type: 'success', duration: 3000 })

      const fileErrors = []
      const fileSuccess = []

      const uploadPromises = uploadForm.files.map((file, index) => {
        return new Promise((resolve) => {
          uploadDocument(file,
            (progress) => {
              const fileItem = fileList.value.find(item => item.raw === file)
              if (fileItem) {
                fileItem.status = 'uploading'
                fileItem.percentage = progress
              }
            },
            (data) => {
              fileSuccess.push({ index, name: file.name, data })
              const fileItem = fileList.value.find(item => item.raw === file)
              if (fileItem) {
                fileItem.status = 'success'
              }
              resolve({ success: true, data })
            },
            (errorMsg, errorCode) => {
              fileErrors.push({ index, name: file.name, errorMsg, errorCode })
              const fileItem = fileList.value.find(item => item.raw === file)
              if (fileItem) {
                fileItem.status = 'error'
              }
              resolve({ success: false, errorMsg, errorCode })
            }
          )
        })
      })

      Promise.all(uploadPromises).then(results => {
        isUploading.value = false

        const successCount = fileSuccess.length
        const totalCount = results.length

        if (successCount > 0) {
          ElMessage({
            message: `上传成功！成功上传 ${successCount}/${totalCount} 个文件`,
            type: 'success',
            duration: 5000
          })
          resetForm()
          emit('refresh')
        }

        if (fileErrors.length > 0) {
          fileErrors.forEach(fileError => {
            ElMessage.error(`${fileError.name} 上传失败: ${fileError.errorMsg || '未知错误'}`)
          })
        }

        if (successCount === 0 && fileErrors.length > 0) {
          resetForm()
        }
      }).catch(error => {
        isUploading.value = false
        ElMessage.error('上传过程中发生错误: ' + (error.message || '未知错误'))
        resetForm()
      })
    }
  })
}

function resetForm() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  fileList.value = []
  uploadForm.files = []
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
