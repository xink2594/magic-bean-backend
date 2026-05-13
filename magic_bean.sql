/*
 Navicat Premium Dump SQL

 Source Server         : ali
 Source Server Type    : MySQL
 Source Server Version : 80043 (8.0.43)
 Source Host           : 121.43.58.136:3306
 Source Schema         : magic_bean

 Target Server Type    : MySQL
 Target Server Version : 80043 (8.0.43)
 File Encoding         : 65001

 Date: 13/05/2026 15:32:15
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for plant_data
-- ----------------------------
DROP TABLE IF EXISTS `plant_data`;
CREATE TABLE `plant_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备MAC地址或唯一标识',
  `timestamp` bigint NOT NULL COMMENT '整点/半点的时间戳(毫秒)',
  `temperature` double NULL DEFAULT NULL COMMENT '环境温度',
  `air_humidity` double NULL DEFAULT NULL COMMENT '空气湿度',
  `dirt_humidity` double NULL DEFAULT NULL COMMENT '土壤湿度',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '云端入库时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_time`(`device_id` ASC, `timestamp` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 124 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '传感器整点/半点历史数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for plant_diary
-- ----------------------------
DROP TABLE IF EXISTS `plant_diary`;
CREATE TABLE `plant_diary`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '设备标识',
  `timestamp` bigint NOT NULL COMMENT '手记创建的时间戳(毫秒)',
  `temperature` double NULL DEFAULT NULL COMMENT '拍照/记录瞬间的温度',
  `air_humidity` double NULL DEFAULT NULL COMMENT '拍照/记录瞬间的空气湿度',
  `dirt_humidity` double NULL DEFAULT NULL COMMENT '拍照/记录瞬间的土壤湿度',
  `image_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图床直链',
  `note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户手记内容',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '云端入库时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_time_diary`(`device_id` ASC, `timestamp` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '植物生长日记与画廊' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for plant_status
-- ----------------------------
DROP TABLE IF EXISTS `plant_status`;
CREATE TABLE `plant_status`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `device_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '云端入库时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
