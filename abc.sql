/*
  大学宿舍管理系统 - 最终全功能逻辑版 v8
  开发者：溪 & 朋友
  
  核心修正：
  1. 用户表：补全了昵称、出生日期、头像、籍贯、政治面貌、民族、家庭住址、任职状态等。
  2. 审计字段：对所有表（含水电、资产、计费）全面覆盖 create_by, create_time, update_by, update_time。
  3. 联合约束：确保身份证号(id_card) + 学工号(username) 唯一。
  4. 资源控制：维持楼层级性别管控与床位五种状态。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =================================================================================
-- 1. 行政组织架构 (校区 -> 院系 -> 专业 -> 班级)
-- =================================================================================

DROP TABLE IF EXISTS `sys_campus`;
CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_name` varchar(100) NOT NULL COMMENT '校区名称',
  `campus_code` varchar(20) DEFAULT NULL COMMENT '校区编码',
  `address` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1-启用, 0-停用',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.1 校区表';

DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint NOT NULL,
  `dept_name` varchar(100) NOT NULL COMMENT '院系名称',
  `dept_type` tinyint DEFAULT '0' COMMENT '0-教学, 1-行政, 2-后勤',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.2 院系部门表';

DROP TABLE IF EXISTS `sys_major`;
CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_id` bigint NOT NULL,
  `major_name` varchar(100) NOT NULL,
  `duration` int DEFAULT '4' COMMENT '学制',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.3 专业表';

DROP TABLE IF EXISTS `biz_class`;
CREATE TABLE `biz_class` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `major_id` bigint NOT NULL,
  `grade` varchar(10) NOT NULL COMMENT '入学年份(如：2023级)',
  `class_name` varchar(100) NOT NULL,
  `counselor_id` bigint DEFAULT NULL COMMENT '班级辅导员(sys_user.id)',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.4 班级表';


-- =================================================================================
-- 2. 用户、角色与菜单 (RBAC + 全量个人信息)
-- =================================================================================

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL COMMENT '登录账号(学号/工号)',
  `id_card` varchar(20) NOT NULL COMMENT '身份证号(物理人唯一标识)',
  `password` varchar(100) NOT NULL DEFAULT 'e10adc3949ba59abbe56e057f20f883e' COMMENT '123456',
  `real_name` varchar(50) NOT NULL,
  `nickname` varchar(64) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `user_type` tinyint NOT NULL DEFAULT '1' COMMENT '0-管理员, 1-学生, 2-职员, 3-后勤',
  `sex` tinyint DEFAULT '1' COMMENT '1-男, 2-女',
  `date_of_birth` date DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `hometown` varchar(100) DEFAULT NULL COMMENT '籍贯',
  `political_status` varchar(20) DEFAULT '群众' COMMENT '政治面貌',
  `ethnicity` varchar(50) DEFAULT '汉族' COMMENT '民族',
  `home_address` varchar(255) DEFAULT NULL COMMENT '家庭住址',
  `status` tinyint DEFAULT '1' COMMENT '账号状态：1-正常, 0-归档',
  `employment_status` tinyint DEFAULT '0' COMMENT '任职状态：0-在职/读, 1-离职/毕业, 2-停职/休学',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_person_account` (`id_card`, `username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.1 系统用户表';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL,
  `role_key` varchar(50) NOT NULL COMMENT 'admin, student, counselor...',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.2 角色表';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.3 权限叠加表';


-- =================================================================================
-- 3. 详细档案与生命周期
-- =================================================================================

DROP TABLE IF EXISTS `stu_student`;
CREATE TABLE `stu_student` (
  `id` bigint NOT NULL COMMENT '关联sys_user.id',
  `class_id` bigint DEFAULT NULL,
  `entry_date` date NOT NULL,
  `graduation_date` date DEFAULT NULL,
  `academic_status` tinyint DEFAULT '0' COMMENT '0-在读, 1-休学, 2-退学, 3-毕业',
  `current_bed_id` bigint DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3.1 学生档案';

DROP TABLE IF EXISTS `biz_staff_info`;
CREATE TABLE `biz_staff_info` (
  `id` bigint NOT NULL COMMENT '关联sys_user.id',
  `dept_id` bigint DEFAULT NULL,
  `category` tinyint NOT NULL COMMENT '1-正式职工, 2-后勤',
  `job_title` varchar(50) DEFAULT NULL,
  `hire_date` date NOT NULL,
  `resign_date` date DEFAULT NULL,
  `staff_status` tinyint DEFAULT '0' COMMENT '0-在职, 1-离职, 2-退休',
  `current_bed_id` bigint DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3.2 职员档案';


-- =================================================================================
-- 4. 宿舍物理资源 (层级：楼-层-房-床)
-- =================================================================================

DROP TABLE IF EXISTS `dorm_building`;
CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint NOT NULL,
  `building_name` varchar(50) NOT NULL,
  `total_floors` int NOT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.1 楼栋表';

DROP TABLE IF EXISTS `dorm_floor`;
CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_id` bigint NOT NULL,
  `floor_num` int NOT NULL,
  `gender_type` tinyint DEFAULT '0' COMMENT '0-混住, 1-男层, 2-女层',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_b_f` (`building_id`, `floor_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.2 楼层表';

DROP TABLE IF EXISTS `dorm_room`;
CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `floor_id` bigint NOT NULL,
  `room_no` varchar(20) NOT NULL,
  `room_capacity` int NOT NULL DEFAULT '4',
  `room_purpose` char(2) DEFAULT '00',
  `room_status` tinyint DEFAULT '1' COMMENT '1-正常, 0-封禁',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.3 房间表';

DROP TABLE IF EXISTS `dorm_bed`;
CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `bed_no` varchar(10) NOT NULL,
  `bed_status` tinyint DEFAULT '0' COMMENT '0-置空, 1-占用, 2-维修, 3-预留, 4-封禁',
  `occupant_id` bigint DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.4 床位表';


-- =================================================================================
-- 5. 核心业务、水电、资产、完整偏好
-- =================================================================================

DROP TABLE IF EXISTS `dorm_room_asset`;
CREATE TABLE `dorm_room_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `asset_name` varchar(100) NOT NULL,
  `asset_no` varchar(50) DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1-正常, 0-损坏',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.1 资产表';

DROP TABLE IF EXISTS `dorm_meter_electric`;
CREATE TABLE `dorm_meter_electric` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.2 电表';

DROP TABLE IF EXISTS `biz_billing_record`;
CREATE TABLE `biz_billing_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1-电, 2-水',
  `amount` decimal(10,2) NOT NULL,
  `usage_val` decimal(10,2) NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-未缴, 1-已缴',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.3 计费记录';

-- 完整 18 维偏好表
DROP TABLE IF EXISTS `biz_user_preference`;
CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL COMMENT '关联sys_user.id',
  `is_smoker` tinyint DEFAULT '0',
  `is_drinker` tinyint DEFAULT '0',
  `wake_type` char(1) DEFAULT '0',
  `sleep_type` char(1) DEFAULT '0',
  `is_light_sleeper` tinyint DEFAULT '0',
  `study_at_night` tinyint DEFAULT '0',
  `mobile_game_freq` char(1) DEFAULT '0',
  `board_game_interest` tinyint DEFAULT '0',
  `cleanliness_level` char(1) DEFAULT '1',
  `air_condition_pref` char(1) DEFAULT '0',
  `noise_tolerance` char(1) DEFAULT '1',
  `guest_frequency` char(1) DEFAULT '0',
  `study_location_pref` char(1) DEFAULT '0',
  `in_room_noise_level` char(1) DEFAULT '0',
  `smell_sensitivity` char(1) DEFAULT '0',
  `hobby_tags` varchar(500) DEFAULT NULL,
  `group_code` varchar(50) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.4 全维度住宿偏好';

DROP TABLE IF EXISTS `biz_repair_order`;
CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `description` text NOT NULL,
  `status` tinyint DEFAULT '0',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.5 报修单';


-- =================================================================================
-- 6. 测试数据修补 (完整复杂场景)
-- =================================================================================

-- 角色
INSERT INTO `sys_role` (id, role_name, role_key) VALUES (1, '超级管理员', 'admin'), (2, '学生', 'student'), (3, '辅导员', 'counselor');

-- 用户：张三 (本科毕业后再考研)
-- 历史本科账号 (归档)
INSERT INTO `sys_user` (id, username, id_card, real_name, nickname, user_type, status, date_of_birth, hometown, political_status) 
VALUES (2, 'S2017001', '110101199901018888', '张三', '小张学长', 1, 0, '1999-01-01', '北京', '党员');
INSERT INTO `stu_student` (id, class_id, entry_date, academic_status) VALUES (2, 1, '2017-09-01', 3);

-- 当前研究生账号
INSERT INTO `sys_user` (id, username, id_card, real_name, nickname, user_type, status, sex, date_of_birth) 
VALUES (3, 'S2024G888', '110101199901018888', '张三', '张哥', 1, 1, 1, '1999-01-01');
INSERT INTO `stu_student` (id, class_id, entry_date, academic_status) VALUES (3, 1, '2024-09-01', 0);
INSERT INTO `sys_user_role` VALUES (3, 2);

-- 李华 (学生兼辅导员助理)
INSERT INTO `sys_user` (id, username, id_card, real_name, nickname, user_type, status, avatar) 
VALUES (4, 'S2021123', '440101200305051234', '李华', '阿华', 1, 1, 'http://api.dicebear.com/7.x/avataaars/svg?seed=华');
INSERT INTO `stu_student` (id, class_id, entry_date, academic_status) VALUES (4, 1, '2021-09-01', 0);
INSERT INTO `sys_user_role` VALUES (4, 2), (4, 3);

-- 宿舍：1层男，4层女
INSERT INTO `dorm_building` (id, campus_id, building_name, total_floors) VALUES (1, 1, '海棠苑1号', 6);
INSERT INTO `dorm_floor` (id, building_id, floor_num, gender_type) VALUES (1, 1, 1, 1), (2, 1, 4, 2);
INSERT INTO `dorm_room` (id, floor_id, room_no) VALUES (1, 1, '101'), (2, 2, '401');
INSERT INTO `dorm_bed` (room_id, bed_no, bed_status) VALUES (1, '1号', 0), (2, '1号', 0);

SET FOREIGN_KEY_CHECKS = 1;
