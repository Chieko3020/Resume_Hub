<script setup>
import { useRouter } from 'vue-router'
import { ElButton, ElCard, ElRow, ElCol, ElIcon } from 'element-plus'
import { 
  User, 
  Document, 
  Download, 
  Edit, 
  Setting, 
  Star,
  Clock,
  Monitor
} from '@element-plus/icons-vue'

const router = useRouter()
const goLogin = () => router.push('/login')

// 功能特色数据
const features = [
  {
    icon: Document,
    title: '智能简历管理',
    description: '支持多套简历管理，一键切换，实时预览效果',
    color: '#409EFF'
  },
  {
    icon: Edit,
    title: '在线编辑',
    description: '所见即所得编辑器，支持实时保存',
    color: '#67C23A'
  },
  {
    icon: Download,
    title: 'PDF导出',
    description: '支持PDF格式导出，保持原始格式不变',
    color: '#E6A23C'
  },
  {
    icon: Setting,
    title: '主题定制',
    description: '多种精美主题色彩，个性化定制你的简历风格',
    color: '#F56C6C'
  },
  {
    icon: Star,
    title: '安全可靠',
    description: 'JWT身份认证，数据加密存储，保障信息安全',
    color: '#909399'
  },
  {
    icon: Monitor,
    title: '响应式设计',
    description: '完美适配桌面、平板、手机等各种设备',
    color: '#9C27B0'
  }
]

// 技术栈数据
const techStack = [
  { name: 'Vue 3', color: '#4FC08D' },
  { name: 'Element Plus', color: '#409EFF' },
  { name: 'Spring Boot', color: '#6DB33F' },
  { name: 'MyBatis Plus', color: '#FF6B35' },
  { name: 'MySQL', color: '#4479A1' },
  { name: 'JWT', color: '#000000' }
]

// 统计数据
const stats = [
  { number: '100+', label: '用户使用' },
  { number: '500+', label: '简历创建' },
  { number: '99.9%', label: '系统稳定性' },
  { number: '24/7', label: '技术支持' }
]
</script>

<template>
  <div class="home-container">
    <!-- 头部导航 -->
    <header class="header">
      <div class="nav-container">
        <div class="logo">
          <el-icon class="logo-icon"><Document /></el-icon>
          <span class="logo-text">ResumeHub</span>
        </div>
        <nav class="nav-menu">
          <a href="#features" class="nav-link">功能特色</a>
          <a href="#tech" class="nav-link">技术栈</a>
          <a href="#about" class="nav-link">关于我们</a>
        </nav>
        <el-button type="primary" size="large" @click="goLogin" class="login-btn">
          <el-icon><User /></el-icon>
          立即体验
        </el-button>
      </div>
    </header>

    <!-- 英雄区域 -->
    <section class="hero">
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            打造你的
            <span class="gradient-text">专业简历</span>
          </h1>
          <p class="hero-description">
            现代化的个人简历管理系统，让简历制作变得简单高效。
            支持在线编辑、多格式导出、主题定制，助你脱颖而出。
          </p>
          <div class="hero-buttons">
            <el-button type="primary" size="large" @click="goLogin" class="cta-button">
              <el-icon><Star /></el-icon>
              开始创建简历
            </el-button>
          </div>
        </div>
        <div class="hero-image">
          <div class="resume-preview">
            <div class="resume-card">
              <div class="resume-header">
                <div class="avatar"></div>
                <div class="info">
                  <h3>张三</h3>
                  <p>高级软件工程师</p>
                </div>
              </div>
              <div class="resume-content">
                <div class="section">
                  <h4>工作经历</h4>
                  <p>5年+ 软件开发经验</p>
                </div>
                <div class="section">
                  <h4>技能专长</h4>
                  <div class="skills">
                    <span class="skill-tag">Java</span>
                    <span class="skill-tag">Vue.js</span>
                    <span class="skill-tag">Spring Boot</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 统计数据 -->
    <section class="stats">
      <div class="stats-container">
        <div class="stat-item" v-for="stat in stats" :key="stat.label">
          <div class="stat-number">{{ stat.number }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </section>

    <!-- 功能特色 -->
    <section id="features" class="features">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">功能特色</h2>
          <p class="section-description">强大的功能，简单的操作</p>
        </div>
        <el-row :gutter="30">
          <el-col :xs="24" :sm="12" :lg="8" v-for="feature in features" :key="feature.title">
            <div class="feature-card">
              <div class="feature-icon" :style="{ backgroundColor: feature.color }">
                <el-icon><component :is="feature.icon" /></el-icon>
              </div>
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-description">{{ feature.description }}</p>
            </div>
          </el-col>
        </el-row>
      </div>
    </section>

    <!-- 技术栈 -->
    <section id="tech" class="tech-stack">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">技术栈</h2>
          <p class="section-description">采用现代化的技术架构</p>
        </div>
        <div class="tech-grid">
          <div class="tech-item" v-for="tech in techStack" :key="tech.name">
            <div class="tech-name" :style="{ color: tech.color }">{{ tech.name }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 关于我们 -->
    <section id="about" class="about">
      <div class="container">
        <div class="about-content">
          <div class="about-text">
            <h2 class="section-title">关于 ResumeHub</h2>
            <p class="about-description">
              ResumeHub 是一个现代化的个人简历管理系统，致力于为求职者提供简单、高效、专业的简历制作体验。
              我们相信，一份好的简历是成功求职的第一步。
            </p>
            <div class="about-features">
              <div class="about-feature">
                <el-icon class="feature-icon"><Document /></el-icon>
                <span>灵活编辑</span>
              </div>
              <div class="about-feature">
                <el-icon class="feature-icon"><Clock /></el-icon>
                <span>快速制作</span>
              </div>
              <div class="about-feature">
                <el-icon class="feature-icon"><Star /></el-icon>
                <span>智能优化</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 底部 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-section">
            <h3>ResumeHub</h3>
            <p>让简历制作变得简单高效</p>
          </div>
          <div class="footer-section">
            <h4>产品</h4>
            <ul>
              <li><a href="#features">功能特色</a></li>
              <li><a href="#tech">技术栈</a></li>
              <li><a href="#about">关于我们</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>支持</h4>
            <ul>
              <li><a href="#">帮助中心</a></li>
              <li><a href="#">用户指南</a></li>
              <li><a href="#">联系我们</a></li>
            </ul>
          </div>
          <div class="footer-section">
            <h4>联系方式</h4>
            <p>邮箱: support@resumehub.com</p>
            <p>电话: 400-123-4567</p>
          </div>
        </div>
        <div class="footer-bottom">
          <p>&copy; 2024 ResumeHub. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 头部导航 */
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.logo-icon {
  font-size: 28px;
  color: #409EFF;
}

.nav-menu {
  display: flex;
  gap: 30px;
}

.nav-link {
  color: #666;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #409EFF;
}

.login-btn {
  border-radius: 25px;
  padding: 12px 24px;
}

/* 英雄区域 */
.hero {
  padding: 120px 0 80px;
  min-height: 100vh;
  display: flex;
  align-items: center;
}

.hero-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  line-height: 1.2;
  color: white;
  margin-bottom: 20px;
}

.gradient-text {
  background: linear-gradient(45deg, #FFD700, #FFA500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-description {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.6;
  margin-bottom: 40px;
}

.hero-buttons {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.cta-button {
  background: linear-gradient(45deg, #FF6B6B, #FF8E53);
  border: none;
  border-radius: 30px;
  padding: 15px 30px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.3);
  transition: transform 0.3s, box-shadow 0.3s;
}

.cta-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 35px rgba(255, 107, 107, 0.4);
}


/* 简历预览 */
.resume-preview {
  position: relative;
  perspective: 1000px;
}

.resume-card {
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  transform: rotateY(-5deg) rotateX(5deg);
  transition: transform 0.3s;
}

.resume-card:hover {
  transform: rotateY(0deg) rotateX(0deg);
}

.resume-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(45deg, #409EFF, #67C23A);
}

.info h3 {
  margin: 0 0 5px;
  color: #333;
  font-size: 1.5rem;
}

.info p {
  margin: 0;
  color: #666;
  font-size: 1rem;
}

.resume-content .section {
  margin-bottom: 20px;
}

.resume-content h4 {
  color: #333;
  margin-bottom: 10px;
  font-size: 1.1rem;
}

.resume-content p {
  color: #666;
  margin: 0;
}

.skills {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.skill-tag {
  background: #409EFF;
  color: white;
  padding: 5px 12px;
  border-radius: 15px;
  font-size: 0.9rem;
}

/* 统计数据 */
.stats {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 60px 0;
}

.stats-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 40px;
}

.stat-item {
  text-align: center;
  color: white;
}

.stat-number {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 10px;
  background: linear-gradient(45deg, #FFD700, #FFA500);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 1.1rem;
  opacity: 0.9;
}

/* 功能特色 */
.features {
  background: white;
  padding: 100px 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #333;
  margin-bottom: 20px;
}

.section-description {
  font-size: 1.2rem;
  color: #666;
  max-width: 600px;
  margin: 0 auto;
}

.feature-card {
  text-align: center;
  padding: 40px 20px;
  border-radius: 15px;
  transition: transform 0.3s, box-shadow 0.3s;
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: white;
  font-size: 2rem;
}

.feature-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 15px;
}

.feature-description {
  color: #666;
  line-height: 1.6;
}

/* 技术栈 */
.tech-stack {
  background: #f8f9fa;
  padding: 100px 0;
}

.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 30px;
  margin-top: 40px;
}

.tech-item {
  background: white;
  padding: 30px 20px;
  border-radius: 15px;
  text-align: center;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
}

.tech-item:hover {
  transform: translateY(-5px);
}

.tech-name {
  font-size: 1.2rem;
  font-weight: 600;
}

/* 关于我们 */
.about {
  background: white;
  padding: 100px 0;
}

.about-content {
  display: flex;
  justify-content: center;
  align-items: center;
}

.about-text {
  max-width: 800px;
  text-align: center;
}

.about-description {
  font-size: 1.1rem;
  color: #666;
  line-height: 1.8;
  margin-bottom: 30px;
}

.about-features {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}

.about-feature {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #333;
  font-weight: 500;
}

.about-feature .feature-icon {
  color: #409EFF;
  font-size: 1.2rem;
}


/* 底部 */
.footer {
  background: #333;
  color: white;
  padding: 60px 0 20px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 40px;
  margin-bottom: 40px;
}

.footer-section h3,
.footer-section h4 {
  margin-bottom: 20px;
  color: white;
}

.footer-section ul {
  list-style: none;
  padding: 0;
}

.footer-section ul li {
  margin-bottom: 10px;
}

.footer-section ul li a {
  color: #ccc;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-section ul li a:hover {
  color: #409EFF;
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #555;
  color: #ccc;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-content {
    grid-template-columns: 1fr;
    text-align: center;
  }
  
  .hero-title {
    font-size: 2.5rem;
  }
  
  .nav-menu {
    display: none;
  }
  
  
  .hero-buttons {
    justify-content: center;
  }
  
  .stats-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .hero-title {
    font-size: 2rem;
  }
  
  .stats-container {
    grid-template-columns: 1fr;
  }
  
  .about-features {
    flex-direction: column;
  }
}
</style>


