# ResumeHub - 个人简历管理系统

一个现代化的个人简历管理系统，基于 Spring Boot + Vue 3 构建，支持在线编辑、主题定制、PDF导出等功能。
- AI Agents : Cursor & Github Copilot
## 📋 项目概述

ResumeHub 是一个简历管理系统，致力于为求职者提供简单、高效、专业的简历制作体验。系统采用前后端分离架构，支持多用户管理、简历编辑、主题定制、文件上传等核心功能。

## ✨ 核心功能

### 🎯 用户功能
- **智能简历管理**：支持多套简历管理，一键切换，实时预览效果
- **在线编辑**：所见即所得编辑器，支持实时保存
- **PDF导出**：支持PDF格式导出，保持原始格式不变
- **主题定制**：多种精美主题色彩，个性化定制你的简历风格
- **文件上传**：支持上传PDF/DOCX文件（最大5MB），本地存储管理
- **响应式设计**：完美适配桌面、平板、手机等各种设备

### 🔧 管理功能
- **用户管理**：查看所有用户，修改用户名，删除用户
- **简历管理**：查看所有简历，删除简历，查看简历详情
- **密码管理**：重置用户密码

## 🛠️ 技术栈

### 后端技术
- **Java 8** - 开发语言
- **Spring Boot 2.6.13** - 主框架
- **MyBatis-Plus 3.5.2** - ORM框架
- **MySQL 8.0** - 数据库
- **JWT 0.9.1** - 身份认证
- **iText 5.5.13.3** - PDF导出
- **Spring Security** - 密码加密

### 前端技术
- **Vue 3** - 前端框架
- **Element Plus 2.11.2** - UI组件库
- **Vue Router 4.5.1** - 路由管理
- **Axios 1.12.2** - HTTP客户端
- **Vite 7.1.2** - 构建工具
- **Pinia 3.0.3** - 状态管理

## 🏗️ 项目结构

```
resume-platform/
├── src/main/java/com/resume/          # 后端Java代码
│   ├── config/                        # 配置类
│   │   ├── JwtInterceptor.java        # JWT拦截器
│   │   ├── MybatisPlusConfig.java     # MyBatis-Plus配置
│   │   └── WebMvcConfig.java          # Web MVC配置
│   ├── controller/                    # 控制器层
│   │   ├── AdminController.java       # 管理员控制器
│   │   ├── ResumeController.java      # 简历控制器
│   │   ├── UserController.java        # 用户控制器
│   │   ├── ThemeController.java       # 主题控制器
│   │   ├── FileController.java        # 文件控制器
│   │   └── OssController.java         # OSS文件访问控制器
│   ├── service/                       # 服务层
│   │   ├── impl/                      # 服务实现
│   │   ├── ResumeService.java         # 简历服务
│   │   ├── UserService.java           # 用户服务
│   │   ├── UserFileService.java       # 用户文件服务
│   │   └── LocalOssService.java       # 本地OSS服务
│   ├── mapper/                        # 数据访问层
│   │   ├── ResumeMapper.java          # 简历Mapper
│   │   ├── UserMapper.java            # 用户Mapper
│   │   └── UserFileMapper.java        # 用户文件Mapper
│   ├── entity/                        # 实体类
│   │   ├── Resume.java                # 简历实体
│   │   ├── User.java                  # 用户实体
│   │   ├── ThemeConfig.java           # 主题配置实体
│   │   └── UserFile.java              # 用户文件实体
│   ├── dto/                           # 数据传输对象
│   │   ├── LoginDTO.java              # 登录DTO
│   │   ├── ChangePasswordDTO.java     # 修改密码DTO
│   │   └── ResumeDTO.java             # 简历DTO
│   ├── vo/                            # 视图对象
│   │   └── Result.java                # 统一响应格式
│   ├── util/                          # 工具类
│   │   ├── JwtUtil.java               # JWT工具类
│   │   └── PasswordUtil.java          # 密码工具类
│   ├── exception/                     # 异常处理
│   │   ├── BusinessException.java     # 业务异常
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   └── ResumeApplication.java         # 启动类
├── src/main/resources/                # 资源文件
│   ├── application.properties         # 应用配置
│   └── init.sql                       # 数据库初始化脚本
├── src/test/java/                     # 测试代码
│   ├── com/resume/                    # 测试包
│   └── README.md                      # 测试说明文档
├── resume-frontend/                   # 前端项目
│   ├── src/                           # 前端源码
│   │   ├── views/                     # 页面组件
│   │   │   ├── Home.vue               # 首页
│   │   │   ├── Login.vue              # 登录页
│   │   │   ├── UserCenter.vue         # 用户中心
│   │   │   └── AdminCenter.vue        # 管理后台
│   │   ├── router/                    # 路由配置
│   │   ├── utils/                     # 工具函数
│   │   └── style.css                  # 全局样式
│   ├── package.json                   # 前端依赖配置
│   └── vite.config.js                 # Vite配置
├── pom.xml                            # Maven配置
└── README.md                          # 项目说明文档
```

## 🚀 快速开始

### 环境要求
- Java 8+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 1. 克隆项目
```bash
git clone <https://github.com/Chieko3020/Resume_Hub.git>
cd resume-platform
```

### 2. 数据库配置
1. 创建MySQL数据库：
```sql
CREATE DATABASE resume_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 导入初始化脚本：
```bash
mysql -u root -p resume_db < src/main/resources/init.sql
```

### 3. 后端配置
1. 修改数据库连接配置（`src/main/resources/application.properties`）：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/resume_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
spring.datasource.username=your_username
spring.datasource.password=your_password
```

2. 启动后端服务：
```bash
mvn spring-boot:run
```

### 4. 前端配置
1. 进入前端目录：
```bash
cd resume-frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动前端服务：
```bash
npm run dev
```

### 5. 访问系统
- 前端地址：http://localhost:5173
- 后端API：http://localhost:8080

## 📊 数据库设计

### 核心表结构

#### 用户表 (user)
```sql
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL UNIQUE COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `role` varchar(20) DEFAULT 'user' COMMENT '角色(user/admin)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
```

#### 简历表 (resume)
```sql
CREATE TABLE `resume` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `name` varchar(100) NOT NULL COMMENT '简历名称',
  `is_active` tinyint(1) DEFAULT 0 COMMENT '是否启用(0-否,1-是)',
  `content` json NOT NULL COMMENT '简历内容(JSON格式)',
  `theme_id` int(11) DEFAULT 1 COMMENT '主题ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_active` (`is_active`)
);
```

#### 主题配置表 (theme_config)
```sql
CREATE TABLE `theme_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '主题名称',
  `primary_color` varchar(20) NOT NULL COMMENT '主色调',
  `secondary_color` varchar(20) NOT NULL COMMENT '辅助色',
  `background_color` varchar(20) NOT NULL COMMENT '背景色',
  `text_color` varchar(20) NOT NULL COMMENT '文字色',
  `is_active` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);
```

#### 用户文件表 (user_file)
```sql
CREATE TABLE `user_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(10) NOT NULL COMMENT '文件类型',
  `upload_time` datetime NOT NULL COMMENT '上传时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`)
);
```

## 🔌 API接口文档

### 认证相关
- `POST /api/user/login` - 用户登录
- `POST /api/user/register` - 用户注册
- `POST /api/admin/login` - 管理员登录

### 简历管理
- `GET /api/user/resume` - 获取用户简历列表
- `POST /api/user/resume` - 创建简历
- `PUT /api/user/resume/{id}` - 更新简历
- `DELETE /api/user/resume/{id}` - 删除简历
- `PUT /api/user/resume/{id}/active` - 设置简历为启用状态
- `GET /api/user/resume/{id}/export/pdf` - 导出PDF

### 主题管理
- `GET /api/user/themes` - 获取主题列表
- `GET /api/admin/themes` - 获取所有主题（管理员）

### 文件管理
- `POST /api/user/file/upload` - 上传文件
- `GET /api/user/file/info` - 获取文件信息
- `GET /api/user/file/download` - 下载文件
- `DELETE /api/user/file/delete` - 删除文件

### 管理员功能
- `GET /api/admin/users` - 获取用户列表
- `PUT /api/admin/users/{id}` - 更新用户信息
- `DELETE /api/admin/users/{id}` - 删除用户
- `POST /api/admin/users/{id}/reset-password` - 重置用户密码
- `GET /api/admin/resume` - 获取所有简历
- `DELETE /api/admin/resume/{id}` - 删除简历

## 🎨 主题系统

系统支持多种主题色彩，用户可以根据个人喜好选择：

1. **经典蓝**
2. **活力橙**
3. **优雅紫**
4. **清新绿**
5. **热情红**

每个主题包含：
- 主色调 (Primary Color)
- 辅助色 (Secondary Color)
- 背景色 (Background Color)
- 文字色 (Text Color)

## 📁 文件存储

### 本地OSS存储
系统采用本地OSS存储方案，文件存储结构：
```
oss-storage/
└── user-files/
    └── 2025/
        └── 09/
            └── 17/
                ├── 20250917053846_09227f37.pdf
                └── 20250917054012_a1b2c3d4.docx
```

### 文件访问
- 访问URL：`http://localhost:8080/oss/user-files/2025/09/17/filename.pdf`
- 支持文件类型：PDF、DOCX
- 文件大小限制：5MB
- 每个用户只能上传一个文件

## 🔒 安全特性

### 身份认证
- JWT Token认证
- Token有效期：7天
- 自动刷新机制

### 密码安全
- BCrypt加密存储

## 🧪 测试

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=ResumeServiceTest

# 生成测试报告
mvn surefire-report:report
```

### 测试覆盖
- 单元测试：工具类、服务层
- 集成测试：控制器、完整业务流程

## 📦 部署

### 开发环境
```bash
# 后端
mvn spring-boot:run

# 前端
cd resume-frontend
npm run dev
```

### 生产环境
```bash
# 构建前端
cd resume-frontend
npm run build

# 构建后端
mvn clean package

# 运行
java -jar target/resume-0.0.1-SNAPSHOT.jar
```


## 📝 更新日志

### v1.0.0 (2025-09-17)
- ✨ 初始版本发布
- 🎯 用户注册登录功能
- 📝 简历CRUD操作
- 🎨 主题定制系统
- 📄 PDF导出功能
- 📁 文件上传管理
- 🔧 管理员后台
- 📱 响应式设计
