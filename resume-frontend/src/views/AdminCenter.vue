<script setup>
import { onMounted, ref, reactive, computed } from 'vue'
import http from '../utils/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const token = localStorage.getItem('token')
const role = localStorage.getItem('role')
if (!token || role !== 'admin') {
  router.replace('/login')
}

// 请求拦截器在 http 中统一设置

// 用户管理
const users = ref([])
const fetchUsers = async () => {
  const res = await http.get('/api/admin/user/list')
  if (res.data?.code === 1 || res.data?.code === 200) {
    users.value = res.data.data
    initForms() // 初始化表单数据
  }
}
const resetForms = ref({})
const editForms = ref({})

// 初始化表单数据
const initForms = () => {
  users.value.forEach(user => {
    if (!resetForms.value[user.id]) {
      resetForms.value[user.id] = ''
    }
    if (!editForms.value[user.id]) {
      editForms.value[user.id] = ''
    }
  })
}


const resetPwd = async (row) => {
  const password = resetForms.value[row.id]
  if (!password) { ElMessage.error('请输入新密码'); return }
  const res = await http.post(`/api/admin/user/${row.id}/reset-password`, { password })
  if (res.data?.code === 1 || res.data?.code === 200) { 
    ElMessage.success('重置成功')
    resetForms.value[row.id] = ''
  } else { 
    ElMessage.error(res.data?.message || res.data?.msg || '重置失败') 
  }
}

const editUser = async (row) => {
  const newUsername = editForms.value[row.id]
  if (!newUsername || !newUsername.trim()) { 
    ElMessage.error('请输入新用户名'); 
    return 
  }
  
  if (newUsername === row.username) {
    ElMessage.error('新用户名不能与当前用户名相同')
    return
  }
  
  const updateData = { username: newUsername.trim() }
  
  const res = await http.put(`/api/admin/user/${row.id}`, updateData)
  if (res.data?.code === 1 || res.data?.code === 200) { 
    ElMessage.success('用户名修改成功')
    editForms.value[row.id] = ''
    fetchUsers() // 重新获取用户列表
  } else { 
    ElMessage.error(res.data?.message || res.data?.msg || '修改失败') 
  }
}

const deleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？\n删除后该用户的所有简历也会被删除，此操作不可恢复！`, 
      '确认删除', 
      { 
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    
    const res = await http.delete(`/api/admin/user/${row.id}`)
    if (res.data?.code === 1 || res.data?.code === 200) {
      ElMessage.success('删除成功')
      fetchUsers() // 重新获取用户列表
      fetchAllResumes() // 重新获取简历列表
    } else {
      ElMessage.error(res.data?.message || res.data?.msg || '删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

// 简历管理（管理员）
const resumes = ref([])
const fetchAllResumes = async () => {
  const res = await http.get('/api/admin/resume/all')
  if (res.data?.code === 1 || res.data?.code === 200) resumes.value = res.data.data
}
const deleteResume = async (row) => {
  const res = await http.delete(`/api/admin/resume/${row.id}`)
  if (res.data?.code === 1 || res.data?.code === 200) { ElMessage.success('删除成功'); fetchAllResumes() } else { ElMessage.error(res.data?.message || res.data?.msg || '删除失败') }
}

// 查看简历详情（解析 content JSON）
const viewVisible = ref(false)
const viewData = ref(null)
const viewTheme = ref(null)
const themes = ref([])

// 加载主题列表
const loadThemes = async () => {
  const res = await http.get('/api/admin/themes')
  if (res.data?.code === 1 || res.data?.code === 200) themes.value = res.data.data
}

const openView = async (row) => {
  const res = await http.get(`/api/admin/resume/${row.id}`)
  if (res.data?.code === 1 || res.data?.code === 200) {
    const r = res.data.data
    try {
      viewData.value = {
        name: r.name,
        themeId: r.themeId,
        ...JSON.parse(r.content || '{}')
      }
      // 获取主题色
      viewTheme.value = themes.value.find(t => t.id === r.themeId)
      viewVisible.value = true
    } catch (e) {
      ElMessage.error('简历内容解析失败')
    }
  } else {
    ElMessage.error(res.data?.message || res.data?.msg || '获取简历失败')
  }
}

// 计算主题色样式
const viewThemeStyles = computed(() => {
  if (!viewTheme.value) return {}
  const primaryColor = viewTheme.value.primaryColor
  return {
    '--theme-primary': primaryColor,
    '--theme-primary-light': lightenColor(primaryColor, 0.1),
    '--theme-primary-lighter': lightenColor(primaryColor, 0.2),
    '--theme-primary-dark': darkenColor(primaryColor, 0.1),
    '--theme-bg': lightenColor(primaryColor, 0.35),
    '--theme-bg-light': lightenColor(primaryColor, 0.4)
  }
})

// 颜色处理函数
const lightenColor = (color, amount) => {
  const hex = color.replace('#', '')
  const r = Math.min(255, parseInt(hex.substr(0, 2), 16) + Math.round(255 * amount))
  const g = Math.min(255, parseInt(hex.substr(2, 2), 16) + Math.round(255 * amount))
  const b = Math.min(255, parseInt(hex.substr(4, 2), 16) + Math.round(255 * amount))
  return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`
}

const darkenColor = (color, amount) => {
  const hex = color.replace('#', '')
  const r = Math.max(0, parseInt(hex.substr(0, 2), 16) - Math.round(255 * amount))
  const g = Math.max(0, parseInt(hex.substr(2, 2), 16) - Math.round(255 * amount))
  const b = Math.max(0, parseInt(hex.substr(4, 2), 16) - Math.round(255 * amount))
  return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`
}

onMounted(async () => {
  try {
    await fetchUsers()
    await fetchAllResumes()
    await loadThemes()
  } catch (error) {
    console.error('AdminCenter onMounted error:', error)
    ElMessage.error('加载数据失败')
  }
})

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.replace('/')
}
</script>

<template>
  <div style="padding:16px;">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
      <h3>管理中心</h3>
      <el-button @click="logout">退出登录</el-button>
    </div>

    <el-card style="margin-bottom:16px;">
      <template #header>用户管理</template>
      <el-table :data="users" size="small" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="role" label="权限" width="120" />
        <el-table-column label="修改用户名" width="300">
          <template #default="{ row }">
            <div style="display:flex;gap:8px;align-items:center;">
              <el-input 
                v-model="editForms[row.id]"
                placeholder="新用户名" 
                style="width:150px;" 
              />
              <el-button size="small" type="primary" @click="editUser(row)">修改</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="重置密码" width="200">
          <template #default="{ row }">
            <el-input 
              v-model="resetForms[row.id]"
              placeholder="新密码" 
              style="width:120px;margin-right:8px;" 
            />
            <el-button size="small" @click="resetPwd(row)">重置</el-button>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" type="danger" @click="deleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card>
      <template #header>简历管理</template>
      <el-table :data="resumes" size="small" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="name" label="简历名称" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button size="small" @click="() => openView(row)">查看</el-button>
            <el-popconfirm title="确定删除该简历吗？" @confirm="() => deleteResume(row)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="viewVisible" title="简历详情" width="800px" :style="viewThemeStyles">
      <div v-if="viewData" class="admin-resume-view">
        <div class="resume-header">
          <h2>{{ viewData.name }}</h2>
          <div v-if="viewTheme" class="theme-info">
            <span class="theme-label">主题：</span>
            <span class="theme-name">{{ viewTheme.name }}</span>
            <span class="theme-color" :style="{ backgroundColor: viewTheme.primaryColor }"></span>
          </div>
        </div>

        <div class="resume-section">
          <h3 class="section-title">个人信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">姓名：</span>
              <span class="value">{{ viewData.personalInfo?.name }}</span>
            </div>
            <div class="info-item">
              <span class="label">岗位：</span>
              <span class="value">{{ viewData.personalInfo?.title }}</span>
            </div>
            <div class="info-item">
              <span class="label">电话：</span>
              <span class="value">{{ viewData.personalInfo?.phone }}</span>
            </div>
            <div class="info-item">
              <span class="label">邮箱：</span>
              <span class="value">{{ viewData.personalInfo?.email }}</span>
            </div>
            <div class="info-item">
              <span class="label">地址：</span>
              <span class="value">{{ viewData.personalInfo?.location }}</span>
            </div>
          </div>
        </div>

        <div class="resume-section">
          <h3 class="section-title">个人简介</h3>
          <p class="summary-text">{{ viewData.summary }}</p>
        </div>

        <div class="resume-section">
          <h3 class="section-title">教育背景</h3>
          <div v-for="(e,i) in viewData.education || []" :key="i" class="experience-item">
            <div class="experience-header">
              <span class="main-title">{{ e.school }}</span>
              <span class="time">{{ e.startDate }} - {{ e.endDate }}</span>
            </div>
            <div class="experience-detail">
              <span class="sub-title">{{ e.major }}（{{ e.degree }}）</span>
            </div>
          </div>
        </div>

        <div class="resume-section">
          <h3 class="section-title">工作经历</h3>
          <div v-for="(w,i) in viewData.workExperience || []" :key="i" class="experience-item">
            <div class="experience-header">
              <span class="main-title">{{ w.company }} - {{ w.position }}</span>
              <span class="time">{{ w.startDate }} - {{ w.endDate }}</span>
            </div>
            <div class="experience-detail">
              <p>{{ w.description }}</p>
            </div>
          </div>
        </div>

        <div class="resume-section">
          <h3 class="section-title">项目经历</h3>
          <div v-for="(p,i) in viewData.projectExperience || []" :key="i" class="experience-item">
            <div class="experience-header">
              <span class="main-title">{{ p.name }} - {{ p.role }}</span>
              <span class="time">{{ p.startDate }} - {{ p.endDate }}</span>
            </div>
            <div class="experience-detail">
              <p><strong>技术栈：</strong>{{ Array.isArray(p.technologies) ? p.technologies.join(', ') : (p.technologies || '') }}</p>
              <p>{{ p.description }}</p>
            </div>
          </div>
        </div>

        <div class="resume-section">
          <h3 class="section-title">技能特长</h3>
          <div class="skills-container">
            <el-tag v-for="(skill,i) in viewData.skills || []" :key="i" class="skill-tag">{{ skill }}</el-tag>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="viewVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-resume-view {
  background: linear-gradient(135deg, var(--theme-bg, #f8f9fa) 0%, var(--theme-bg-light, #ffffff) 100%);
  padding: 20px;
  border-radius: 8px;
  border: 2px solid var(--theme-primary, #409eff);
}

.resume-header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid var(--theme-primary, #409eff);
}

.resume-header h2 {
  color: var(--theme-primary, #409eff);
  margin: 0 0 8px 0;
}

.theme-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.theme-label {
  color: #666;
  font-size: 14px;
}

.theme-name {
  color: var(--theme-primary-dark, #337ecc);
  font-weight: bold;
}

.theme-color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 4px rgba(0, 0, 0, 0.2);
}

.resume-section {
  margin-bottom: 24px;
  padding: 16px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--theme-primary-lighter, #b3d8ff);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-title {
  color: var(--theme-primary, #409eff);
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 2px solid var(--theme-primary, #409eff);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  font-weight: bold;
  color: #666;
  min-width: 50px;
  font-size: 14px;
}

.value {
  color: #333;
  font-size: 14px;
}

.summary-text {
  line-height: 1.6;
  color: #555;
  font-size: 14px;
}

.experience-item {
  margin-bottom: 12px;
  padding: 12px;
  background: var(--theme-bg-light, #fafafa);
  border-radius: 6px;
  border-left: 3px solid var(--theme-primary, #409eff);
}

.experience-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.main-title {
  font-weight: bold;
  color: #333;
  font-size: 14px;
}

.sub-title {
  color: #666;
  font-size: 13px;
}

.time {
  color: var(--theme-primary-dark, #337ecc);
  font-size: 11px;
  background: var(--theme-primary-lighter, #e8f4fd);
  padding: 2px 6px;
  border-radius: 3px;
  border: 1px solid var(--theme-primary-light, #b3d8ff);
}

.experience-detail p {
  margin: 2px 0;
  line-height: 1.4;
  color: #555;
  font-size: 13px;
}

.skills-container {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.skill-tag {
  margin: 0;
  background: var(--theme-primary-lighter, #e8f4fd);
  color: var(--theme-primary-dark, #337ecc);
  border: 1px solid var(--theme-primary-light, #b3d8ff);
  font-size: 12px;
}
</style>


