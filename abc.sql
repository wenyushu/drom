/*
  大学宿舍管理系统 - 最终全功能逻辑版 v7 (溪 & 朋友 修正版)
  
  核心修正：
  1. 修复了 INSERT 数据中 entry_year 导致的字段名错误（统一改为 entry_date）。
  2. 审计字段全覆盖：所有业务表均包含 create_by, create_time, update_by, update_time。
  3. 逻辑闭环：(id_card, username) 联合唯一，解决物理人多重账号历史归档问题。
  4. 细节恢复：完整保留 18 个维度的 biz_user_preference 偏好表。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =================================================================================
-- 1. 行政组织架构模块 (管理者视角)
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
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
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
  `dept_id` bigint NOT NULL COMMENT '所属院系ID',
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
  `grade` varchar(10) NOT NULL COMMENT '入学年份',
  `class_name` varchar(100) NOT NULL,
  `counselor_id` bigint DEFAULT NULL COMMENT '关联辅导员ID',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.4 班级表';


-- =================================================================================
-- 2. 用户与权限模块 (身份排他 + 物理人追踪)
-- =================================================================================

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL COMMENT '学号/工号 (唯一)',
  `id_card` varchar(20) NOT NULL COMMENT '身份证号 (物理标识)',
  `password` varchar(100) NOT NULL DEFAULT 'e10adc3949ba59abbe56e057f20f883e',
  `real_name` varchar(50) NOT NULL,
  `user_type` tinyint NOT NULL COMMENT '主身份：0-学生, 1-正式职员, 2-后勤人员',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-有效, 0-历史归档',
  `sex` tinyint DEFAULT '1' COMMENT '1-男, 2-女',
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_person_account` (`id_card`, `username`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.1 用户基础表';

DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL,
  `role_key` varchar(50) NOT NULL COMMENT 'admin, student, counselor, repairman',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.2 角色表';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.3 权限叠加表';


-- =================================================================================
-- 3. 详细档案模块
-- =================================================================================

DROP TABLE IF EXISTS `stu_student`;
CREATE TABLE `stu_student` (
  `id` bigint NOT NULL COMMENT '关联sys_user.id',
  `class_id` bigint DEFAULT NULL,
  `entry_date` date NOT NULL COMMENT '学号对应的入学日期',
  `graduation_date` date DEFAULT NULL COMMENT '预计/实际毕业日期',
  `academic_status` tinyint DEFAULT '0' COMMENT '0-在读, 1-休学, 2-退学, 3-毕业',
  `current_bed_id` bigint DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3.1 学生详细档案';

DROP TABLE IF EXISTS `biz_staff_info`;
CREATE TABLE `biz_staff_info` (
  `id` bigint NOT NULL COMMENT '关联sys_user.id',
  `dept_id` bigint DEFAULT NULL,
  `category` tinyint NOT NULL COMMENT '1-正式職工, 2-后勤/外包',
  `job_title` varchar(50) DEFAULT NULL COMMENT '职称/职务',
  `hire_date` date NOT NULL COMMENT '入职日期',
  `resign_date` date DEFAULT NULL,
  `staff_status` tinyint DEFAULT '0' COMMENT '0-在职, 1-离职, 2-退休',
  `current_bed_id` bigint DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3.2 职员档案表';


-- =================================================================================
-- 4. 宿舍物理资源模块 (含混住分层逻辑)
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
  `gender_type` tinyint DEFAULT '0' COMMENT '性别规则：0-不限, 1-男层, 2-女层',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_b_f` (`building_id`, `floor_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.2 楼层配置表';

DROP TABLE IF EXISTS `dorm_room`;
CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `floor_id` bigint NOT NULL,
  `room_no` varchar(20) NOT NULL,
  `room_capacity` int NOT NULL DEFAULT '4',
  `room_purpose` char(2) DEFAULT '00' COMMENT '00-学生, 01-宿管, 02-物资',
  `room_status` tinyint DEFAULT '1' COMMENT '1-正常, 0-封禁',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_f_r` (`floor_id`,`room_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.3 房间表';

DROP TABLE IF EXISTS `dorm_bed`;
CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `bed_no` varchar(10) NOT NULL,
  `bed_status` tinyint DEFAULT '0' COMMENT '0-置空, 1-占用, 2-维修, 3-预留, 4-封禁',
  `occupant_id` bigint DEFAULT NULL COMMENT 'sys_user.id',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.4 床位信息';


-- =================================================================================
-- 5. 核心业务与全维度偏好 (恢复 18 个偏好字段)
-- =================================================================================

-- 房间资产
DROP TABLE IF EXISTS `dorm_room_asset`;
CREATE TABLE `dorm_room_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `asset_name` varchar(100) NOT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1-正常, 0-损坏',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产表';

-- 水电费
DROP TABLE IF EXISTS `biz_billing_record`;
CREATE TABLE `biz_billing_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1-电, 2-水',
  `amount` decimal(10,2) NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-未缴, 1-已缴',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计费表';

-- 溪，这是你要求的【全维度住宿偏好表】
DROP TABLE IF EXISTS `biz_user_preference`;
CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL COMMENT '用户ID (PK, FK: sys_user)',
  `is_smoker` tinyint DEFAULT '0' COMMENT '是否吸烟 (0: 否, 1: 是)',
  `is_drinker` tinyint DEFAULT '0' COMMENT '是否饮酒 (0: 否, 1: 是)',
  `wake_type` char(1) DEFAULT '0' COMMENT '起床习惯 (0: 早起, 1: 随性, 2: 晚起)',
  `sleep_type` char(1) DEFAULT '0' COMMENT '作息习惯 (0: 早睡, 1: 晚睡, 2: 熬夜)',
  `is_light_sleeper` tinyint DEFAULT '0' COMMENT '是否浅眠',
  `study_at_night` tinyint DEFAULT '0' COMMENT '是否有夜间学习/工作习惯',
  `mobile_game_freq` char(1) DEFAULT '0' COMMENT '手游频率 (0: 不玩, 1: 偶尔, 2: 经常)',
  `board_game_interest` tinyint DEFAULT '0' COMMENT '对桌游的兴趣',
  `cleanliness_level` char(1) DEFAULT '1' COMMENT '卫生整洁度 (0: 邋遢, 1: 一般, 2: 整洁)',
  `air_condition_pref` char(1) DEFAULT '0' COMMENT '空调/温度偏好 (0: 适中, 1: 喜欢冷, 2: 喜欢热)',
  `noise_tolerance` char(1) DEFAULT '1' COMMENT '噪音容忍度 (0: 低, 1: 一般, 2: 高)',
  `guest_frequency` char(1) DEFAULT '0' COMMENT '访客频率',
  `study_location_pref` char(1) DEFAULT '0' COMMENT '学习地点偏好',
  `in_room_noise_level` char(1) DEFAULT '0' COMMENT '室内噪音水平',
  `smell_sensitivity` char(1) DEFAULT '0' COMMENT '气味敏感度',
  `hobby_tags` varchar(500) DEFAULT NULL COMMENT '爱好标签 (逗号分隔)',
  `group_code` varchar(50) DEFAULT NULL COMMENT '组队分配码',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全量用户住宿偏好表';

-- 报修与离校
DROP TABLE IF EXISTS `biz_repair_order`;
CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `description` text NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-待派单, 1-维修中, 2-完成',
  `handler_id` bigint DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修记录';

DROP TABLE IF EXISTS `stu_leave_status`;
CREATE TABLE `stu_leave_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `status_type` tinyint NOT NULL COMMENT '0-在校, 1-离校',
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生考勤表';


-- =================================================================================
-- 6. 初始化核心测试数据 (修正 1054 报错并同步权限)
-- =================================================================================

-- 6.1 角色初始化
INSERT INTO `sys_role` (id, role_name, role_key, create_by) VALUES (1, '超级管理员', 'admin', 'SYSTEM'), (2, '在校学生', 'student', 'SYSTEM'), (3, '班级辅导员', 'counselor', 'SYSTEM'), (4, '维修工', 'repairman', 'SYSTEM');

-- 6.2 行政结构
INSERT INTO `sys_campus` (id, campus_name, campus_code) VALUES (1, '东湖校区', 'EAST');
INSERT INTO `sys_department` (id, campus_id, dept_name, dept_type) VALUES (1, 1, '信息工程学院', 0), (2, 1, '后勤保障部', 2);
INSERT INTO `sys_major` (id, dept_id, major_name) VALUES (1, 1, '软件工程');
INSERT INTO `biz_class` (id, major_id, grade, class_name) VALUES (1, 1, '2021', '软件2101班');

-- 6.3 物理人生命周期测试 (张三)
-- 本科账号 (已毕业)
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status, create_by) 
VALUES (2, 'S2017001', '110101199901018888', '张三', 0, 0, 'SYSTEM'); 
INSERT INTO `stu_student` (id, class_id, entry_date, academic_status, create_by) 
VALUES (2, 1, '2017-09-01', 3, 'SYSTEM');

-- 研究生账号 (当前活动)
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status, create_by) 
VALUES (3, 'S2024G008', '110101199901018888', '张三', 0, 1, 'SYSTEM'); 
INSERT INTO `stu_student` (id, class_id, entry_date, academic_status, create_by) 
VALUES (3, 1, '2024-09-01', 0, 'SYSTEM'); -- 此处已修复
INSERT INTO `sys_user_role` VALUES (3, 2);

-- 6.4 身份叠加测试 (李华)
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status, create_by) 
VALUES (4, 'S2021111', '440101200305051234', '李华', 0, 1, 'SYSTEM');
INSERT INTO `stu_student` (id, class_id, entry_date, academic_status, create_by) 
VALUES (4, 1, '2021-09-01', 0, 'SYSTEM');
INSERT INTO `sys_user_role` VALUES (4, 2), (4, 3); 

-- 6.5 宿舍布局案例 (1层男, 4层女)
INSERT INTO `dorm_building` (id, campus_id, building_name, total_floors, create_by) VALUES (1, 1, '知行楼1号', 6, 'SYSTEM');
INSERT INTO `dorm_floor` (id, building_id, floor_num, gender_type, create_by) VALUES (1, 1, 1, 1, 'SYSTEM'), (2, 1, 4, 2, 'SYSTEM');
INSERT INTO `dorm_room` (id, floor_id, room_no, create_by) VALUES (1, 1, '101', 'SYSTEM'), (2, 2, '401', 'SYSTEM');
INSERT INTO `dorm_bed` (id, room_id, bed_no, bed_status) VALUES (1, 1, '1号', 0), (2, 1, '2号', 2), (3, 2, '1号', 0);

SET FOREIGN_KEY_CHECKS = 1;
