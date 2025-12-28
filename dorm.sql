/*
  大学宿舍管理系统 - 数据库架构 v5 (本科毕业设计最终版)
  
  核心特性：
  1. 行政划分：校区 -> 院系 -> 专业 -> 班级
  2. 宿舍划分：楼栋 -> 楼层(含性别规则) -> 房间 -> 床位(含细分状态)
  3. 身份生命周期：身份证(id_card)追踪物理人，status区分历史归档，学工号独立。
  4. 职能重叠：主身份排他，通过角色叠加权限。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =================================================================================
-- 1. 行政组织架构 (管理者视角)
-- =================================================================================

-- 校区
DROP TABLE IF EXISTS `sys_campus`;
CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_name` varchar(100) NOT NULL COMMENT '校区名称',
  `campus_code` varchar(20) DEFAULT NULL COMMENT '校区编码',
  `address` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.1 校区表';

-- 院系/学院
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `dept_name` varchar(100) NOT NULL COMMENT '学院名称',
  `dept_type` tinyint DEFAULT '0' COMMENT '0-教学学院, 1-行政部门, 2-后勤部门',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.2 院系部门表';

-- 专业
DROP TABLE IF EXISTS `sys_major`;
CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_id` bigint NOT NULL COMMENT '所属院系ID',
  `major_name` varchar(100) NOT NULL,
  `duration` int DEFAULT '4' COMMENT '学制',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.3 专业表';

-- 班级
DROP TABLE IF EXISTS `biz_class`;
CREATE TABLE `biz_class` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `major_id` bigint NOT NULL COMMENT '所属专业ID',
  `grade` varchar(10) NOT NULL COMMENT '入学年份(级)',
  `class_name` varchar(100) NOT NULL COMMENT '班级全称',
  `counselor_id` bigint DEFAULT NULL COMMENT '关联辅导员(sys_user.id)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.4 班级表';


-- =================================================================================
-- 2. 用户与权限 (RBAC + 物理人追踪)
-- =================================================================================

-- 用户核心表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL COMMENT '学号/工号 (逻辑账号，唯一标识单次入校)',
  `password` varchar(100) NOT NULL DEFAULT 'e10adc3949ba59abbe56e057f20f883e' COMMENT '123456',
  `real_name` varchar(50) NOT NULL,
  `id_card` varchar(20) NOT NULL COMMENT '身份证号 (物理人标识)',
  `user_type` tinyint NOT NULL COMMENT '主身份：0-学生, 1-工作人员(正式), 2-后勤人员',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-当前在校/职, 0-历史数据(归档)',
  `sex` tinyint DEFAULT '0' COMMENT '1-男, 2-女',
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  INDEX `idx_id_card` (`id_card`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.1 系统用户表';

-- 角色与权限
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL,
  `role_key` varchar(50) NOT NULL COMMENT '标识: admin, student, counselor, repairman, logistics',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.2 角色表';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.3 权限叠加表';

DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT '0',
  `menu_name` varchar(50) NOT NULL,
  `perms` varchar(100) DEFAULT NULL COMMENT 'Sa-Token权限码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.4 菜单权限表';


-- =================================================================================
-- 3. 详细档案 (生命周期管理)
-- =================================================================================

-- 学生档案
DROP TABLE IF EXISTS `stu_student`;
CREATE TABLE `stu_student` (
  `id` bigint NOT NULL COMMENT '关联sys_user.id',
  `class_id` bigint DEFAULT NULL,
  `entry_year` varchar(10) NOT NULL COMMENT '入学年份',
  `expected_graduation` date DEFAULT NULL COMMENT '预计毕业',
  `academic_status` tinyint DEFAULT '0' COMMENT '0-在读, 1-休学, 2-退学, 3-毕业',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前床位ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3.1 学生档案';

-- 工作人员档案 (教职工 + 后勤)
DROP TABLE IF EXISTS `biz_staff_info`;
CREATE TABLE `biz_staff_info` (
  `id` bigint NOT NULL COMMENT '关联sys_user.id',
  `dept_id` bigint DEFAULT NULL,
  `category` tinyint NOT NULL COMMENT '1-学校正式职工, 2-后勤人员/外包',
  `title` varchar(50) DEFAULT NULL COMMENT '职务名称',
  `entry_date` date NOT NULL COMMENT '入职日期',
  `resign_date` date DEFAULT NULL COMMENT '离职日期',
  `staff_status` tinyint DEFAULT '0' COMMENT '0-在职, 1-离职, 2-退休',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前床位(如果住校)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3.2 职员档案';


-- =================================================================================
-- 4. 宿舍资源模块 (含楼层性别划分)
-- =================================================================================

-- 楼栋
DROP TABLE IF EXISTS `dorm_building`;
CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint NOT NULL,
  `building_name` varchar(50) NOT NULL,
  `total_floors` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.1 楼栋表';

-- 楼层
DROP TABLE IF EXISTS `dorm_floor`;
CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_id` bigint NOT NULL,
  `floor_num` int NOT NULL,
  `gender_type` tinyint DEFAULT '0' COMMENT '性别规则：0-混住, 1-男生层, 2-女生层',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_b_f` (`building_id`,`floor_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.2 楼层表';

-- 房间
DROP TABLE IF EXISTS `dorm_room`;
CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `floor_id` bigint NOT NULL,
  `room_no` varchar(20) NOT NULL,
  `room_purpose` char(2) DEFAULT '00' COMMENT '00-学生, 01-宿管, 02-物资, 03-预留',
  `room_status` tinyint DEFAULT '1' COMMENT '1-正常, 0-封禁',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.3 房间表';

-- 床位
DROP TABLE IF EXISTS `dorm_bed`;
CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `bed_no` varchar(10) NOT NULL,
  `bed_status` tinyint DEFAULT '0' COMMENT '0-置空, 1-占用, 2-维修, 3-预留, 4-封禁',
  `occupant_id` bigint DEFAULT NULL COMMENT 'sys_user.id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.4 床位表';


-- =================================================================================
-- 5. 业务运行模块 (水电、报修、偏好、请假)
-- =================================================================================

-- 资产表
DROP TABLE IF EXISTS `dorm_room_asset`;
CREATE TABLE `dorm_room_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `asset_name` varchar(100) NOT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1-正常, 0-损坏',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.1 房间资产';

-- 水电账单
DROP TABLE IF EXISTS `biz_billing_record`;
CREATE TABLE `biz_billing_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1-水, 2-电',
  `amount` decimal(10,2) NOT NULL,
  `usage_val` decimal(10,2) NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-未缴, 1-已缴',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.2 计费记录';

-- 报修单
DROP TABLE IF EXISTS `biz_repair_order`;
CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `description` text NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-待派单, 1-维修中, 2-完成, 3-无法修复',
  `handler_id` bigint DEFAULT NULL COMMENT '维修人员工号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.3 报修工单';

-- 住宿偏好
DROP TABLE IF EXISTS `user_preference`;
CREATE TABLE `user_preference` (
  `user_id` bigint NOT NULL,
  `sleep_habits` tinyint DEFAULT '0' COMMENT '0-早睡, 1-晚睡',
  `is_snoring` tinyint DEFAULT '0' COMMENT '0-否, 1-是',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.4 住宿偏好';

-- 请假离校
DROP TABLE IF EXISTS `stu_leave_status`;
CREATE TABLE `stu_leave_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-审批中, 1-已批准, 2-已销假',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.5 学生请假';


-- =================================================================================
-- 6. 测试数据修补 (体现人员生命周期与复杂场景)
-- =================================================================================

-- 角色
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin'), (2, '学生', 'student'), (3, '辅导员', 'counselor'), (4, '后勤人员', 'logistics'), (5, '维修工', 'repairman');

-- 行政
INSERT INTO `sys_campus` (id, campus_name) VALUES (1, '主校区');
INSERT INTO `sys_department` (id, campus_id, dept_name, dept_type) VALUES (1, 1, '计科系', 0), (2, 1, '后勤部', 2);
INSERT INTO `sys_major` (id, dept_id, major_name) VALUES (1, 1, '软件工程');
INSERT INTO `biz_class` (id, major_id, grade, class_name) VALUES (1, 1, '2021', '软工21-1班');

-- 人员：张三 (二次入校示例)
-- 1. 张三旧账号 (2017级本科生，已毕业归档)
INSERT INTO `sys_user` (id, username, real_name, id_card, user_type, status, sex) 
VALUES (2, 'S2017001', '张三', '110101199901018888', 0, 0, 1);
INSERT INTO `stu_student` (id, class_id, entry_year, academic_status) VALUES (2, 1, '2017', 3);

-- 2. 张三新账号 (2024级研究生，当前活动)
INSERT INTO `sys_user` (id, username, real_name, id_card, user_type, status, sex) 
VALUES (3, 'S2024888', '张三', '110101199901018888', 0, 1, 1);
INSERT INTO `stu_student` (id, class_id, entry_year, academic_status) VALUES (3, 1, '2024', 0);
INSERT INTO `sys_user_role` VALUES (3, 2); -- 分配学生角色

-- 人员：李华 (身份叠加示例：学生兼辅导员助理)
INSERT INTO `sys_user` (id, username, real_name, id_card, user_type, status, sex) 
VALUES (4, 'S2021001', '李华', '440101200305051234', 0, 1, 1);
INSERT INTO `stu_student` (id, class_id, entry_year, academic_status) VALUES (4, 1, '2021', 0);
INSERT INTO `sys_user_role` VALUES (4, 2), (4, 3); -- 赋予学生 + 辅导员 两个角色的权限

-- 职员数据
INSERT INTO `sys_user` (id, username, real_name, id_card, user_type, status, sex) 
VALUES (5, 'T1001', '王辅导', '440101198501017777', 1, 1, 2);
INSERT INTO `biz_staff_info` (id, dept_id, category, entry_date) VALUES (5, 1, 1, '2020-09-01');
INSERT INTO `sys_user_role` VALUES (5, 3);

-- 宿舍资源：知行楼1栋 (1层男, 2层女)
INSERT INTO `dorm_building` (id, campus_id, building_name, total_floors) VALUES (1, 1, '知行楼1栋', 6);
INSERT INTO `dorm_floor` (id, building_id, floor_num, gender_type) VALUES (1, 1, 1, 1), (2, 1, 2, 2);
-- 101房及床位
INSERT INTO `dorm_room` (id, floor_id, room_no) VALUES (1, 1, '101');
INSERT INTO `dorm_bed` (room_id, bed_no, bed_status) VALUES (1, '1', 0), (1, '2', 2), (1, '3', 4), (1, '4', 0);

SET FOREIGN_KEY_CHECKS = 1;
