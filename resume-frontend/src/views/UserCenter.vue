<script setup>
import { onMounted, reactive, ref, computed, watch, nextTick } from 'vue'
import http from '../utils/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

// 检查token - 但不在顶层直接跳转
const checkAuth = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.replace('/login')
    return false
  }
  return true
}

// 拦截器在 http 中已设置

const resumes = ref([])
const currentId = ref(null)
const currentResume = computed(() => resumes.value.find(r => r.id === currentId.value))

// 文件上传相关
const userFile = ref(null)
const uploadDialogVisible = ref(false)
const fileDialogVisible = ref(false)
const uploadLoading = ref(false)
const uploadRef = ref()

const fetchResumes = async () => {
  const res = await http.get('/api/user/resume/list')
  if (res.data?.code === 1 || res.data?.code === 200) {
    resumes.value = res.data.data || []
    if (resumes.value.length && !currentId.value) {
      currentId.value = resumes.value[0].id
      // 确保主题色立即应用
      nextTick(() => {
        const theme = getCurrentTheme()
        if (theme) {
          currentTheme.value = theme
          console.log('初始主题已应用:', theme.name, theme.primaryColor)
        }
      })
    }
  }
}

onMounted(async () => {
  if (!checkAuth()) return
  
  try {
    // 先加载主题数据
    await loadThemes()
    // 再加载简历数据
    await fetchResumes()
    // 加载用户文件信息
    await fetchUserFile()
  } catch (error) {
    console.error('UserCenter onMounted error:', error)
    ElMessage.error('加载数据失败')
  }
})

// 弹窗相关
const dialogVisible = ref(false)
const form = reactive({ 
  id: null, 
  name: '', 
  themeId: null,
  personalInfo: {
    name: '',
    title: '',
    phone: '',
    email: '',
    location: ''
  },
  summary: '',
  education: [],
  workExperience: [],
  projectExperience: [],
  skills: []
})
const themes = ref([])
const currentTheme = ref(null)
const loadThemes = async () => {
  const res = await http.get('/api/user/themes')
  if (res.data?.code === 1 || res.data?.code === 200) themes.value = res.data.data
}
const isEdit = ref(false)

const openAdd = () => {
  isEdit.value = false
  Object.assign(form, { 
    id: null, 
    name: '', 
    themeId: null,
    personalInfo: { name: '', title: '', phone: '', email: '', location: '' },
    summary: '',
    education: [],
    workExperience: [],
    projectExperience: [],
    skills: []
  })
  dialogVisible.value = true
}
const openEdit = () => {
  if (!currentResume.value) return
  isEdit.value = true
  try {
    const content = JSON.parse(currentResume.value.content)
    
    // 处理项目经历中的technologies字段
    const processedProjectExperience = (content.projectExperience || []).map(project => ({
      ...project,
      technologies: Array.isArray(project.technologies) 
        ? project.technologies.join(', ')
        : (project.technologies || '')
    }))
    
    Object.assign(form, { 
      id: currentResume.value.id, 
      name: currentResume.value.name, 
      themeId: currentResume.value.themeId || null,
      personalInfo: content.personalInfo || { name: '', title: '', phone: '', email: '', location: '' },
      summary: content.summary || '',
      education: content.education || [],
      workExperience: content.workExperience || [],
      projectExperience: processedProjectExperience,
      skills: content.skills || []
    })
  } catch (e) {
    ElMessage.error('简历数据格式错误')
    return
  }
  dialogVisible.value = true
}

const saveResume = async () => {
  // 处理项目经历中的technologies字段
  const processedProjectExperience = form.projectExperience.map(project => ({
    ...project,
    technologies: typeof project.technologies === 'string' 
      ? project.technologies.split(',').map(t => t.trim()).filter(t => t)
      : project.technologies
  }))
  
  const content = {
    personalInfo: form.personalInfo,
    summary: form.summary,
    education: form.education,
    workExperience: form.workExperience,
    projectExperience: processedProjectExperience,
    skills: form.skills
  }
  const payload = { name: form.name, content: JSON.stringify(content), themeId: form.themeId }
  const api = isEdit.value ? http.put(`/api/user/resume/${form.id}`, payload) : http.post('/api/user/resume', payload)
  const res = await api
  if (res.data?.code === 1 || res.data?.code === 200) {
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
    dialogVisible.value = false
    await fetchResumes()
    if (!isEdit.value && resumes.value.length) currentId.value = resumes.value[resumes.value.length - 1].id
  } else {
    ElMessage.error(res.data?.message || res.data?.msg || '保存失败')
  }
}

const removeResume = async () => {
  if (!currentResume.value) return
  await ElMessageBox.confirm('确定删除当前简历吗？', '提示', { type: 'warning' })
  const res = await http.delete(`/api/user/resume/${currentResume.value.id}`)
  if (res.data?.code === 1 || res.data?.code === 200) {
    ElMessage.success('删除成功')
    await fetchResumes()
    currentId.value = resumes.value[0]?.id || null
  } else {
    ElMessage.error(res.data?.message || res.data?.msg || '删除失败')
  }
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.replace('/')
}

// 动态添加/删除功能
const addEducation = () => {
  form.education.push({ school: '', major: '', degree: '', startDate: '', endDate: '' })
}
const removeEducation = (index) => {
  form.education.splice(index, 1)
}

const addWorkExperience = () => {
  form.workExperience.push({ company: '', position: '', startDate: '', endDate: '', description: '' })
}
const removeWorkExperience = (index) => {
  form.workExperience.splice(index, 1)
}

const addProjectExperience = () => {
  form.projectExperience.push({ name: '', role: '', startDate: '', endDate: '', technologies: [], description: '' })
}
const removeProjectExperience = (index) => {
  form.projectExperience.splice(index, 1)
}

const addSkill = () => {
  form.skills.push('')
}
const removeSkill = (index) => {
  form.skills.splice(index, 1)
}

// 滚动导航功能
const scrollToSection = (sectionId) => {
  const element = document.getElementById(sectionId)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' })
  }
}

// PDF导出功能
const exportPdf = async () => {
  if (!currentResume.value) return
  try {
    const token = localStorage.getItem('token')
    const url = `/api/user/resume/${currentResume.value.id}/export/pdf`
    
    // 使用fetch方式，在请求头中携带token
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.ok) {
      // 创建blob并下载
      const blob = await response.blob()
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = `${currentResume.value.name || '简历'}.pdf`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
      
      ElMessage.success('PDF导出成功')
    } else {
      const errorData = await response.json()
      ElMessage.error(errorData.msg || 'PDF导出失败')
    }
  } catch (error) {
    console.error('PDF导出失败:', error)
    ElMessage.error('PDF导出失败，请重试')
  }
}

// 文件上传相关方法
const fetchUserFile = async () => {
  try {
    const res = await http.get('/api/user/file/info')
    if (res.data?.code === 1 || res.data?.code === 200) {
      userFile.value = res.data.data
    } else {
      userFile.value = null
    }
  } catch (error) {
    console.error('获取文件信息失败:', error)
    userFile.value = null
  }
}

const openUploadDialog = () => {
  uploadDialogVisible.value = true
}

const openFileDialog = () => {
  fileDialogVisible.value = true
}

const handleFileUpload = async (file) => {
  const formData = new FormData()
  formData.append('file', file.raw)
  
  uploadLoading.value = true
  try {
    const res = await http.post('/api/user/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    if (res.data?.code === 1 || res.data?.code === 200) {
      ElMessage.success('文件上传成功')
      userFile.value = res.data.data
      uploadDialogVisible.value = false
      uploadRef.value.clearFiles()
    } else {
      ElMessage.error(res.data?.message || res.data?.msg || '文件上传失败')
    }
  } catch (error) {
    console.error('文件上传失败:', error)
    ElMessage.error('文件上传失败，请重试')
  } finally {
    uploadLoading.value = false
  }
}

const downloadFile = async () => {
  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/user/file/download', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
    
    if (response.ok) {
      const blob = await response.blob()
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = userFile.value.originalName
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
      
      ElMessage.success('文件下载成功')
    } else {
      ElMessage.error('文件下载失败')
    }
  } catch (error) {
    console.error('文件下载失败:', error)
    ElMessage.error('文件下载失败，请重试')
  }
}

const deleteFile = async () => {
  try {
    await ElMessageBox.confirm('确定要删除当前文件吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await http.delete('/api/user/file/delete')
    if (res.data?.code === 1 || res.data?.code === 200) {
      ElMessage.success('文件删除成功')
      userFile.value = null
      fileDialogVisible.value = false
    } else {
      ElMessage.error(res.data?.message || res.data?.msg || '文件删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('文件删除失败:', error)
      ElMessage.error('文件删除失败，请重试')
    }
  }
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 简历切换处理
const onResumeChange = (newId) => {
  currentId.value = newId
}

const parsedContent = computed(() => {
  if (!currentResume.value) return null
  try { 
    return JSON.parse(currentResume.value.content)
  } catch (e) { 
    console.error('简历内容解析失败:', e)
    return null 
  }
})

// 获取当前简历的主题色
const getCurrentTheme = () => {
  if (!currentResume.value || !themes.value.length) return null
  const themeId = currentResume.value.themeId
  if (!themeId) return null
  return themes.value.find(t => t.id === themeId)
}

// 监听当前简历变化，更新主题
watch(currentResume, () => {
  const newTheme = getCurrentTheme()
  if (newTheme !== currentTheme.value) {
    currentTheme.value = newTheme
    console.log('主题已更新:', newTheme?.name, newTheme?.primaryColor)
  }
}, { immediate: true })

// 监听主题变化，确保主题色正确应用
watch(currentTheme, (newTheme) => {
  if (newTheme) {
    nextTick(() => {
      console.log('主题色已应用:', newTheme.name, newTheme.primaryColor)
    })
  }
}, { immediate: true })

// 计算主题色相关的CSS变量
const themeStyles = computed(() => {
  if (!currentTheme.value) return {}
  const primaryColor = currentTheme.value.primaryColor
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

</script>

<template>
  <div style="padding:16px;">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
      <div>
        <el-select v-model="currentId" placeholder="选择简历" style="width:260px" @change="onResumeChange">
          <el-option v-for="r in resumes" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
        <el-button style="margin-left:8px;" @click="openEdit" :disabled="!currentResume">编辑</el-button>
        <el-button type="danger" @click="removeResume" :disabled="!currentResume">删除</el-button>
        <el-button type="primary" @click="openAdd">新增简历</el-button>
        <el-button type="success" @click="openUploadDialog" style="margin-left:8px;">上传文件</el-button>
        <el-button type="info" @click="openFileDialog" :disabled="!userFile" style="margin-left:8px;">查看文件</el-button>
      </div>
      <el-button @click="logout">退出登录</el-button>
    </div>

    <div v-if="parsedContent" style="display:flex;gap:20px;" :style="themeStyles">
      <!-- 简历内容 -->
      <div style="flex:1;">
        <el-card class="resume-card">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center;">
              <span>{{ currentResume?.name }}</span>
              <el-button type="primary" size="small" @click="exportPdf">导出PDF</el-button>
            </div>
          </template>
          
          <!-- 个人信息 -->
          <div id="personal-info" class="resume-section">
            <h3 class="section-title">个人信息</h3>
            <div class="info-grid">
              <div class="info-item">
                <span class="label">姓名：</span>
                <span class="value">{{ parsedContent.personalInfo?.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">岗位：</span>
                <span class="value">{{ parsedContent.personalInfo?.title }}</span>
              </div>
              <div class="info-item">
                <span class="label">电话：</span>
                <span class="value">{{ parsedContent.personalInfo?.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱：</span>
                <span class="value">{{ parsedContent.personalInfo?.email }}</span>
              </div>
              <div class="info-item">
                <span class="label">地址：</span>
                <span class="value">{{ parsedContent.personalInfo?.location }}</span>
              </div>
            </div>
          </div>

          <!-- 个人简介 -->
          <div id="summary" class="resume-section">
            <h3 class="section-title">个人简介</h3>
            <p class="summary-text">{{ parsedContent.summary }}</p>
          </div>

          <!-- 教育背景 -->
          <div id="education" class="resume-section">
            <h3 class="section-title">教育背景</h3>
            <div v-for="(e,i) in parsedContent.education || []" :key="i" class="experience-item">
              <div class="experience-header">
                <span class="main-title">{{ e.school }}</span>
                <span class="time">{{ e.startDate }} - {{ e.endDate }}</span>
              </div>
              <div class="experience-detail">
                <span class="sub-title">{{ e.major }}（{{ e.degree }}）</span>
              </div>
            </div>
          </div>

          <!-- 工作经历 -->
          <div id="work-experience" class="resume-section">
            <h3 class="section-title">工作经历</h3>
            <div v-for="(w,i) in parsedContent.workExperience || []" :key="i" class="experience-item">
              <div class="experience-header">
                <span class="main-title">{{ w.company }} - {{ w.position }}</span>
                <span class="time">{{ w.startDate }} - {{ w.endDate }}</span>
              </div>
              <div class="experience-detail">
                <p>{{ w.description }}</p>
              </div>
            </div>
          </div>

          <!-- 项目经历 -->
          <div id="project-experience" class="resume-section">
            <h3 class="section-title">项目经历</h3>
            <div v-for="(p,i) in parsedContent.projectExperience || []" :key="i" class="experience-item">
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

          <!-- 技能特长 -->
          <div id="skills" class="resume-section">
            <h3 class="section-title">技能特长</h3>
            <div class="skills-container">
              <el-tag v-for="(skill,i) in parsedContent.skills || []" :key="i" class="skill-tag">{{ skill }}</el-tag>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧导航 -->
      <div style="width:200px;">
        <el-card class="nav-card">
          <template #header>
            <span>快速导航</span>
          </template>
          <div class="nav-menu">
            <div class="nav-item" @click="scrollToSection('personal-info')">个人信息</div>
            <div class="nav-item" @click="scrollToSection('summary')">个人简介</div>
            <div class="nav-item" @click="scrollToSection('education')">教育背景</div>
            <div class="nav-item" @click="scrollToSection('work-experience')">工作经历</div>
            <div class="nav-item" @click="scrollToSection('project-experience')">项目经历</div>
            <div class="nav-item" @click="scrollToSection('skills')">技能特长</div>
          </div>
        </el-card>
      </div>
    </div>

    <el-empty v-else description="暂无简历" />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑简历' : '新增简历'" width="900px" :close-on-click-modal="false">
      <el-scrollbar height="600px">
        <el-form label-width="100px">
          <el-form-item label="简历名称">
            <el-input v-model="form.name" placeholder="请输入简历名称" />
          </el-form-item>
          <el-form-item label="主题色">
            <el-select v-model="form.themeId" placeholder="选择主题">
              <el-option v-for="t in themes" :key="t.id" :label="t.name + ' ' + t.primaryColor" :value="t.id" />
            </el-select>
          </el-form-item>

          <el-divider content-position="left">个人信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="姓名">
                <el-input v-model="form.personalInfo.name" placeholder="请输入姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="职位">
                <el-input v-model="form.personalInfo.title" placeholder="请输入职位" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="电话">
                <el-input v-model="form.personalInfo.phone" placeholder="请输入电话" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱">
                <el-input v-model="form.personalInfo.email" placeholder="请输入邮箱" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="地址">
            <el-input v-model="form.personalInfo.location" placeholder="请输入地址" />
          </el-form-item>

          <el-divider content-position="left">个人简介</el-divider>
          <el-form-item label="简介">
            <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入个人简介" />
          </el-form-item>

          <el-divider content-position="left">教育背景</el-divider>
          <div v-for="(edu, index) in form.education" :key="index" style="border:1px solid #eee;padding:12px;margin-bottom:12px;border-radius:4px;">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
              <span style="font-weight:bold;">教育经历 {{ index + 1 }}</span>
              <el-button size="small" type="danger" @click="removeEducation(index)">删除</el-button>
            </div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="学校">
                  <el-input v-model="edu.school" placeholder="请输入学校" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="专业">
                  <el-input v-model="edu.major" placeholder="请输入专业" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="学历">
                  <el-input v-model="edu.degree" placeholder="请输入学历" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="开始时间">
                  <el-input v-model="edu.startDate" placeholder="如：2015-09" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="结束时间">
                  <el-input v-model="edu.endDate" placeholder="如：2019-06" />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
          <el-button type="primary" size="small" @click="addEducation">添加教育经历</el-button>

          <el-divider content-position="left">工作经历</el-divider>
          <div v-for="(work, index) in form.workExperience" :key="index" style="border:1px solid #eee;padding:12px;margin-bottom:12px;border-radius:4px;">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
              <span style="font-weight:bold;">工作经历 {{ index + 1 }}</span>
              <el-button size="small" type="danger" @click="removeWorkExperience(index)">删除</el-button>
            </div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="公司">
                  <el-input v-model="work.company" placeholder="请输入公司名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="职位">
                  <el-input v-model="work.position" placeholder="请输入职位" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开始时间">
                  <el-input v-model="work.startDate" placeholder="如：2020-06" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="结束时间">
                  <el-input v-model="work.endDate" placeholder="如：至今" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="工作描述">
              <el-input v-model="work.description" type="textarea" :rows="2" placeholder="请输入工作描述" />
            </el-form-item>
          </div>
          <el-button type="primary" size="small" @click="addWorkExperience">添加工作经历</el-button>

          <el-divider content-position="left">项目经历</el-divider>
          <div v-for="(project, index) in form.projectExperience" :key="index" style="border:1px solid #eee;padding:12px;margin-bottom:12px;border-radius:4px;">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
              <span style="font-weight:bold;">项目经历 {{ index + 1 }}</span>
              <el-button size="small" type="danger" @click="removeProjectExperience(index)">删除</el-button>
            </div>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="项目名称">
                  <el-input v-model="project.name" placeholder="请输入项目名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="担任角色">
                  <el-input v-model="project.role" placeholder="请输入担任角色" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="开始时间">
                  <el-input v-model="project.startDate" placeholder="如：2021-03" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="结束时间">
                  <el-input v-model="project.endDate" placeholder="如：2022-01" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="技术栈">
              <el-input v-model="project.technologies" placeholder="请输入技术栈，用逗号分隔" />
            </el-form-item>
            <el-form-item label="项目描述">
              <el-input v-model="project.description" type="textarea" :rows="2" placeholder="请输入项目描述" />
            </el-form-item>
          </div>
          <el-button type="primary" size="small" @click="addProjectExperience">添加项目经历</el-button>

          <el-divider content-position="left">技能特长</el-divider>
          <div v-for="(skill, index) in form.skills" :key="index" style="display:flex;align-items:center;margin-bottom:8px;">
            <el-input v-model="form.skills[index]" placeholder="请输入技能" style="margin-right:8px;" />
            <el-button size="small" type="danger" @click="removeSkill(index)">删除</el-button>
          </div>
          <el-button type="primary" size="small" @click="addSkill">添加技能</el-button>
        </el-form>
      </el-scrollbar>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveResume">保存</el-button>
      </template>
    </el-dialog>

    <!-- 文件上传弹窗 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="500px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="handleFileUpload"
        :limit="1"
        accept=".docx,.pdf"
        :file-list="[]"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            只能上传 docx/pdf 文件，且不超过 5MB
          </div>
        </template>
      </el-upload>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 文件查看弹窗 -->
    <el-dialog v-model="fileDialogVisible" title="文件信息" width="500px">
      <div v-if="userFile" class="file-info">
        <div class="file-item">
          <span class="label">文件名：</span>
          <span class="value">{{ userFile.originalName }}</span>
        </div>
        <div class="file-item">
          <span class="label">文件大小：</span>
          <span class="value">{{ formatFileSize(userFile.fileSize) }}</span>
        </div>
        <div class="file-item">
          <span class="label">文件类型：</span>
          <span class="value">{{ userFile.fileType.toUpperCase() }}</span>
        </div>
        <div class="file-item">
          <span class="label">上传时间：</span>
          <span class="value">{{ userFile.uploadTime }}</span>
        </div>
      </div>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="fileDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="downloadFile">下载文件</el-button>
          <el-button type="danger" @click="deleteFile">删除文件</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 简历卡片主题色背景 */
.resume-card {
  background: linear-gradient(135deg, var(--theme-bg, #f8f9fa) 0%, var(--theme-bg-light, #ffffff) 100%);
  border: 2px solid var(--theme-primary, #409eff);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.nav-card {
  background: var(--theme-bg-light, #ffffff);
  border: 1px solid var(--theme-primary-light, #79bbff);
}

.resume-section {
  margin-bottom: 32px;
  padding: 20px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--theme-primary-lighter, #b3d8ff);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.resume-section:last-child {
  border-bottom: 1px solid var(--theme-primary-lighter, #b3d8ff);
}

.section-title {
  color: var(--theme-primary, #409eff);
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--theme-primary, #409eff);
  background: linear-gradient(90deg, var(--theme-primary, #409eff), var(--theme-primary-light, #79bbff));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  font-weight: bold;
  color: #666;
  min-width: 60px;
}

.value {
  color: #333;
}

.summary-text {
  line-height: 1.6;
  color: #555;
  font-size: 14px;
}

.experience-item {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--theme-bg-light, #fafafa);
  border-radius: 8px;
  border-left: 4px solid var(--theme-primary, #409eff);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.experience-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.experience-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.main-title {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.sub-title {
  color: #666;
  font-size: 14px;
}

.time {
  color: var(--theme-primary-dark, #337ecc);
  font-size: 12px;
  background: var(--theme-primary-lighter, #e8f4fd);
  padding: 4px 8px;
  border-radius: 4px;
  border: 1px solid var(--theme-primary-light, #b3d8ff);
}

.experience-detail p {
  margin: 4px 0;
  line-height: 1.5;
  color: #555;
}

.skills-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.skill-tag {
  margin: 0;
  background: var(--theme-primary-lighter, #e8f4fd);
  color: var(--theme-primary-dark, #337ecc);
  border: 1px solid var(--theme-primary-light, #b3d8ff);
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-item {
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s;
  color: #666;
  font-size: 14px;
}

.nav-item:hover {
  background: var(--theme-primary-lighter, #f0f9ff);
  color: var(--theme-primary, #409eff);
  transform: translateX(4px);
}

.nav-item:active {
  background: var(--theme-primary-light, #e1f3ff);
}

/* 文件信息样式 */
.file-info {
  padding: 16px;
}

.file-item {
  display: flex;
  margin-bottom: 12px;
  align-items: center;
}

.file-item .label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
}

.file-item .value {
  color: #303133;
  flex: 1;
}
</style>


