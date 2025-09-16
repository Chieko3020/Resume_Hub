/*
 Navicat Premium Data Transfer

 Source Server         : Chieko3020
 Source Server Type    : MySQL
 Source Server Version : 80019
 Source Host           : localhost:3306
 Source Schema         : resume_db

 Target Server Type    : MySQL
 Target Server Version : 80019
 File Encoding         : 65001

 Date: 17/09/2025 02:10:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for resume
-- ----------------------------
DROP TABLE IF EXISTS `resume`;
CREATE TABLE `resume`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '所属用户ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '简历名称',
  `content` json NOT NULL COMMENT '简历内容(JSON格式)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `theme_id` int NULL DEFAULT NULL COMMENT '主题ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id`) USING BTREE,
  CONSTRAINT `resume_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of resume
-- ----------------------------
INSERT INTO `resume` VALUES (1, 2, '张三的简历', '{\"skills\": [\"Java\", \"Spring Boot\", \"MySQL\", \"Redis\"], \"summary\": \"五年Java开发经验，熟悉Spring Boot、MySQL、Redis等技术。\", \"education\": [{\"major\": \"软件工程\", \"degree\": \"本科\", \"school\": \"复旦大学\", \"endDate\": \"2016-06\", \"startDate\": \"2012-09\"}], \"personalInfo\": {\"name\": \"张三\", \"email\": \"zhangsan@example.com\", \"phone\": \"13800000001\", \"title\": \"Java开发工程师\", \"location\": \"上海市浦东新区\"}}', '2025-09-16 00:56:40', '2025-09-17 01:31:44', 1);
INSERT INTO `resume` VALUES (2, 3, '李四的简历', '{\"skills\": [\"Vue\", \"JavaScript\", \"CSS\"], \"summary\": \"三年前端开发经验，精通Vue、JavaScript、CSS。\", \"education\": [{\"major\": \"计算机科学\", \"degree\": \"本科\", \"school\": \"中山大学\", \"endDate\": \"2017-06\", \"startDate\": \"2013-09\"}], \"personalInfo\": {\"name\": \"李四\", \"email\": \"lisi@example.com\", \"phone\": \"13800000002\", \"title\": \"前端工程师\", \"location\": \"广州市天河区\"}}', '2025-09-16 00:56:40', '2025-09-17 01:31:45', 1);
INSERT INTO `resume` VALUES (3, 4, '王五的简历', '{\"skills\": [\"产品设计\", \"项目管理\", \"需求分析\"], \"summary\": \"六年互联网产品经理经验，擅长需求分析和项目管理。\", \"education\": [{\"major\": \"管理学\", \"degree\": \"硕士\", \"school\": \"北京大学\", \"endDate\": \"2013-06\", \"startDate\": \"2010-09\"}], \"personalInfo\": {\"name\": \"王五\", \"email\": \"wangwu@example.com\", \"phone\": \"13800000003\", \"title\": \"产品经理\", \"location\": \"北京市海淀区\"}}', '2025-09-16 00:56:40', '2025-09-17 01:31:46', 1);

-- ----------------------------
-- Table structure for theme_config
-- ----------------------------
DROP TABLE IF EXISTS `theme_config`;
CREATE TABLE `theme_config`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主题名称',
  `primary_color` varchar(7) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主题色（hex格式）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of theme_config
-- ----------------------------
INSERT INTO `theme_config` VALUES (1, '标准蓝', '#4B6EAF', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (2, '深邃蓝', '#2C3E50', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (3, '天空蓝', '#739AE8', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (4, '珊瑚红', '#F67280', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (5, '森林绿', '#4CAF50', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (6, '活力橙', '#E67E22', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (7, '热情红', '#E74C3C', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (8, '沉稳棕', '#795548', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (9, '湖水蓝', '#3498DB', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (10, '靛青蓝', '#476EBF', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (11, '优雅紫', '#9B59B6', '2025-09-16 00:53:46', '2025-09-16 00:53:46');
INSERT INTO `theme_config` VALUES (12, '金秋黄', '#F1C40F', '2025-09-16 00:53:46', '2025-09-16 00:53:46');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `role` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'user' COMMENT '角色(admin/user)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '123456', 'admin', '2025-09-16 00:53:46', '2025-09-17 02:10:04');
INSERT INTO `user` VALUES (2, 'zhangsan', '123456', 'user', '2025-09-16 00:56:10', '2025-09-17 02:10:08');
INSERT INTO `user` VALUES (3, 'lisi', '123456', 'user', '2025-09-16 00:56:10', '2025-09-17 02:10:17');
INSERT INTO `user` VALUES (4, 'wangwu', '123456', 'user', '2025-09-16 00:56:10', '2025-09-16 00:56:10');

-- ----------------------------
-- Table structure for user_file
-- ----------------------------
DROP TABLE IF EXISTS `user_file`;
CREATE TABLE `user_file` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_name` varchar(255) NOT NULL COMMENT '存储文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint(20) NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(10) NOT NULL COMMENT '文件类型',
  `upload_time` datetime NOT NULL COMMENT '上传时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`) COMMENT '每个用户只能有一个文件',
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户文件表';

SET FOREIGN_KEY_CHECKS = 1;
