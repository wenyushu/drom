/*
  大学宿舍管理系统 - 最终逻辑修正版
  
  核心特性：
  1. 行政链条：校区 -> 院系 -> 专业 -> 班级。
  2. 宿舍链条：楼栋 -> 楼层(性别规则) -> 房间 -> 床位(置空/占用/维修/预留/封禁)。
  3. 人员管理：身份证(id_card)标识物理人，学工号(username)标识单次身份。
  4. 权限叠加：主身份排他(学生不能是后勤)，职能通过角色叠加(学生可兼任助理)。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =================================================================================
-- 1. 行政组织架构 (管理者视角)
-- =================================================================================

-- 1.1 校区
CREATE TABLE `sys_campus` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_name` varchar(100) NOT NULL COMMENT '校区名称',
  `address` varchar(255) DEFAULT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1-启用, 0-停用',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区表';

-- 1.2 院系/学院
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint NOT NULL,
  `dept_name` varchar(100) NOT NULL COMMENT '院系名称',
  `dept_type` tinyint DEFAULT '0' COMMENT '0-教学, 1-行政, 2-后勤',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院系部门表';

-- 1.3 专业
CREATE TABLE `sys_major` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_id` bigint NOT NULL,
  `major_name` varchar(100) NOT NULL,
  `duration` int DEFAULT '4' COMMENT '学制',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

-- 1.4 班级
CREATE TABLE `biz_class` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `major_id` bigint NOT NULL,
  `grade` varchar(10) NOT NULL COMMENT '入学年份',
  `class_name` varchar(100) NOT NULL,
  `counselor_id` bigint DEFAULT NULL COMMENT '关联辅导员(sys_user.id)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';


-- =================================================================================
-- 2. 用户与权限 (身份唯一原则 + 物理人追踪)
-- =================================================================================

-- 2.1 用户核心表
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) NOT NULL COMMENT '登录账号(学号/工号)，唯一标识单次入校经历',
  `id_card` varchar(20) NOT NULL COMMENT '身份证号，物理人唯一标识',
  `password` varchar(100) NOT NULL DEFAULT 'e10adc3949ba59abbe56e057f20f883e',
  `real_name` varchar(50) NOT NULL,
  `user_type` tinyint NOT NULL COMMENT '主身份：0-学生, 1-正式职员, 2-后勤人员',
  `status` tinyint DEFAULT '1' COMMENT '1-当前有效, 0-历史归档(毕业/离职)',
  `sex` tinyint DEFAULT '1' COMMENT '1-男, 2-女',
  `phone` varchar(20) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  -- 你的建议：身份证 + 账号 联合唯一。这确保了一个物理人在同一个时期只能有一个账号。
  UNIQUE KEY `uk_person_account` (`id_card`, `username`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础表';

-- 2.2 角色权限
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL,
  `role_key` varchar(50) NOT NULL COMMENT '标识: admin, student, counselor, repairman',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限叠加表';

CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT '0',
  `menu_name` varchar(50) NOT NULL,
  `perms` varchar(100) DEFAULT NULL COMMENT '权限码',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';


-- =================================================================================
-- 3. 身份详细档案 (学生、职员、后勤)
-- =================================================================================

-- 3.1 学生扩展
CREATE TABLE `stu_student` (
  `id` bigint NOT NULL COMMENT '关联sys_user.id',
  `class_id` bigint DEFAULT NULL,
  `entry_year` varchar(10) NOT NULL,
  `graduation_date` date DEFAULT NULL COMMENT '预计毕业日期',
  `academic_status` tinyint DEFAULT '0' COMMENT '0-在读, 1-休学, 2-退学, 3-毕业',
  `current_bed_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生档案';

-- 3.2 职员与后勤扩展
CREATE TABLE `biz_staff_info` (
  `id` bigint NOT NULL COMMENT '关联sys_user.id',
  `dept_id` bigint DEFAULT NULL,
  `category` tinyint NOT NULL COMMENT '1-正式职工, 2-后勤/外包',
  `title` varchar(50) DEFAULT NULL COMMENT '职称',
  `hire_date` date NOT NULL COMMENT '入职日期',
  `resign_date` date DEFAULT NULL,
  `staff_status` tinyint DEFAULT '0' COMMENT '0-在职, 1-离职, 2-退休',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职员后勤档案';


-- =================================================================================
-- 4. 宿舍物理资源 (支持混住逻辑)
-- =================================================================================

-- 4.1 楼栋
CREATE TABLE `dorm_building` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campus_id` bigint NOT NULL,
  `building_name` varchar(50) NOT NULL,
  `total_floors` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼栋表';

-- 4.2 楼层 (实现你的混住逻辑)
CREATE TABLE `dorm_floor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `building_id` bigint NOT NULL,
  `floor_num` int NOT NULL,
  `gender_type` tinyint DEFAULT '0' COMMENT '0-混住, 1-男层, 2-女层',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_b_f` (`building_id`, `floor_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='楼层表';

-- 4.3 房间
CREATE TABLE `dorm_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `floor_id` bigint NOT NULL,
  `room_no` varchar(20) NOT NULL,
  `room_purpose` char(2) DEFAULT '00' COMMENT '00-学生, 01-宿管, 02-物资',
  `room_status` tinyint DEFAULT '1' COMMENT '1-可用, 0-封禁',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间表';

-- 4.4 床位 (精细化状态)
CREATE TABLE `dorm_bed` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `bed_no` varchar(10) NOT NULL,
  `bed_status` tinyint DEFAULT '0' COMMENT '0-无人置空, 1-占用, 2-维修, 3-预留, 4-封禁',
  `occupant_id` bigint DEFAULT NULL COMMENT '关联sys_user.id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='床位表';


-- =================================================================================
-- 5. 核心业务表 (资产、水电、报修、请假、偏好)
-- =================================================================================

-- 房间资产
CREATE TABLE `dorm_room_asset` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `asset_name` varchar(100) NOT NULL,
  `status` tinyint DEFAULT '1' COMMENT '1-正常, 0-损坏',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产表';

-- 水电账单
CREATE TABLE `biz_billing_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL,
  `type` tinyint NOT NULL COMMENT '1-电, 2-水',
  `amount` decimal(10,2) NOT NULL,
  `usage_val` decimal(10,2) NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-未缴, 1-已缴',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='水电费记录';

-- 报修工单
CREATE TABLE `biz_repair_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `applicant_id` bigint NOT NULL COMMENT '申请人sys_user.id',
  `room_id` bigint NOT NULL,
  `description` text NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-待处理, 1-维修中, 2-完成',
  `handler_id` bigint DEFAULT NULL COMMENT '维修工工号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修表';

-- 住宿偏好 (用于智能分配)
CREATE TABLE `user_preference` (
  `user_id` bigint NOT NULL,
  `sleep_habits` tinyint DEFAULT '0' COMMENT '0-早睡, 1-晚睡',
  `is_snoring` tinyint DEFAULT '0' COMMENT '0-否, 1-是',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='偏好表';

-- 学生请假
CREATE TABLE `stu_leave_status` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0-请假中, 1-销假',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假离校表';


-- =================================================================================
-- 6. 测试数据修补与权限分配
-- =================================================================================

-- 初始化角色
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin'), (2, '学生', 'student'), (3, '辅导员', 'counselor'), (4, '后勤人员', 'logistics'), (5, '维修工', 'repairman');

-- 初始化行政数据
INSERT INTO `sys_campus` (id, campus_name) VALUES (1, '南校区'), (2, '北校区');
INSERT INTO `sys_department` (id, campus_id, dept_name, dept_type) VALUES (1, 1, '信息工程学院', 0), (2, 1, '后勤保卫处', 2);
INSERT INTO `sys_major` (id, dept_id, major_name) VALUES (1, 1, '计算机科学与技术');
INSERT INTO `biz_class` (id, major_id, grade, class_name) VALUES (1, 1, '2021', '计科21级1班');

-- 物理人测试：张三。他既有离校的本科账号，又有在读的研究生账号。
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status, sex) 
VALUES (2, 'S2017001', '110101199901018888', '张三', 0, 0, 1); -- 历史归档数据
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status, sex) 
VALUES (3, 'S2024999', '110101199901018888', '张三', 0, 1, 1); -- 当前研究生数据

-- 身份叠加测试：李华(学生)，兼任辅导员助手
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status, sex) 
VALUES (4, 'S2021001', '440101200305051234', '李华', 0, 1, 1);

-- 职员：王老师
INSERT INTO `sys_user` (id, username, id_card, real_name, user_type, status, sex) 
VALUES (5, 'T1001', '440101198501017777', '王辅导', 1, 1, 2);

-- 权限叠加关联
INSERT INTO `sys_user_role` VALUES (3, 2); -- 张三 -> 学生权限
INSERT INTO `sys_user_role` VALUES (4, 2), (4, 3); -- 李华 -> 学生权限 + 辅导员权限 (身份叠加体现)
INSERT INTO `sys_user_role` VALUES (5, 3); -- 王老师 -> 辅导员权限

-- 档案录入
INSERT INTO `stu_student` (id, class_id, entry_year, academic_status) VALUES (2, 1, '2017', 3), (3, 1, '2024', 0), (4, 1, '2021', 0);
INSERT INTO `biz_staff_info` (id, dept_id, category, hire_date) VALUES (5, 1, 1, '2015-08-01');

-- 物理环境录入：1栋 1层男，2层女
INSERT INTO `dorm_building` (id, campus_id, building_name, total_floors) VALUES (1, 1, '学生公寓1栋', 6);
INSERT INTO `dorm_floor` (id, building_id, floor_num, gender_type) VALUES (1, 1, 1, 1), (2, 1, 2, 2);
INSERT INTO `dorm_room` (id, floor_id, room_no) VALUES (1, 1, '101'), (2, 2, '201');
INSERT INTO `dorm_bed` (room_id, bed_no, bed_status) VALUES (1, '1号', 0), (1, '2号', 2), (1, '3号', 4); -- 置空, 维修, 封禁

SET FOREIGN_KEY_CHECKS = 1;
