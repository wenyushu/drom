/*
  大学宿舍管理系统 - 数据库架构 v4 (最终修正版)
  
  核心逻辑设计：
  1. 行政链条：校区 (Campus) -> 院系 (Dept) -> 专业 (Major) -> 班级 (Class)。
  2. 宿舍链条：楼栋 (Building) -> 楼层 (Floor) -> 房间 (Room) -> 床位 (Bed)。
  3. 人员逻辑：
     - 使用 id_card (身份证) 标识物理人，username (学工号) 标识单次身份。
     - status (1:活动, 0:归档) 区分当前与历史。
     - 学生/老师/后勤主身份排他，权限通过 sys_user_role 动态赋予（支持学生兼任职能）。
  4. 资源控制：支持按楼层划分性别，床位状态细分为：空闲、占用、预留、维修、封禁。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =================================================================================
-- 1. 行政组织架构 (校区 -> 院系 -> 专业 -> 班级)
-- =================================================================================

-- 校区表
DROP TABLE IF EXISTS `sys_campus`;
CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_name` varchar(100) NOT NULL COMMENT '校区名称 (如：南校区)',
  `address` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '状态 (1:启用, 0:停用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.1 校区表';

-- 院系/学院表
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint NOT NULL COMMENT '所属校区ID',
  `dept_name` varchar(100) NOT NULL COMMENT '学院/部门名称',
  `dept_type` tinyint DEFAULT '0' COMMENT '0-教学学院, 1-行政部门, 2-后勤部门',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.2 院系部门表';

-- 专业表
DROP TABLE IF EXISTS `sys_major`;
CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_id` bigint NOT NULL COMMENT '所属院系ID',
  `major_name` varchar(100) NOT NULL COMMENT '专业名称 (如：软件工程)',
  `duration` int DEFAULT '4' COMMENT '学制年限',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.3 专业表';

-- 班级表
DROP TABLE IF EXISTS `biz_class`;
CREATE TABLE `biz_class` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `major_id` bigint NOT NULL COMMENT '所属专业ID',
  `grade` varchar(10) NOT NULL COMMENT '入学年份 (如：2023)',
  `class_name` varchar(100) NOT NULL COMMENT '班级全称',
  `counselor_id` bigint DEFAULT NULL COMMENT '关联辅导员(sys_user.id)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='1.4 班级表';


-- =================================================================================
-- 2. 用户与权限模块 (RBAC + 物理人唯一标识)
-- =================================================================================

-- 系统用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL COMMENT '学号/工号 (逻辑账号，唯一)',
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `id_card` varchar(20) NOT NULL COMMENT '身份证号 (物理人标识)',
  `user_type` tinyint NOT NULL COMMENT '主身份：0-管理员, 1-学生, 2-正式职员, 3-后勤人员',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-当前在校/职, 0-历史归档(不再使用)',
  `sex` tinyint DEFAULT '0' COMMENT '性别：1-男, 2-女',
  `phone` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  INDEX `idx_id_card` (`id_card`) COMMENT '用于多账号追溯'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.1 用户基础表';

-- 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL,
  `role_key` varchar(50) NOT NULL COMMENT '角色Key: admin, student, counselor, repairman, logistics',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.2 角色表';

-- 用户角色关联 (支持学生兼职通过分配角色获得管理权限)
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.3 用户角色中间表';

-- 权限菜单表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT '0',
  `menu_name` varchar(50) NOT NULL,
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识符',
  `menu_type` char(1) DEFAULT 'C' COMMENT 'M-目录, C-菜单, F-按钮',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='2.4 菜单表';


-- =================================================================================
-- 3. 详细档案模块 (学籍与人事生命周期)
-- =================================================================================

-- 学生扩展信息表
DROP TABLE IF EXISTS `stu_student`;
CREATE TABLE `stu_student` (
  `id` bigint NOT NULL COMMENT '关联sys_user.id',
  `class_id` bigint DEFAULT NULL,
  `entry_year` varchar(10) NOT NULL COMMENT '入学年份',
  `expected_graduation` date DEFAULT NULL COMMENT '预计毕业日期',
  `academic_status` tinyint DEFAULT '0' COMMENT '学籍：0-在读, 1-休学, 2-退学, 3-毕业, 4-考回(研究生)',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前入住床位',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3.1 学生信息表';

-- 职员扩展信息表 (学校工作人员 + 后勤人员)
DROP TABLE IF EXISTS `biz_staff_info`;
CREATE TABLE `biz_staff_info` (
  `id` bigint NOT NULL COMMENT '关联sys_user.id',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门',
  `staff_category` tinyint NOT NULL COMMENT '分类：1-正式职工(行政/讲师等), 2-后勤人员(维修/保洁/外包)',
  `title` varchar(50) DEFAULT NULL COMMENT '职称或职位名称',
  `hire_date` date NOT NULL COMMENT '入职日期',
  `resign_date` date DEFAULT NULL COMMENT '离职/退休日期',
  `staff_status` tinyint DEFAULT '0' COMMENT '0-在职, 1-离职, 2-退休',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前床位(如果住校)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='3.2 职员后勤信息表';


-- =================================================================================
-- 4. 宿舍资源模块 (支持男女混住楼层划分)
-- =================================================================================

-- 楼栋表
DROP TABLE IF EXISTS `dorm_building`;
CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint NOT NULL,
  `building_name` varchar(50) NOT NULL,
  `total_floors` int NOT NULL,
  `manager_id` bigint DEFAULT NULL COMMENT '责任宿管ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.1 楼栋表';

-- 楼层表 (通过此表控制混住逻辑)
DROP TABLE IF EXISTS `dorm_floor`;
CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_id` bigint NOT NULL,
  `floor_num` int NOT NULL COMMENT '物理层数',
  `gender_type` tinyint DEFAULT '0' COMMENT '性别规则：0-混住, 1-男生层, 2-女生层',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_building_floor` (`building_id`,`floor_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.2 楼层表';

-- 房间表
DROP TABLE IF EXISTS `dorm_room`;
CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `floor_id` bigint NOT NULL,
  `room_no` varchar(20) NOT NULL,
  `room_capacity` int NOT NULL DEFAULT '4' COMMENT '物理床位数',
  `room_purpose` char(2) DEFAULT '00' COMMENT '00-学生宿舍, 01-宿管房, 02-物资房, 03-预留',
  `room_status` tinyint DEFAULT '1' COMMENT '1-正常, 0-封禁/不可用',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_floor_room` (`floor_id`,`room_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.3 房间表';

-- 床位表
DROP TABLE IF EXISTS `dorm_bed`;
CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `bed_no` varchar(10) NOT NULL,
  `bed_status` tinyint DEFAULT '0' COMMENT '状态：0-无人置空, 1-已占用, 2-维修中, 3-预留, 4-封禁',
  `occupant_id` bigint DEFAULT NULL COMMENT '当前入住人sys_user.id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='4.4 床位表';


-- =================================================================================
-- 5. 业务运行模块 (资产、水电、报修、调宿)
-- =================================================================================

-- 房间资产表
DROP TABLE IF EXISTS `dorm_room_asset`;
CREATE TABLE `dorm_room_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `asset_name` varchar(100) NOT NULL COMMENT '空调、桌椅等',
  `status` tinyint DEFAULT '1' COMMENT '1-正常, 0-损坏',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.1 资产表';

-- 水电费计费
DROP TABLE IF EXISTS `biz_billing_record`;
CREATE TABLE `biz_billing_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1-电费, 2-水费',
  `amount` decimal(10,2) NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-未缴, 1-已缴',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.2 计费表';

-- 报修单
DROP TABLE IF EXISTS `biz_repair_order`;
CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_id` bigint NOT NULL,
  `room_id` bigint NOT NULL,
  `description` text NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-待派单, 1-处理中, 2-已完成, 3-无法修复',
  `handler_id` bigint DEFAULT NULL COMMENT '维修人员工号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.3 报修单表';

-- 调宿申请表
DROP TABLE IF EXISTS `biz_room_change_request`;
CREATE TABLE `biz_room_change_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `reason` text NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-审批中, 1-通过, 2-驳回, 3-已执行',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.4 调宿表';

-- 住宿偏好
DROP TABLE IF EXISTS `user_preference`;
CREATE TABLE `user_preference` (
  `user_id` bigint NOT NULL,
  `sleep_habits` tinyint DEFAULT '0' COMMENT '0-早睡, 1-晚睡',
  `is_snoring` tinyint DEFAULT '0' COMMENT '0-不打呼, 1-打呼',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='5.5 偏好表';


-- =================================================================================
-- 6. 初始化核心测试数据 (反映复杂生命周期与人员属性)
-- =================================================================================

-- 6.1 初始化角色
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin'), (2, '学生', 'student'), (3, '辅导员', 'counselor'), (4, '后勤管理', 'logistics'), (5, '维修工', 'repairman');

-- 6.2 组织架构
INSERT INTO `sys_campus` (id, campus_name) VALUES (1, '东校区'), (2, '西校区');
INSERT INTO `sys_department` (id, campus_id, dept_name, dept_type) VALUES (1, 1, '计算机学院', 0), (2, 1, '后勤保障部', 2);
INSERT INTO `sys_major` (id, dept_id, major_name) VALUES (1, 1, '软件工程');
INSERT INTO `biz_class` (id, major_id, grade, class_name) VALUES (1, 1, '2021', '软件21-1班');

-- 6.3 用户数据 (密码均为 123456 的散列: e10adc3949ba59abbe56e057f20f883e)

-- A. 张同学：身份证 1001。2017年入校本科(已毕业归档)，2024年入校读研(当前活动)
INSERT INTO `sys_user` (id, username, password, real_name, id_card, user_type, status, sex) 
VALUES (2, '20170001', 'e10adc3949ba59abbe56e057f20f883e', '张三', '110101199901018888', 1, 0, 1); -- 历史
INSERT INTO `sys_user` (id, username, password, real_name, id_card, user_type, status, sex) 
VALUES (3, '20248001', 'e10adc3949ba59abbe56e057f20f883e', '张三', '110101199901018888', 1, 1, 1); -- 当前研究生

-- B. 李同学：2021级本科生，且兼任“辅导员助理” (身份叠加)
INSERT INTO `sys_user` (id, username, password, real_name, id_card, user_type, status, sex) 
VALUES (4, '20210001', 'e10adc3949ba59abbe56e057f20f883e', '李华', '440101200305051234', 1, 1, 1);

-- C. 职员与后勤
INSERT INTO `sys_user` (id, username, password, real_name, id_card, user_type, status, sex) 
VALUES (5, 'T9001', 'e10adc3949ba59abbe56e057f20f883e', '王老师', '440101198501017777', 2, 1, 2);
INSERT INTO `sys_user` (id, username, password, real_name, id_card, user_type, status, sex) 
VALUES (6, 'W8001', 'e10adc3949ba59abbe56e057f20f883e', '陈师傅', '440101197508082222', 3, 1, 1);

-- 6.4 分配角色权限
INSERT INTO `sys_user_role` VALUES (3, 2); -- 张三(研) -> 学生角色
INSERT INTO `sys_user_role` VALUES (4, 2), (4, 3); -- 李华 -> 学生角色 + 辅导员权限 (身份叠加示例)
INSERT INTO `sys_user_role` VALUES (5, 3); -- 王老师 -> 辅导员角色
INSERT INTO `sys_user_role` VALUES (6, 5); -- 陈师傅 -> 维修工角色

-- 6.5 档案
INSERT INTO `stu_student` (id, class_id, entry_year, academic_status) VALUES (2, 1, '2017', 3); -- 毕业归档
INSERT INTO `stu_student` (id, class_id, entry_year, academic_status) VALUES (3, 1, '2024', 0); -- 在读
INSERT INTO `stu_student` (id, class_id, entry_year, academic_status) VALUES (4, 1, '2021', 0); -- 在读
INSERT INTO `biz_staff_info` (id, dept_id, staff_category, hire_date) VALUES (5, 1, 1, '2015-07-01');
INSERT INTO `biz_staff_info` (id, dept_id, staff_category, hire_date) VALUES (6, 2, 2, '2022-03-15');

-- 6.6 宿舍布局演示 (1栋6层，1-3男，4-6女)
INSERT INTO `dorm_building` VALUES (1, 1, '知行楼', 6, 5);
-- 1层(男)
INSERT INTO `dorm_floor` VALUES (1, 1, 1, 1);
INSERT INTO `dorm_room` (id, floor_id, room_no, room_capacity) VALUES (1, 1, '101', 4);
INSERT INTO `dorm_bed` (room_id, bed_no, bed_status) VALUES (1, '1', 0), (1, '2', 4), (1, '3', 2), (1, '4', 0); -- 包含封禁与维修
-- 4层(女)
INSERT INTO `dorm_floor` VALUES (2, 1, 4, 2);
INSERT INTO `dorm_room` (id, floor_id, room_no, room_capacity) VALUES (2, 2, '401', 4);

SET FOREIGN_KEY_CHECKS = 1;
