<template>
  <div class="role-edit">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑角色' : '新增角色' }}</span>
        </div>
      </template>
      
      <el-form :model="roleForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" maxlength="50"></el-input>
        </el-form-item>
        
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" maxlength="20" :disabled="isEdit"></el-input>
        </el-form-item>
        
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入角色描述"
            maxlength="200"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="权限设置">
          <el-tree
            :data="permissions"
            show-checkbox
            node-key="id"
            :default-checked-keys="roleForm.permissionIds"
            @check="handlePermissionCheck"
          >
            <template #default="{ node, data }">
              <span class="custom-tree-node">
                <span>{{ data.label }}</span>
              </span>
            </template>
          </el-tree>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref()

// 路由参数
const id = route.params.id
const isEdit = computed(() => !!id)

// 角色表单
const roleForm = reactive({
  roleName: '',
  roleCode: '',
  description: '',
  permissionIds: []
})

// 表单验证规则
const rules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' }
  ]
}

// 权限树数据
const permissions = ref([
  {
    id: 1,
    label: '系统管理',
    children: [
      { id: 11, label: '用户管理' },
      { id: 12, label: '角色管理' },
      { id: 13, label: '系统配置' },
      { id: 14, label: '日志管理' }
    ]
  },
  {
    id: 2,
    label: '文档管理',
    children: [
      { id: 21, label: '文档审核' },
      { id: 22, label: '文档管理' },
      { id: 23, label: '评论管理' }
    ]
  }
])

// 处理权限选择
function handlePermissionCheck() {
  // 这里可以处理权限选择逻辑
}

// 提交表单
function submitForm() {
  formRef.value.validate((valid) => {
    if (valid) {
      console.log('角色表单:', roleForm)
      // 这里可以添加保存角色的API调用
      ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
      router.push('/admin/roles')
    }
  })
}

// 重置表单
function resetForm() {
  formRef.value.resetFields()
  roleForm.permissionIds = []
}

// 返回列表
function goBack() {
  router.push('/admin/roles')
}

// 初始化数据
function initData() {
  if (isEdit.value) {
    // 这里可以添加获取角色详情的API调用
    // 模拟数据
    roleForm.roleName = '管理员'
    roleForm.roleCode = 'ADMIN'
    roleForm.description = '拥有系统管理权限'
    roleForm.permissionIds = [1, 11, 12, 13, 14, 2, 21, 22, 23]
  }
}

onMounted(() => {
  initData()
})
</script>

<style scoped>
.role-edit {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.custom-tree-node {
  font-size: 14px;
}
</style>