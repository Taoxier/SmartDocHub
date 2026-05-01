<template>
  <div class="role-edit">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑角色' : '新增角色' }}</span>
        </div>
      </template>

      <el-form :model="roleForm" :rules="rules" ref="formRef" label-width="100px" v-loading="formLoading">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" maxlength="50"></el-input>
        </el-form-item>

        <el-form-item label="角色编码" prop="code">
          <el-input v-model="roleForm.code" placeholder="请输入角色编码" maxlength="20" :disabled="isEdit"></el-input>
        </el-form-item>

        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="roleForm.sort" :min="0" :max="9999"></el-input-number>
        </el-form-item>

        <el-form-item label="角色状态" prop="status">
          <el-radio-group v-model="roleForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="权限设置">
          <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedMenuIds"
            :props="{ label: 'name', children: 'children' }"
          ></el-tree>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
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
import { addRole, getRoleForm, updateRole, getRoleMenuIds, assignMenusToRole, getMenuList } from '@/api/admin'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const menuTreeRef = ref()
const formLoading = ref(false)
const submitting = ref(false)

const id = route.params.id
const isEdit = computed(() => !!id)

const roleForm = reactive({
  id: null,
  name: '',
  code: '',
  sort: 0,
  status: 1,
  dataScope: null
})

const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' }
  ]
}

const menuTree = ref([])
const checkedMenuIds = ref([])

function loadMenuTree() {
  getMenuList((data) => {
    menuTree.value = data || []
  }, (msg) => {
    ElMessage.error(msg || '加载菜单树失败')
  })
}

function loadRoleData() {
  if (!isEdit.value) return

  formLoading.value = true
  getRoleForm(id, (data) => {
    if (data) {
      roleForm.id = data.id
      roleForm.name = data.name || ''
      roleForm.code = data.code || ''
      roleForm.sort = data.sort || 0
      roleForm.status = data.status !== undefined ? data.status : 1
      roleForm.dataScope = data.dataScope
    }
    formLoading.value = false
  }, (msg) => {
    ElMessage.error(msg || '加载角色信息失败')
    formLoading.value = false
  })

  getRoleMenuIds(id, (data) => {
    checkedMenuIds.value = data || []
  }, (msg) => {
    ElMessage.error(msg || '加载角色权限失败')
  })
}

function submitForm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    submitting.value = true

    const formData = { ...roleForm }
    const saveApi = isEdit.value ? updateRole : addRole

    saveApi(formData, () => {
      const roleId = isEdit.value ? id : null

      const checkedKeys = menuTreeRef.value ? menuTreeRef.value.getCheckedKeys() : []
      const halfCheckedKeys = menuTreeRef.value ? menuTreeRef.value.getHalfCheckedKeys() : []
      const allMenuIds = [...checkedKeys, ...halfCheckedKeys]

      if (roleId && allMenuIds.length > 0) {
        assignMenusToRole(roleId, allMenuIds, () => {
          ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
          submitting.value = false
          router.push('/admin/roles')
        }, (msg) => {
          ElMessage.warning(isEdit.value ? '角色已保存，但权限分配失败' : '角色已创建，但权限分配失败')
          submitting.value = false
          router.push('/admin/roles')
        })
      } else {
        ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
        submitting.value = false
        router.push('/admin/roles')
      }
    }, (msg) => {
      ElMessage.error(msg || '保存失败')
      submitting.value = false
    })
  })
}

function resetForm() {
  formRef.value.resetFields()
  if (menuTreeRef.value) {
    menuTreeRef.value.setCheckedKeys([])
  }
}

function goBack() {
  router.push('/admin/roles')
}

onMounted(() => {
  loadMenuTree()
  loadRoleData()
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
</style>
