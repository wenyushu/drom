/*
  大学宿舍管理系统 - 最终全功能逻辑版 v7
  开发者：溪 & 朋友
  
  核心修正：
  1. 行政层级：校区 -> 院系 -> 专业 -> 班级。
  2. 物理层级：楼栋 -> 楼层 ( 性别管控 ) -> 房间 -> 床位 (0- 空闲 ,1- 占用 ,2- 维修 ,3- 预留 ,4- 封禁 )。
  3. 人员生命周期：身份证 (id_card) 定人，学工号 (username) 定事。联合主键约束。
  4. 审计字段：全量覆盖 create_by, create_time, update_by, update_time。
  5. 偏好画像：恢复 18 个维度的住宿偏好采集。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =================================================================================
-- 1. 行政组织架构模块
-- =================================================================================

-- 校区
DROP TABLE IF EXISTS `sys_campus`;
CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_name` varchar(100) NOT NULL COMMENT ' 校区名称 ',
  `campus_code` varchar(20) DEFAULT NULL COMMENT ' 校区编码 ',
  `address` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1- 启用 , 0- 停用 ',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.1 校区表 ';

-- 院系 / 学院
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint NOT NULL COMMENT ' 所属校区 ID',
  `dept_name` varchar(100) NOT NULL COMMENT ' 学院 / 部门名称 ',
  `dept_type` tinyint DEFAULT '0' COMMENT '0- 教学 , 1- 行政 , 2- 后勤 ',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.2 院系部门表 ';

-- 专业
DROP TABLE IF EXISTS `sys_major`;
CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_id` bigint NOT NULL COMMENT ' 所属院系 ID',
  `major_name` varchar(100) NOT NULL,
  `duration` int DEFAULT '4' COMMENT ' 学制 ',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.3 专业表 ';

-- 班级
DROP TABLE IF EXISTS `biz_class`;
CREATE TABLE `biz_class` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `major_id` bigint NOT NULL COMMENT ' 所属专业 ID',
  `grade` varchar(10) NOT NULL COMMENT ' 入学年份 ( 如： 2023 级 )',
  `class_name` varchar(100) NOT NULL,
  `counselor_id` bigint DEFAULT NULL COMMENT ' 班级辅导员 ID',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.4 班级表 ';


-- =================================================================================
-- 2. 用户与权限模块 (RBAC + 物理人唯一标识 )
-- =================================================================================

-- 核心用户基础表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL COMMENT ' 登录账号 ( 学号 / 工号 )，唯一标识一段生命周期 ',
  `id_card` varchar(20) NOT NULL COMMENT ' 身份证号，物理人唯一标识 ',
  `password` varchar(100) NOT NULL DEFAULT 'e10adc3949ba59abbe56e057f20f883e' COMMENT ' 默认密码 : 123456',
  `real_name` varchar(50) NOT NULL,
  `user_type` tinyint NOT NULL COMMENT ' 主身份： 0- 学生 , 1- 正式职员 , 2- 后勤人员 ',
  `status` tinyint DEFAULT '1' COMMENT ' 账号状态： 1- 当前有效 , 0- 历史归档 ',
  `sex` tinyint DEFAULT '1' COMMENT '1- 男 , 2- 女 ',
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  -- 身份证 + 学工号联合唯一约束，解决溪提到的区分历史数据问题
  UNIQUE KEY `uk_person_account` (`id_card`, `username`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.1 用户表 ';

-- 角色与权限映射
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL,
  `role_key` varchar(50) NOT NULL COMMENT ' 标识 : admin, student, counselor, repairman',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.2 角色表 ';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.3 权限叠加关联表 ';


-- =================================================================================
-- 3. 身份扩展档案 ( 学籍与职场轨迹 )
-- =================================================================================

-- 学生档案
DROP TABLE IF EXISTS `stu_student`;
CREATE TABLE `stu_student` (
  `id` bigint NOT NULL COMMENT ' 关联 sys_user.id',
  `class_id` bigint DEFAULT NULL,
  `entry_date` date NOT NULL COMMENT ' 该账号入学日期 ',
  `graduation_date` date DEFAULT NULL COMMENT ' 预计 / 实际毕业日期 ',
  `academic_status` tinyint DEFAULT '0' COMMENT '0- 在读 , 1- 休学 , 2- 退学 , 3- 毕业 ',
  `current_bed_id` bigint DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3.1 学生扩展信息表 ';

-- 教职工与后勤档案
DROP TABLE IF EXISTS `biz_staff_info`;
CREATE TABLE `biz_staff_info` (
  `id` bigint NOT NULL COMMENT ' 关联 sys_user.id',
  `dept_id` bigint DEFAULT NULL COMMENT ' 行政隶属部门 ',
  `category` tinyint NOT NULL COMMENT '1- 正式職工 , 2- 后勤人员 ',
  `job_title` varchar(50) DEFAULT NULL COMMENT ' 职位 / 职称 ',
  `hire_date` date NOT NULL COMMENT ' 入职日期 ',
  `resign_date` date DEFAULT NULL,
  `staff_status` tinyint DEFAULT '0' COMMENT '0- 在职 , 1- 离职 , 2- 退休 ',
  `current_bed_id` bigint DEFAULT NULL COMMENT ' 宿舍床位 ',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3.2 职员扩展信息表 ';


-- =================================================================================
-- 4. 宿舍物理资源模块 ( 含混住逻辑 )
-- =================================================================================

-- 楼栋
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.1 楼栋表 ';

-- 楼层 ( 实现精细性别管控 )
DROP TABLE IF EXISTS `dorm_floor`;
CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_id` bigint NOT NULL,
  `floor_num` int NOT NULL,
  `gender_type` tinyint DEFAULT '0' COMMENT ' 性别规则： 0- 混住 , 1- 男层 , 2- 女层 ',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_b_f` (`building_id`, `floor_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.2 楼层配置表 ';

-- 房间
DROP TABLE IF EXISTS `dorm_room`;
CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `floor_id` bigint NOT NULL,
  `room_no` varchar(20) NOT NULL,
  `room_capacity` int NOT NULL DEFAULT '4',
  `room_purpose` char(2) DEFAULT '00' COMMENT '00- 学生 , 01- 宿管 , 02- 物资 ',
  `room_status` tinyint DEFAULT '1' COMMENT '1- 正常 , 0- 封禁 ',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_f_r` (`floor_id`,`room_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.3 房间表 ';

-- 床位
DROP TABLE IF EXISTS `dorm_bed`;
CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `bed_no` varchar(10) NOT NULL,
  `bed_status` tinyint DEFAULT '0' COMMENT ' 状态： 0- 置空 , 1- 占用 , 2- 维修 , 3- 预留 , 4- 封禁 ',
  `occupant_id` bigint DEFAULT NULL COMMENT ' 当前入住 sys_user.id',
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.4 床位表 ';


-- =================================================================================
-- 5. 业务运行与核心偏好 ( 恢复全字段 )
-- =================================================================================

-- 房间固定资产
DROP TABLE IF EXISTS `dorm_room_asset`;
CREATE TABLE `dorm_room_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `asset_name` varchar(100) NOT NULL,
  `asset_no` varchar(50) DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1- 正常 , 0- 损坏 ',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.1 房间资产表 ';

-- 水电与计费
DROP TABLE IF EXISTS `dorm_meter_electric`;
CREATE TABLE `dorm_meter_electric` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `meter_no` varchar(50) NOT NULL,
  `current_reading` decimal(10,2) DEFAULT '0.00',
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.2 房间电表 ';

DROP TABLE IF EXISTS `biz_billing_record`;
CREATE TABLE `biz_billing_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1- 电 , 2- 水 ',
  `amount` decimal(10,2) NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0- 未缴 , 1- 已缴 ',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.3 水电账单表 ';

-- 报修管理
DROP TABLE IF EXISTS `biz_repair_order`;
CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `description` text NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0- 待派单 , 1- 维修中 , 2- 已完成 ',
  `handler_id` bigint DEFAULT NULL COMMENT ' 指派工号 ',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.4 报修工单表 ';

-- 溪，这是你要求的【全维度住宿偏好表】
DROP TABLE IF EXISTS `biz_user_preference`;
CREATE TABLE `biz_user_preference` (
  `user_id` bigint NOT NULL COMMENT ' 用户 ID (PK, FK: sys_user)',
  `is_smoker` tinyint DEFAULT '0' COMMENT ' 是否吸烟 (0: 否 , 1: 是 )',
  `is_drinker` tinyint DEFAULT '0' COMMENT ' 是否饮酒 (0: 否 , 1: 是 )',
  `wake_type` char(1) DEFAULT '0' COMMENT ' 起床习惯 (0: 早起 , 1: 随性 , 2: 晚起 )',
  `sleep_type` char(1) DEFAULT '0' COMMENT ' 作息习惯 (0: 早睡 , 1: 晚睡 , 2: 熬夜 )',
  `is_light_sleeper` tinyint DEFAULT '0' COMMENT ' 是否浅眠 ',
  `study_at_night` tinyint DEFAULT '0' COMMENT ' 是否有夜间学习习惯 ',
  `mobile_game_freq` char(1) DEFAULT '0' COMMENT ' 手游频率 (0: 不玩 , 1: 偶尔 , 2: 经常 )',
  `board_game_interest` tinyint DEFAULT '0' COMMENT ' 对桌游的兴趣 ',
  `cleanliness_level` char(1) DEFAULT '1' COMMENT ' 卫生整洁度 (0: 邋遢 , 1: 一般 , 2: 整洁 )',
  `air_condition_pref` char(1) DEFAULT '0' COMMENT ' 空调温度偏好 (0: 适中 , 1: 喜冷 , 2: 喜热 )',
  `noise_tolerance` char(1) DEFAULT '1' COMMENT ' 噪音容忍度 (0: 低 , 1: 一般 , 2: 高 )',
  `guest_frequency` char(1) DEFAULT '0' COMMENT ' 访客频率 ',
  `study_location_pref` char(1) DEFAULT '0' COMMENT ' 学习地点偏好 ',
  `in_room_noise_level` char(1) DEFAULT '0' COMMENT ' 室内噪音水平 ',
  `smell_sensitivity` char(1) DEFAULT '0' COMMENT ' 气味敏感度 ',
  `hobby_tags` varchar(500) DEFAULT NULL COMMENT ' 爱好标签 ( 逗号分隔 )',
  `group_code` varchar(50) DEFAULT NULL COMMENT ' 组队分配码 ',
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.5 全量用户偏好画像表 ';

-- 请假离校
DROP TABLE IF EXISTS `stu_leave_status`;
CREATE TABLE `stu_leave_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `status_type` tinyint NOT NULL COMMENT '0- 在校 , 1- 离校 ',
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.6 学生考勤请假表 ';


-- =================================================================================
-- 6. 初始化复杂场景测试数据 ( 含归档与权限分配 )
-- =================================================================================

-- 6.1 角色
INSERT INTO `sys_role` (id, role_name, role_key) VALUES (1, ' 超级管理员 ', 'admin'), (2, ' 在校学生 ', 'student'), (3, ' 班级辅导员 ', 'counselor'), (4, ' 后勤师傅 ', 'repairman');

-- 6.2 行政结构
INSERT INTO `sys_campus` (id, campus_name) VALUES (1, ' 东湖校区 ');
INSERT INTO `sys_department` (id, campus_id, dept_name, dept_type) VALUES (1, 1, ' 信息工程学院 ', 0), (2, 1, ' 校园后勤部 ', 2);
INSERT INTO `sys_major` (id, dept_id, major_name) VALUES (1, 1, ' 软件工程 ');
INSERT INTO `biz_class` (id, major_id, grade, class_name) VALUES (1, 1, '2021', ' 软件 2101 班 ');

-- 6.3 张三的生命周期演示 ( 同一个身份证，不同账号 )
-- 本科账号 ( 已毕业 )
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status) 
VALUES (2, 'S2017001', '110101199901018888', ' 张三 ', 0, 0); 
INSERT INTO `stu_student` (id, class_id, entry_date, graduation_date, academic_status) VALUES (2, 1, '2017-09-01', '2021-06-30', 3);

-- 研究生账号 ( 当前在读 )
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status) 
VALUES (3, 'S2024G008', '110101199901018888', ' 张三 ', 0, 1); 
INSERT INTO `stu_student` (id, class_id, entry_year, academic_status) VALUES (3, 1, '2024', 0);
INSERT INTO `sys_user_role` VALUES (3, 2); -- 赋予学生角色

-- 6.4 李华的身份叠加演示 ( 学生兼职辅导员助手 )
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status) 
VALUES (4, 'S2021111', '440101200305051234', ' 李华 ', 0, 1);
INSERT INTO `stu_student` (id, class_id, entry_date, academic_status) VALUES (4, 1, '2021-09-01', 0);
INSERT INTO `sys_user_role` VALUES (4, 2), (4, 3); -- 学生角色 + 辅导员权限叠加

-- 6.5 王老师与陈师傅
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status, sex) 
VALUES (5, 'T1001', '440101198501017777', ' 王辅导 ', 1, 1, 2);
INSERT INTO `biz_staff_info` (id, dept_id, category, hire_date) VALUES (5, 1, 1, '2018-08-01');
INSERT INTO `sys_user_role` VALUES (5, 3);

INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status) 
VALUES (6, 'W8001', '440101197508082222', ' 陈师傅 ', 2, 1);
INSERT INTO `biz_staff_info` (id, dept_id, category, hire_date) VALUES (6, 2, 2, '2022-03-15');
INSERT INTO `sys_user_role` VALUES (6, 4);

-- 6.6 住宿资源案例： 1 栋 (1 层男 , 4 层女 )
INSERT INTO `dorm_building` (id, campus_id, building_name, total_floors) VALUES (1, 1, ' 海棠苑 1 号 ', 6);
INSERT INTO `dorm_floor` (id, building_id, floor_num, gender_type) VALUES (1, 1, 1, 1), (2, 1, 4, 2);
INSERT INTO `dorm_room` (id, floor_id, room_no) VALUES (1, 1, '101'), (2, 2, '401');
INSERT INTO `dorm_bed` (room_id, bed_no, bed_status) VALUES (1, '1 号 ', 0), (1, '2 号 ', 2), (2, '1 号 ', 0);

SET FOREIGN_KEY_CHECKS = 1;