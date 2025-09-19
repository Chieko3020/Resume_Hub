<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../utils/http'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

// 注册相关
const isRegister = ref(false)
const registerForm = reactive({ username: '', password: '', confirmPassword: '' })
const registerLoading = ref(false)

const onSubmit = async () => {
  if (isRegister.value) {
    await onRegister()
    return
  }
  
  if (!form.username || !form.password) {
    ElMessage.error('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await http.post('/api/user/login', form)
    const ok = res.data?.code === 1 || res.data?.code === 200
    if (ok) {
      const { token, role } = res.data.data
      localStorage.setItem('token', token)
      localStorage.setItem('role', role)
      ElMessage.success('登录成功')
      router.replace(role === 'admin' ? '/admin' : '/user')
    } else {
      ElMessage.error(res.data?.message || res.data?.msg || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

const onRegister = async () => {
  if (!registerForm.username || !registerForm.password || !registerForm.confirmPassword) {
    ElMessage.error('请填写完整信息')
    return
  }
  if (registerForm.password !== registerForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (registerForm.password.length < 6) {
    ElMessage.error('密码长度至少6位')
    return
  }
  
  registerLoading.value = true
  try {
    const res = await http.post('/api/user/register', {
      username: registerForm.username,
      password: registerForm.password
    })
    const ok = res.data?.code === 1 || res.data?.code === 200
    if (ok) {
      ElMessage.success('注册成功，请登录')
      isRegister.value = false
      registerForm.username = ''
      registerForm.password = ''
      registerForm.confirmPassword = ''
    } else {
      ElMessage.error(res.data?.message || res.data?.msg || '注册失败')
    }
  } finally {
    registerLoading.value = false
  }
}

// 自助重置密码弹窗
const resetDialogVisible = ref(false)
const rp = reactive({ username: '', password: '' })
const resetLoading = ref(false)
const showResetDialog = () => {
  resetDialogVisible.value = true
  rp.username = ''
  rp.password = ''
}
const onReset = async () => {
  if (!rp.username || !rp.password) {
    ElMessage.error('请输入用户名和新密码')
    return
  }
  resetLoading.value = true
  try {
    const res = await http.post('/api/user/reset-password', rp)
    if (res.data?.code === 1 || res.data?.code === 200) {
      ElMessage.success('密码重置成功，请登录')
      resetDialogVisible.value = false
      rp.username = ''
      rp.password = ''
    } else {
      ElMessage.error(res.data?.message || res.data?.msg || '重置失败')
    }
  } finally {
    resetLoading.value = false
  }
}
</script>

<template>
  <div style="display:flex;justify-content:center;align-items:center;height:100vh;background:#f5f7fa;">
    <el-card shadow="hover" style="width:520px;">
      <h3 style="margin:0 0 16px;">{{ isRegister ? '注册' : '登录' }}</h3>
      
      <!-- 登录表单 -->
      <el-form v-if="!isRegister" :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" autocomplete="current-password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="onSubmit">登录</el-button>
          <el-button type="text" @click="showResetDialog" style="margin-left:12px;">忘记密码？</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="text" @click="isRegister = true" style="width:100%;">还没有账号？立即注册</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 注册表单 -->
      <el-form v-else :model="registerForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="registerForm.username" autocomplete="username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="registerForm.password" type="password" autocomplete="new-password" placeholder="请输入密码（至少6位）" />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="registerForm.confirmPassword" type="password" autocomplete="new-password" placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="registerLoading" @click="onSubmit">注册</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="text" @click="isRegister = false" style="width:100%;">已有账号？立即登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetDialogVisible" title="重置密码" width="400px">
      <el-form :model="rp" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="rp.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="rp.password" type="password" placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetLoading" @click="onReset">重置密码</el-button>
      </template>
    </el-dialog>
  </div>
  
</template>

<style scoped>
</style>


