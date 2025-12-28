/*
SQLyog Community v13.3.1 (64 bit)
MySQL - 8.4.6 : Database - dorm_manager
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`dorm_manager` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `dorm_manager`;

/*Table structure for table `biz_billing_rate` */

DROP TABLE IF EXISTS `biz_billing_rate`;

CREATE TABLE `biz_billing_rate` (
  `rate_id` bigint NOT NULL AUTO_INCREMENT COMMENT '费率ID',
  `rate_name` varchar(100) NOT NULL COMMENT '费率名称 (如：学生电费-阶梯1)',
  `meter_type` char(1) NOT NULL COMMENT '计量类型 (1: 电, 2: 水)',
  `unit_price` decimal(6,3) NOT NULL COMMENT '单价/基础费率',
  `threshold` decimal(10,3) DEFAULT NULL COMMENT '阶梯阈值 (例如，水费超过此值应用下一阶梯)',
  `valid_from` date NOT NULL COMMENT '生效日期',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL,
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`rate_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='计费费率配置表';

/*Data for the table `biz_billing_rate` */

insert  into `biz_billing_rate`(`rate_id`,`rate_name`,`meter_type`,`unit_price`,`threshold`,`valid_from`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(100,'基础电费-学生','1',6.550,NULL,'2025-01-01','SYSTEM','2025-10-28 17:37:52','SYSTEM','2025-10-28 17:37:52');

/*Table structure for table `biz_billing_record` */

DROP TABLE IF EXISTS `biz_billing_record`;

CREATE TABLE `biz_billing_record` (
  `record_id` bigint NOT NULL AUTO_INCREMENT COMMENT '账单ID',
  `room_id` bigint NOT NULL COMMENT '关联房间ID',
  `meter_type` char(1) NOT NULL COMMENT '计量类型 (1: 电, 2: 水)',
  `cycle_start_date` date NOT NULL COMMENT '计费周期开始日期',
  `cycle_end_date` date NOT NULL COMMENT '计费周期结束日期',
  `unit_consumed` decimal(10,3) NOT NULL COMMENT '本周期消耗量 (度/吨)',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额 (元)',
  `is_paid` tinyint DEFAULT '0' COMMENT '是否已支付 (0: 否, 1: 是)',
  `paid_time` datetime DEFAULT NULL COMMENT '支付时间',
  `status` char(1) DEFAULT '0' COMMENT '账单状态 (0: 未出账, 1: 已出账, 2: 已支付, 3: 逾期)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL,
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`record_id`) USING BTREE,
  UNIQUE KEY `uk_room_type_cycle` (`room_id`,`meter_type`,`cycle_start_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='水电费账单记录表';

/*Data for the table `biz_billing_record` */

/*Table structure for table `biz_class` */

DROP TABLE IF EXISTS `biz_class`;

CREATE TABLE `biz_class` (
  `class_id` bigint NOT NULL AUTO_INCREMENT COMMENT '班级ID',
  `class_name` varchar(100) NOT NULL COMMENT '班级名称 (如：计算机科学23-1班)',
  `department_id` bigint NOT NULL COMMENT '所属院系ID (FK: sys_department)',
  `major_name` varchar(100) NOT NULL COMMENT '专业名称',
  `counselor_user_id` bigint DEFAULT NULL COMMENT '辅导员用户ID (FK: sys_user)',
  `enrollment_year` varchar(10) NOT NULL COMMENT '入学年级 (例如: 2023)',
  `student_count` int DEFAULT '0' COMMENT '班级人数 (冗余字段，可选)',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `create_time` datetime NOT NULL,
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`class_id`) USING BTREE,
  KEY `idx_department_id` (`department_id`),
  KEY `idx_counselor_id` (`counselor_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='班级信息表';

/*Data for the table `biz_class` */

insert  into `biz_class`(`class_id`,`class_name`,`department_id`,`major_name`,`counselor_user_id`,`enrollment_year`,`student_count`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1,'计科 23-1班',101,'计算机科学与技术',10006,'2023',0,'SYSTEM','2025-10-26 12:42:10',NULL,NULL),
(2,'计科 23-2班',101,'计算机科学与技术',10006,'2023',0,'SYSTEM','2025-10-26 12:42:10',NULL,NULL),
(3,'市场营销 23-1班',102,'市场营销',10006,'2023',0,'SYSTEM','2025-10-26 12:42:10',NULL,NULL),
(1001,'软工 23-1班',101,'软件工程',10006,'2023',0,'SYSTEM','2025-10-28 17:37:11','SYSTEM','2025-10-28 17:37:11');

/*Table structure for table `biz_electric_alert` */

DROP TABLE IF EXISTS `biz_electric_alert`;

CREATE TABLE `biz_electric_alert` (
  `alert_id` bigint NOT NULL AUTO_INCREMENT COMMENT '告警记录ID',
  `room_id` bigint NOT NULL COMMENT '发生告警的房间',
  `alert_type` char(1) NOT NULL COMMENT '告警类型 (1: 超功率跳闸, 2: 违规电器)',
  `measured_value` int DEFAULT NULL COMMENT '实时测得的功率值 (W)',
  `alert_time` datetime NOT NULL COMMENT '告警发生时间',
  `is_resolved` tinyint DEFAULT '0' COMMENT '是否已处理/复位 (0: 否, 1: 是)',
  PRIMARY KEY (`alert_id`) USING BTREE,
  KEY `fk_room_id` (`room_id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='违规用电告警记录表';

/*Data for the table `biz_electric_alert` */

insert  into `biz_electric_alert`(`alert_id`,`room_id`,`alert_type`,`measured_value`,`alert_time`,`is_resolved`) values 
(100,1001,'1',3000,'2025-10-29 12:23:51',0);

/*Table structure for table `biz_meter_reading` */

DROP TABLE IF EXISTS `biz_meter_reading`;

CREATE TABLE `biz_meter_reading` (
  `reading_id` bigint NOT NULL AUTO_INCREMENT COMMENT '读数记录ID',
  `meter_id` bigint NOT NULL COMMENT '关联的电表/水表资产ID',
  `meter_type` char(1) NOT NULL COMMENT '计量类型 (1: 电, 2: 水)',
  `reading_value` decimal(10,3) NOT NULL COMMENT '本次采集的累计读数',
  `reading_date` datetime NOT NULL COMMENT '读数采集时间',
  `charge_cycle_id` bigint DEFAULT NULL COMMENT '关联的计费周期ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`reading_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='水/电表读数记录表';

/*Data for the table `biz_meter_reading` */

insert  into `biz_meter_reading`(`reading_id`,`meter_id`,`meter_type`,`reading_value`,`reading_date`,`charge_cycle_id`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1000,1,'1',123.450,'2025-10-24 08:30:00',NULL,'SYSTEM','2025-10-24 21:16:05','SYSTEM','2025-10-24 21:16:05'),
(1001,1,'1',123.450,'2025-10-24 08:30:00',NULL,'SYSTEM','2025-10-24 21:57:29','SYSTEM','2025-10-24 21:57:29');

/*Table structure for table `biz_repair_order` */

DROP TABLE IF EXISTS `biz_repair_order`;

CREATE TABLE `biz_repair_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `room_id` bigint NOT NULL COMMENT '报修房间ID',
  `asset_id` bigint NOT NULL COMMENT '关联的资产ID (如空调)',
  `applicant_user_id` bigint NOT NULL COMMENT '申请人ID',
  `order_title` varchar(100) NOT NULL COMMENT '报修标题',
  `description` text NOT NULL COMMENT '损坏详情描述',
  `contact_info` varchar(50) NOT NULL COMMENT '联系电话/方式',
  `order_status` char(1) DEFAULT '0' COMMENT '工单状态 (0: 待分配, 1: 处理中, 2: 已完成, 3: 无法修复)',
  `submit_time` datetime NOT NULL COMMENT '提交时间',
  `handler_user_id` bigint DEFAULT NULL COMMENT '分配的维修人员ID',
  `finish_time` datetime DEFAULT NULL COMMENT '完工时间',
  `repair_result` varchar(255) DEFAULT NULL COMMENT '维修结果总结',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`order_id`) USING BTREE,
  KEY `idx_room_id` (`room_id`),
  KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='宿舍报修工单表';

/*Data for the table `biz_repair_order` */

/*Table structure for table `biz_room_change_request` */

DROP TABLE IF EXISTS `biz_room_change_request`;

CREATE TABLE `biz_room_change_request` (
  `request_id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `student_id` bigint NOT NULL COMMENT '申请学生ID (FK: stu_student)',
  `current_bed_id` bigint NOT NULL COMMENT '当前床位ID (FK: dorm_bed)',
  `target_bed_id` bigint DEFAULT NULL COMMENT '目标床位ID (可选, 学生指定)',
  `reason` varchar(500) NOT NULL COMMENT '申请原因',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态 (0: 待审核, 1: 辅导员批准, 2: 驳回, 3: 已执行)',
  `approval_by` bigint DEFAULT NULL COMMENT '审批人用户ID (FK: sys_user)',
  `approval_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approval_opinion` varchar(255) DEFAULT NULL COMMENT '审批意见/备注',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `create_time` datetime NOT NULL,
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`request_id`) USING BTREE,
  KEY `idx_student_id` (`student_id`),
  KEY `idx_current_bed_id` (`current_bed_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学生调宿申请表';

/*Data for the table `biz_room_change_request` */

insert  into `biz_room_change_request`(`request_id`,`student_id`,`current_bed_id`,`target_bed_id`,`reason`,`status`,`approval_by`,`approval_time`,`approval_opinion`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1001,20002,10005,10006,'测试调宿申请','0',NULL,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL);

/*Table structure for table `biz_staff_allocation_log` */

DROP TABLE IF EXISTS `biz_staff_allocation_log`;

CREATE TABLE `biz_staff_allocation_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '教职工用户ID (FK: sys_user)',
  `bed_id` bigint NOT NULL COMMENT '床位ID (FK: dorm_bed)',
  `action_type` char(1) NOT NULL COMMENT '动作类型 (0: 入住/分配, 1: 调宿, 2: 迁出/离职)',
  `reason` varchar(255) DEFAULT NULL COMMENT '原因 (如: 管理员手动分配, 离职)',
  `start_time` datetime NOT NULL COMMENT '生效时间',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID (Admin/人事)',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `create_time` datetime NOT NULL,
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`log_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`),
  KEY `idx_bed_id` (`bed_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='教职工/后勤分配日志表';

/*Data for the table `biz_staff_allocation_log` */

/*Table structure for table `biz_staff_info` */

DROP TABLE IF EXISTS `biz_staff_info`;

CREATE TABLE `biz_staff_info` (
  `user_id` bigint NOT NULL COMMENT '用户ID (PK, FK: sys_user)',
  `department_id` bigint DEFAULT NULL COMMENT '所属部门ID (FK: sys_department)',
  `job_title` varchar(50) DEFAULT NULL COMMENT '职位名称',
  `hire_date` date NOT NULL COMMENT '入职日期',
  `title_name` varchar(50) DEFAULT NULL COMMENT '职称名称 (如：教授, 处长)',
  `contract_type` varchar(50) DEFAULT NULL COMMENT '合同/任期类型',
  `contract_duration_years` int DEFAULT NULL COMMENT '合同年限（年）',
  `is_on_campus_resident` tinyint DEFAULT '0' COMMENT '住宿意愿 (0: 校外, 1: 校内)',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前床位ID (FK: dorm_bed)',
  `allocation_type` char(1) DEFAULT '1' COMMENT '分配类型 (1: 长期, 2: 短期)',
  `expected_leave_date` date DEFAULT NULL COMMENT '预计离职/搬离日期',
  `create_time` datetime NOT NULL,
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  KEY `idx_department_id` (`department_id`),
  KEY `fk_current_bed_id` (`current_bed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='教职工/后勤住宿信息表';

/*Data for the table `biz_staff_info` */

insert  into `biz_staff_info`(`user_id`,`department_id`,`job_title`,`hire_date`,`title_name`,`contract_type`,`contract_duration_years`,`is_on_campus_resident`,`current_bed_id`,`allocation_type`,`expected_leave_date`,`create_time`,`create_by`,`update_by`,`update_time`) values 
(10006,101,'辅导员','2020-08-01','讲师','辅导员-3年',3,1,NULL,'1',NULL,'2025-10-20 10:34:53','SYSTEM','SYSTEM','2025-10-25 06:33:11'),
(10007,201,'后勤人员','2018-05-15','职员','后勤长期合同',5,1,NULL,'1',NULL,'2025-10-20 10:34:53','SYSTEM','SYSTEM','2025-10-25 06:33:11'),
(10009,101,'导师/教授','2015-02-01','教授','教授任期',5,1,NULL,'1','2020-02-01','2025-10-20 10:34:53','SYSTEM','SYSTEM','2025-10-25 06:33:11'),
(10010,201,'维修人员','2022-09-01','技工',NULL,NULL,0,NULL,'1',NULL,'2025-10-20 10:34:53','SYSTEM','SYSTEM','2025-10-25 06:33:11'),
(10011,301,'财务人员','2021-03-01','会计',NULL,NULL,0,NULL,'1',NULL,'2025-10-20 10:34:53','SYSTEM','SYSTEM','2025-10-25 06:33:11');

/*Table structure for table `biz_user_preference` */

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
  `guest_frequency` char(1) DEFAULT '0' COMMENT '访客频率 (0: 几乎没有, 1: 偶尔, 2: 经常)',
  `study_location_pref` char(1) DEFAULT '0' COMMENT '学习地点偏好 (0: 图书馆/自习室, 1: 宿舍)',
  `in_room_noise_level` char(1) DEFAULT '0' COMMENT '室内噪音水平 (0: 安静, 1: 偶尔, 2: 经常)',
  `smell_sensitivity` char(1) DEFAULT '0' COMMENT '气味敏感度 (0: 不敏感, 1: 一般, 2: 非常敏感)',
  `hobby_tags` varchar(500) DEFAULT NULL COMMENT '爱好标签 (逗号分隔)',
  `group_code` varchar(50) DEFAULT NULL COMMENT '组队分配码',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `create_time` datetime NOT NULL,
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='通用用户住宿偏好表';

/*Data for the table `biz_user_preference` */

insert  into `biz_user_preference`(`user_id`,`is_smoker`,`is_drinker`,`wake_type`,`sleep_type`,`is_light_sleeper`,`study_at_night`,`mobile_game_freq`,`board_game_interest`,`cleanliness_level`,`air_condition_pref`,`noise_tolerance`,`guest_frequency`,`study_location_pref`,`in_room_noise_level`,`smell_sensitivity`,`hobby_tags`,`group_code`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(10004,0,0,'0','0',0,0,'0',0,'2','0','2','0','1','0','0','编程, 篮球, 早睡早起',NULL,'SYSTEM','2025-10-25 07:40:41',NULL,NULL),
(10005,0,1,'2','2',1,1,'2',1,'0','1','0','1','0','2','0','追剧, 游戏, 晚睡',NULL,'SYSTEM','2025-10-25 07:40:41',NULL,NULL),
(10006,0,0,'0','0',0,1,'0',0,'2','0','1','0','1','0','1','阅读, 教学',NULL,'SYSTEM','2025-10-25 07:40:41',NULL,NULL),
(10010,1,1,'1','2',0,0,'0',0,'1','1','2','1','0','1','0','维修, 户外',NULL,'SYSTEM','2025-10-25 07:40:41',NULL,NULL);

/*Table structure for table `dorm_allocation_log` */

DROP TABLE IF EXISTS `dorm_allocation_log`;

CREATE TABLE `dorm_allocation_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` bigint NOT NULL COMMENT '学生ID (FK)',
  `bed_id` bigint NOT NULL COMMENT '床位ID (FK)',
  `action_type` char(1) NOT NULL COMMENT '动作类型 (0: 入住, 1: 调宿, 2: 离校/退宿)',
  `reason_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变动原因 (如：转专业, 校外申请, 新生分配)',
  `flow_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '2' COMMENT '流程状态 (0: 待审, 1: 已批, 2: 已执行, 3: 已拒)',
  `start_time` datetime NOT NULL COMMENT '生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `target_room_id` bigint DEFAULT NULL COMMENT '目标房间ID (调宿申请用)',
  `is_active` tinyint DEFAULT '1' COMMENT '当前是否处于此记录状态 (1: 是, 0: 否)',
  `operator` varchar(64) DEFAULT 'SYSTEM' COMMENT '操作员/系统',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_bed_id` (`bed_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10014 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='宿舍分配/调动记录表';

/*Data for the table `dorm_allocation_log` */

insert  into `dorm_allocation_log`(`log_id`,`student_id`,`bed_id`,`action_type`,`reason_type`,`flow_status`,`start_time`,`end_time`,`target_room_id`,`is_active`,`operator`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1,20001,10001,'0','新生分配','2','2025-10-15 14:25:25',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-15 14:25:25','SYSTEM','2025-10-28 12:56:51'),
(10000,20001,10001,'1','休学','2','2025-10-28 20:57:08',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 20:57:08','SYSTEM','2025-10-28 20:57:08'),
(10001,20001,10001,'1','休学','2','2025-10-28 21:31:40',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 21:31:40','SYSTEM','2025-10-28 21:31:40'),
(10002,20001,10001,'1','休学','2','2025-10-28 21:34:36',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 21:34:36','SYSTEM','2025-10-28 21:34:36'),
(10003,20001,10001,'1','休学','2','2025-10-28 22:00:43',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 22:00:43','SYSTEM','2025-10-28 22:00:43'),
(10004,20001,10001,'1','休学','2','2025-10-28 22:10:51',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 22:10:51','SYSTEM','2025-10-28 22:10:51'),
(10005,20001,10001,'1','休学','2','2025-10-28 22:16:29',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 22:16:29','SYSTEM','2025-10-28 22:16:29'),
(10006,20001,10001,'1','休学','2','2025-10-28 22:21:55',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 22:21:55','SYSTEM','2025-10-28 22:21:55'),
(10007,20001,10001,'1','休学','2','2025-10-28 22:26:03',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 22:26:03','SYSTEM','2025-10-28 22:26:03'),
(10008,20001,10001,'1','学籍状态变更 (1)','2','2025-10-28 22:30:16',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 22:30:16','SYSTEM','2025-10-28 22:30:16'),
(10009,20001,10001,'1','休学','2','2025-10-28 22:37:59',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 22:37:59','SYSTEM','2025-10-28 22:37:59'),
(10010,20001,10001,'1','学籍状态变更 (1)','2','2025-10-28 23:09:08',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 23:09:08','SYSTEM','2025-10-28 23:09:08'),
(10011,20001,10001,'1','学籍状态变更 (1)','2','2025-10-28 23:50:30',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 23:50:30','SYSTEM','2025-10-28 23:50:30'),
(10012,20001,10001,'1','学籍状态变更 (1)','2','2025-10-28 23:54:23',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-28 23:54:23','SYSTEM','2025-10-28 23:54:23'),
(10013,20001,10001,'1','学籍状态变更 (1)','2','2025-10-29 00:01:04',NULL,NULL,0,'SYSTEM','SYSTEM','2025-10-29 00:01:04','SYSTEM','2025-10-29 00:01:04');

/*Table structure for table `dorm_bed` */

DROP TABLE IF EXISTS `dorm_bed`;

CREATE TABLE `dorm_bed` (
  `bed_id` bigint NOT NULL AUTO_INCREMENT COMMENT '床位ID',
  `room_id` bigint NOT NULL COMMENT '所属房间ID (FK: dorm_room)',
  `bed_number` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '床位编号 (如：A, B)',
  `is_occupied` tinyint DEFAULT '0' COMMENT '是否被占用 (0: 空闲, 1: 已占用)',
  `occupant_user_id` bigint DEFAULT NULL COMMENT '当前入住的学生ID (FK: stu_student)',
  `occupant_type` char(1) DEFAULT NULL COMMENT '入住人类型 (1: 学生, 2: 教职工)',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`bed_id`) USING BTREE,
  UNIQUE KEY `uk_room_bed` (`room_id`,`bed_number`) USING BTREE,
  UNIQUE KEY `uk_student_occupancy` (`occupant_user_id`) USING BTREE,
  KEY `idx_room_id` (`room_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='床位信息表';

/*Data for the table `dorm_bed` */

insert  into `dorm_bed`(`bed_id`,`room_id`,`bed_number`,`is_occupied`,`occupant_user_id`,`occupant_type`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(10001,1001,'A',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(10002,1001,'B',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(10003,1001,'C',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(10004,1001,'D',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(10005,1002,'A',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(10006,1002,'B',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(10007,1002,'C',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(10008,1002,'D',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(11001,2001,'A',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(11002,2001,'B',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(11003,2001,'C',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(11004,2001,'D',0,NULL,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL);

/*Table structure for table `dorm_building` */

DROP TABLE IF EXISTS `dorm_building`;

CREATE TABLE `dorm_building` (
  `building_id` bigint NOT NULL AUTO_INCREMENT COMMENT '楼栋ID',
  `building_name` varchar(50) NOT NULL COMMENT '楼栋名称 (如：A1栋)',
  `campus_id` bigint NOT NULL COMMENT '所属校区ID (FK: sys_campus)',
  `total_floors` int NOT NULL COMMENT '总楼层数',
  `gender_type` char(1) NOT NULL COMMENT '性别限制 (0: 男, 1: 女, 2: 混合)',
  `purpose_type` char(1) DEFAULT '0' COMMENT '用途类型 (0: 学生寝室, 1: 教师/职工宿舍)',
  `manager_id` bigint DEFAULT NULL COMMENT '分配的宿管用户ID (FK: sys_user)',
  `status` tinyint DEFAULT '0' COMMENT '楼栋状态 (0: 正常, 1: 维修/关闭)',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`building_id`),
  UNIQUE KEY `building_name` (`building_name`),
  KEY `idx_manager_id` (`manager_id`),
  KEY `idx_campus_id` (`campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='楼栋信息表';

/*Data for the table `dorm_building` */

insert  into `dorm_building`(`building_id`,`building_name`,`campus_id`,`total_floors`,`gender_type`,`purpose_type`,`manager_id`,`status`,`create_by`,`update_by`,`create_time`,`update_time`) values 
(1,'A1栋',1,5,'0','0',10003,0,'SYSTEM','SYSTEM','2025-10-15 14:25:25','2025-10-23 11:52:00'),
(2,'B2栋',1,4,'1','0',NULL,0,'SYSTEM','SYSTEM','2025-10-15 14:25:25','2025-10-23 11:52:00'),
(3,'C3栋',1,3,'2','1',10007,0,'SYSTEM','SYSTEM','2025-10-15 14:25:25','2025-10-23 11:52:00'),
(100,'D4栋',1,6,'0','0',10003,0,'SYSTEM','SYSTEM','2025-10-23 19:20:36','2025-10-23 19:20:36');

/*Table structure for table `dorm_electric_rule` */

DROP TABLE IF EXISTS `dorm_electric_rule`;

CREATE TABLE `dorm_electric_rule` (
  `rule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `room_id` bigint DEFAULT NULL COMMENT '房间ID',
  `building_id` bigint DEFAULT NULL COMMENT '楼栋ID',
  `ac_power_limit` int DEFAULT '2500' COMMENT '空调电路功率限制 (W)',
  `general_power_limit` int DEFAULT '800' COMMENT '普通电路功率限制 (W)',
  `start_date` date NOT NULL COMMENT '规则生效日期',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='宿舍用电规则表';

/*Data for the table `dorm_electric_rule` */

insert  into `dorm_electric_rule`(`rule_id`,`room_id`,`building_id`,`ac_power_limit`,`general_power_limit`,`start_date`) values 
(10,NULL,1,2000,1000,'2025-01-01');

/*Table structure for table `dorm_floor` */

DROP TABLE IF EXISTS `dorm_floor`;

CREATE TABLE `dorm_floor` (
  `floor_id` bigint NOT NULL AUTO_INCREMENT COMMENT '楼层ID',
  `building_id` bigint NOT NULL COMMENT '所属楼栋ID (FK: dorm_building)',
  `floor_number` int NOT NULL COMMENT '楼层编号 (物理层数，如 1, 2, 3)',
  `floor_name` varchar(50) DEFAULT NULL COMMENT '楼层显示名称 (如: 一层, G层)',
  `room_count` int DEFAULT '0' COMMENT '该楼层房间数 (冗余)',
  `manager_id` bigint DEFAULT NULL COMMENT '该楼层负责人ID (精细化管理, 可选)',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `create_time` datetime NOT NULL,
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`floor_id`) USING BTREE,
  UNIQUE KEY `uk_building_floor_number` (`building_id`,`floor_number`)
) ENGINE=InnoDB AUTO_INCREMENT=205 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='楼栋的楼层表';

/*Data for the table `dorm_floor` */

insert  into `dorm_floor`(`floor_id`,`building_id`,`floor_number`,`floor_name`,`room_count`,`manager_id`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(101,1,1,'A1栋-一层',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(102,1,2,'A1栋-二层',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(103,1,3,'A1栋-三层',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(104,1,4,'A1栋-四层',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(105,1,5,'A1栋-五层',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(201,2,1,'B2栋-一层',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(202,2,2,'B2栋-二层',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(203,2,3,'B2栋-三层',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(204,2,4,'B2栋-四层',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL);

/*Table structure for table `dorm_floor_gender_rule` */

DROP TABLE IF EXISTS `dorm_floor_gender_rule`;

CREATE TABLE `dorm_floor_gender_rule` (
  `rule_id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `floor_id` bigint NOT NULL COMMENT '所属楼层ID (FK: dorm_floor)',
  `gender_type` char(1) NOT NULL COMMENT '该楼层的性别限制 (0: 男, 1: 女)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`rule_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='混合楼栋楼层性别规则表';

/*Data for the table `dorm_floor_gender_rule` */

/*Table structure for table `dorm_meter_electric` */

DROP TABLE IF EXISTS `dorm_meter_electric`;

CREATE TABLE `dorm_meter_electric` (
  `meter_id` bigint NOT NULL AUTO_INCREMENT COMMENT '电表ID',
  `room_id` bigint NOT NULL COMMENT '关联房间ID (FK: dorm_room)',
  `meter_code` varchar(50) NOT NULL COMMENT '电表编号/资产编号',
  `model` varchar(50) DEFAULT NULL COMMENT '型号规格',
  `installation_date` date DEFAULT NULL COMMENT '安装日期',
  `status` char(1) DEFAULT '0' COMMENT '状态 (0: 正常, 1: 故障, 2: 停用)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`meter_id`) USING BTREE,
  UNIQUE KEY `room_id` (`room_id`),
  UNIQUE KEY `meter_code` (`meter_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='房间电表资产表';

/*Data for the table `dorm_meter_electric` */

/*Table structure for table `dorm_meter_water` */

DROP TABLE IF EXISTS `dorm_meter_water`;

CREATE TABLE `dorm_meter_water` (
  `meter_id` bigint NOT NULL AUTO_INCREMENT COMMENT '水表ID',
  `room_id` bigint NOT NULL COMMENT '关联房间ID (FK: dorm_room)',
  `meter_code` varchar(50) NOT NULL COMMENT '水表编号/资产编号',
  `model` varchar(50) DEFAULT NULL COMMENT '型号规格',
  `installation_date` date DEFAULT NULL COMMENT '安装日期',
  `status` char(1) DEFAULT '0' COMMENT '状态 (0: 正常, 1: 故障, 2: 停用)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`meter_id`) USING BTREE,
  UNIQUE KEY `room_id` (`room_id`),
  UNIQUE KEY `meter_code` (`meter_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='房间水表资产表';

/*Data for the table `dorm_meter_water` */

/*Table structure for table `dorm_room` */

DROP TABLE IF EXISTS `dorm_room`;

CREATE TABLE `dorm_room` (
  `room_id` bigint NOT NULL AUTO_INCREMENT COMMENT '房间ID',
  `floor_id` bigint NOT NULL COMMENT '所属楼层ID (FK: dorm_floor)',
  `room_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '房间门牌号 (如：101)',
  `room_purpose_type` char(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '00' COMMENT '房间用途 (00:学生宿舍, 01:宿管用房, 02:物资房, 03:教职工用房, 04:单人间, 05:预留房)',
  `room_capacity` int NOT NULL COMMENT '房间的固定物理容量',
  `room_status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '房间状态 (0: 正常, 1: 待维修/暂停使用, 2: 重新装修/长期禁用)',
  `occupied_beds` int DEFAULT '0' COMMENT '已入住床位数',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '房间备注',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`room_id`) USING BTREE,
  UNIQUE KEY `uk_floor_room` (`floor_id`,`room_number`)
) ENGINE=InnoDB AUTO_INCREMENT=2002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='宿舍房间表';

/*Data for the table `dorm_room` */

insert  into `dorm_room`(`room_id`,`floor_id`,`room_number`,`room_purpose_type`,`room_capacity`,`room_status`,`occupied_beds`,`remark`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(1001,101,'101','00',4,'0',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(1002,101,'102','01',4,'0',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL),
(2001,201,'101','00',4,'0',0,NULL,'SYSTEM','2025-10-29 12:23:51',NULL,NULL);

/*Table structure for table `dorm_room_asset` */

DROP TABLE IF EXISTS `dorm_room_asset`;

CREATE TABLE `dorm_room_asset` (
  `asset_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资产ID',
  `room_id` bigint NOT NULL COMMENT '所属房间ID',
  `asset_name` varchar(100) NOT NULL COMMENT '资产名称 (如：空调A, 衣柜1)',
  `asset_type` char(2) NOT NULL COMMENT '资产类型 (KT-空调, YG-衣柜, WS-卫生)',
  `serial_number` varchar(100) DEFAULT NULL COMMENT '资产序列号',
  `status` char(1) DEFAULT '0' COMMENT '资产状态 (0: 正常, 1: 损坏/待修, 2: 报废)',
  `purchase_date` date DEFAULT NULL COMMENT '采购日期',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`asset_id`) USING BTREE,
  KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='房间固定资产表';

/*Data for the table `dorm_room_asset` */

/*Table structure for table `staff_employee` */

DROP TABLE IF EXISTS `staff_employee`;

CREATE TABLE `staff_employee` (
  `employee_id` bigint NOT NULL AUTO_INCREMENT COMMENT '教职工ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID (FK: sys_user)',
  `department` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '所属部门',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '职位 (如：辅导员、后勤经理)',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前床位ID (如果住宿)',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`employee_id`) USING BTREE,
  UNIQUE KEY `user_id` (`user_id`) USING BTREE,
  KEY `idx_bed_id` (`current_bed_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=30003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='教职工详细信息表';

/*Data for the table `staff_employee` */

insert  into `staff_employee`(`employee_id`,`user_id`,`department`,`position`,`current_bed_id`,`create_time`) values 
(30001,10006,'学生工作处','辅导员',NULL,'2025-10-16 14:17:42'),
(30002,10007,'后勤管理部','宿管员',10008,'2025-10-16 14:17:42');

/*Table structure for table `stu_leave_status` */

DROP TABLE IF EXISTS `stu_leave_status`;

CREATE TABLE `stu_leave_status` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `student_id` bigint NOT NULL COMMENT '学生ID (PK, FK)',
  `status_type` char(1) NOT NULL COMMENT '当前状态 (0: 在校, 1: 假期离校, 2: 毕业离校, 3: 寒暑假留校, 4: 请假离校)',
  `start_date` datetime NOT NULL COMMENT '状态开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '状态预计结束时间',
  `is_in_dorm` tinyint DEFAULT '1' COMMENT '是否在寝室 (1: 是, 0: 否)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `student_id` (`student_id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生离校/留校状态表';

/*Data for the table `stu_leave_status` */

insert  into `stu_leave_status`(`id`,`student_id`,`status_type`,`start_date`,`end_date`,`is_in_dorm`,`remark`) values 
(1,20001,'0','2025-10-15 14:25:25',NULL,1,NULL),
(2,20002,'0','2025-10-15 14:25:25',NULL,1,NULL);

/*Table structure for table `stu_student` */

DROP TABLE IF EXISTS `stu_student`;

CREATE TABLE `stu_student` (
  `student_id` bigint NOT NULL AUTO_INCREMENT COMMENT '学生ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID (FK: sys_user)',
  `enrollment_year` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '入学年级 (例如: 23级)',
  `education_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学历层次 (如：本科, 研究生)',
  `standard_duration` int NOT NULL COMMENT '标准学制年限 (如：4)',
  `current_grade_level` int NOT NULL COMMENT '当前年级级别 (1, 2, 3...)',
  `years_held_back` int DEFAULT '0' COMMENT '累计留级年数',
  `class_id` bigint DEFAULT NULL COMMENT '所属班级ID (FK: biz_class)',
  `supervisor_user_id` bigint DEFAULT NULL COMMENT '导师用户ID (FK: sys_user)',
  `current_campus_id` bigint NOT NULL COMMENT '当前所在校区ID (FK: sys_campus)',
  `enter_date` date NOT NULL COMMENT '入学日期',
  `graduation_date` date DEFAULT NULL COMMENT '预计毕业日期',
  `academic_status` char(1) NOT NULL DEFAULT '0' COMMENT '学籍状态 (0: 正常在校, 1: 休学, 2: 毕业, 3: 退学)',
  `is_on_campus_resident` tinyint DEFAULT '1' COMMENT '当前住宿意愿/状态 (0: 校外, 1: 校内)',
  `current_bed_id` bigint DEFAULT NULL COMMENT '当前床位ID (FK: dorm_bed)',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`student_id`),
  UNIQUE KEY `user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_bed_id` (`current_bed_id`),
  KEY `idx_class_id` (`class_id`),
  KEY `idx_supervisor_user_id` (`supervisor_user_id`),
  KEY `idx_current_campus_id` (`current_campus_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生详细信息表';

/*Data for the table `stu_student` */

insert  into `stu_student`(`student_id`,`user_id`,`enrollment_year`,`education_level`,`standard_duration`,`current_grade_level`,`years_held_back`,`class_id`,`supervisor_user_id`,`current_campus_id`,`enter_date`,`graduation_date`,`academic_status`,`is_on_campus_resident`,`current_bed_id`,`create_by`,`create_time`,`update_by`,`update_time`) values 
(20001,10004,'2023','本科',4,2,0,1,10009,1,'2023-09-01',NULL,'0',1,10001,'SYSTEM','2025-10-26 13:46:51','SYSTEM','2025-10-28 20:57:08'),
(20002,10005,'2023','本科',4,2,0,3,NULL,1,'2023-09-01',NULL,'1',1,NULL,'SYSTEM','2025-10-26 13:46:51','SYSTEM','2025-10-26 21:48:54'),
(20003,10012,'2024','本科',4,1,0,NULL,NULL,1,'2024-09-01',NULL,'0',1,NULL,'SYSTEM','2025-10-26 13:46:51','SYSTEM','2025-10-26 13:46:51');

/*Table structure for table `sys_campus` */

DROP TABLE IF EXISTS `sys_campus`;

CREATE TABLE `sys_campus` (
  `campus_id` bigint NOT NULL AUTO_INCREMENT COMMENT '校区ID',
  `campus_name` varchar(100) NOT NULL COMMENT '校区名称',
  `campus_code` varchar(20) DEFAULT NULL COMMENT '校区编码',
  `status` tinyint DEFAULT '0' COMMENT '状态 (0: 启用, 1: 停用)',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`campus_id`) USING BTREE,
  UNIQUE KEY `campus_name` (`campus_name`),
  UNIQUE KEY `campus_code` (`campus_code`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='校区信息表';

/*Data for the table `sys_campus` */

insert  into `sys_campus`(`campus_id`,`campus_name`,`campus_code`,`status`,`create_by`,`update_by`,`create_time`,`update_time`) values 
(1,'主校区','MAIN',0,'SYSTEM','SYSTEM','2025-10-17 13:30:53','2025-10-23 11:17:00'),
(2,'北校区','NORTH',0,'SYSTEM','SYSTEM','2025-10-17 13:30:53','2025-10-23 11:17:00'),
(10,'西校区','WEST',0,'SYSTEM','SYSTEM','2025-10-28 17:33:00','2025-10-28 17:33:00');

/*Table structure for table `sys_department` */

DROP TABLE IF EXISTS `sys_department`;

CREATE TABLE `sys_department` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门/院系ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父级部门ID',
  `dept_name` varchar(100) NOT NULL COMMENT '部门/院系名称',
  `dept_code` varchar(20) DEFAULT NULL COMMENT '部门编码',
  `leader_id` bigint DEFAULT NULL COMMENT '部门负责人ID (FK: sys_user)',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE,
  UNIQUE KEY `dept_name` (`dept_name`),
  UNIQUE KEY `dept_code` (`dept_code`)
) ENGINE=InnoDB AUTO_INCREMENT=303 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='部门/院系字典表';

/*Data for the table `sys_department` */

insert  into `sys_department`(`dept_id`,`parent_id`,`dept_name`,`dept_code`,`leader_id`,`create_by`,`update_by`,`create_time`,`update_time`) values 
(101,0,'计算机学院','CS',NULL,'SYSTEM','SYSTEM','2025-10-20 10:08:04','2025-10-23 11:52:00'),
(102,0,'管理学院','MGMT',NULL,'SYSTEM','SYSTEM','2025-10-20 10:08:04','2025-10-23 11:52:00'),
(201,0,'后勤管理部','LOGI',NULL,'SYSTEM','SYSTEM','2025-10-20 10:08:04','2025-10-23 11:52:00'),
(301,0,'财务部','FIN',NULL,'SYSTEM','SYSTEM','2025-10-26 22:55:21','2025-10-26 22:55:21'),
(302,0,'库存维修部门','IMD',NULL,'SYSTEM','SYSTEM','2025-10-28 17:36:42','2025-10-28 17:36:42');

/*Table structure for table `sys_menu` */

DROP TABLE IF EXISTS `sys_menu`;

CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限/菜单ID',
  `menu_name` varchar(64) NOT NULL COMMENT '菜单名称',
  `menu_sort` int DEFAULT '0' COMMENT '菜单排序',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `menu_type` char(1) NOT NULL COMMENT '类型 (M: 目录, C: 菜单, F: 按钮/权限)',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识 (如：dorm:building:add)',
  `path` varchar(255) DEFAULT NULL COMMENT '路由地址',
  `icon` varchar(100) DEFAULT NULL COMMENT '菜单图标',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT 'SYSTEM' COMMENT '创建者',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限菜单表';

/*Data for the table `sys_menu` */

insert  into `sys_menu`(`menu_id`,`menu_name`,`menu_sort`,`parent_id`,`menu_type`,`perms`,`path`,`icon`,`remark`,`create_by`,`update_by`,`create_time`,`update_time`) values 
(1,'楼栋管理',1,0,'M',NULL,'/dorm','building',NULL,'SYSTEM','SYSTEM','2025-10-15 14:25:25','2025-10-23 11:52:00'),
(2,'学生信息',1,0,'M',NULL,'/dorm','building',NULL,'SYSTEM','SYSTEM','2025-10-15 14:25:25','2025-10-23 11:52:00'),
(3,'系统管理',1,0,'M',NULL,'/dorm','building',NULL,'SYSTEM','SYSTEM','2025-10-16 16:18:11','2025-10-23 11:52:00'),
(101,'楼栋新增',10,1,'F','dorm:building:add','/dorm/building/add','plus',NULL,'SYSTEM','SYSTEM','2025-10-15 14:25:25','2025-10-23 11:52:00'),
(102,'楼栋修改',0,1,'F','dorm:building:edit',NULL,NULL,NULL,'SYSTEM','SYSTEM','2025-10-15 14:25:25','2025-10-23 11:52:00'),
(201,'学生查询',20,2,'F','stu:student:query','/dorm/student/list','user',NULL,'SYSTEM','SYSTEM','2025-10-15 14:25:25','2025-10-23 11:52:00'),
(301,'用户查询',0,3,'F','sys:user:query',NULL,NULL,NULL,'SYSTEM','SYSTEM','2025-10-16 16:18:11','2025-10-23 11:52:00'),
(302,'用户新增',0,3,'F','sys:user:add',NULL,NULL,NULL,'SYSTEM','SYSTEM','2025-10-16 16:18:11','2025-10-23 11:52:00'),
(303,'用户修改',0,3,'F','sys:user:edit',NULL,NULL,NULL,'SYSTEM','SYSTEM','2025-10-16 16:18:11','2025-10-23 11:52:00'),
(400,'报修工单管理',4,0,'M',NULL,'/repair','wrench',NULL,'SYSTEM',NULL,'2025-10-23 16:47:00',NULL),
(403,'提交工单',1,400,'F','biz:repair:submit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-23 16:47:00',NULL),
(404,'处理/分配工单',2,400,'F','biz:repair:handle',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-23 16:47:00',NULL),
(405,'删除工单',3,400,'F','biz:repair:remove',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-23 16:47:00',NULL),
(500,'计量资产管理',5,0,'M',NULL,'/meter','meter',NULL,'SYSTEM',NULL,'2025-10-23 16:23:36',NULL),
(501,'电表查询',10,500,'C','dorm:meter:electric:query','/meter/electric','electric',NULL,'SYSTEM',NULL,'2025-10-23 16:23:36',NULL),
(502,'电表新增',11,500,'F','dorm:meter:electric:add',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-23 16:23:36',NULL),
(503,'电表修改',12,500,'F','dorm:meter:electric:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-23 16:23:36',NULL),
(504,'电表删除',13,500,'F','dorm:meter:electric:remove',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-23 16:23:36',NULL),
(510,'水表查询',20,500,'C','dorm:meter:water:query','/meter/water','water',NULL,'SYSTEM',NULL,'2025-10-23 16:23:36',NULL),
(512,'水表新增',21,500,'F','dorm:meter:water:add',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-23 16:23:36',NULL),
(513,'水表修改',22,500,'F','dorm:meter:water:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:36:43',NULL),
(514,'水表删除',23,500,'F','dorm:meter:water:remove',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:36:43',NULL),
(601,'读数查询',30,500,'F','biz:meter:reading:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 13:10:47',NULL),
(602,'读数录入',31,500,'F','biz:meter:reading:add',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 13:10:47',NULL),
(701,'查询学生状态',30,2,'F','stu:leave-status:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 14:21:34',NULL),
(702,'修改学生状态',31,2,'F','stu:leave-status:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 14:21:34',NULL),
(801,'校区查询',10,3,'F','sys:campus:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(802,'校区新增',11,3,'F','sys:campus:add',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(803,'校区修改',12,3,'F','sys:campus:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(804,'校区删除',13,3,'F','sys:campus:remove',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(805,'部门查询',20,3,'F','sys:dept:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(806,'部门新增',21,3,'F','sys:dept:add',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(807,'部门修改',22,3,'F','sys:dept:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(808,'部门删除',23,3,'F','sys:dept:remove',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(809,'用电规则查询',40,500,'F','dorm:electric-rule:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(810,'用电规则编辑',41,500,'F','dorm:electric-rule:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(811,'用电规则删除',42,500,'F','dorm:electric-rule:remove',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(812,'用电告警查询',50,500,'F','biz:electric-alert:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(813,'用电告警新增',51,500,'F','biz:electric-alert:add',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(814,'用电告警处理',52,500,'F','biz:electric-alert:resolve',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(815,'用电告警删除',53,500,'F','biz:electric-alert:remove',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(816,'修改学生信息',21,2,'F','stu:student:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(817,'查询工单',0,400,'F','biz:repair:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-24 16:41:45',NULL),
(820,'教职工信息查询',30,3,'F','staff:info:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:36:43',NULL),
(821,'教职工信息修改',31,3,'F','staff:info:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:36:43',NULL),
(825,'用户偏好查询(管理)',40,2,'F','user:preference:query:admin',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:57:39',NULL),
(826,'用户偏好修改(管理)',41,2,'F','user:preference:edit:admin',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:57:39',NULL),
(827,'用户偏好查询(本人)',42,2,'F','user:preference:query:self',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:57:39',NULL),
(828,'用户偏好修改(本人)',43,2,'F','user:preference:edit:self',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:57:39',NULL),
(830,'手动分配床位',20,1,'F','dorm:allocation:assign',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:36:43',NULL),
(831,'学生迁出床位',21,1,'F','dorm:allocation:checkout',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:36:43',NULL),
(832,'批量自动分配',22,1,'F','dorm:allocation:auto',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:36:43',NULL),
(833,'手动分配(教职工)',23,1,'F','staff:allocation:assign',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:50:47',NULL),
(834,'迁出(教职工)',24,1,'F','staff:allocation:checkout',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:50:47',NULL),
(835,'自动分配(教职工)',25,1,'F','staff:allocation:auto',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-25 06:50:47',NULL),
(840,'班级查询',30,3,'F','sys:class:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-26 12:47:48',NULL),
(841,'班级新增',31,3,'F','sys:class:add',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-26 12:47:48',NULL),
(842,'班级修改',32,3,'F','sys:class:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-26 12:47:48',NULL),
(843,'班级删除',33,3,'F','sys:class:remove',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-26 12:47:48',NULL),
(850,'提交调宿申请(本人)',30,1,'F','dorm:change:submit:self',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-29 12:10:34',NULL),
(851,'查询调宿申请(管理)',31,1,'F','dorm:change:query:admin',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-29 12:10:34',NULL),
(852,'审批调宿申请(管理)',32,1,'F','dorm:change:approve',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-29 12:10:34',NULL),
(855,'修改房间状态(封禁/解封)',25,1,'F','dorm:room:status:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-29 12:41:50',NULL),
(860,'楼层查询',26,1,'F','dorm:floor:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-29 13:43:11',NULL),
(861,'楼层新增',27,1,'F','dorm:floor:add',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-29 13:43:11',NULL),
(862,'楼层修改',28,1,'F','dorm:floor:edit',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-29 13:43:11',NULL),
(863,'楼层删除',29,1,'F','dorm:floor:remove',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-29 13:43:11',NULL),
(901,'宿舍资源浏览(公共)',40,1,'F','dorm:browse:query',NULL,NULL,NULL,'SYSTEM',NULL,'2025-10-29 17:49:05',NULL);

/*Table structure for table `sys_role` */

DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(64) NOT NULL COMMENT '角色名称 (如：超级管理员)',
  `role_key` varchar(64) NOT NULL COMMENT '权限关键字 (如：admin)',
  `role_sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '0' COMMENT '角色状态 (0: 正常, 1: 停用)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_key` (`role_key`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

/*Data for the table `sys_role` */

insert  into `sys_role`(`role_id`,`role_name`,`role_key`,`role_sort`,`status`,`create_by`,`create_time`,`update_by`,`update_time`,`remark`) values 
(1,'超级管理员','admin',1,0,'','2025-10-20 10:33:16','',NULL,NULL),
(2,'学生事务管理员','counselor',2,0,'','2025-10-20 10:33:16','',NULL,NULL),
(3,'楼栋宿管','dorm_manager',3,0,'','2025-10-20 10:33:16','',NULL,NULL),
(4,'学生用户','student',4,0,'','2025-10-20 10:33:16','',NULL,NULL),
(5,'后勤维修人员','repair_staff',5,0,'','2025-10-20 10:33:16','',NULL,NULL),
(6,'财务/水电管理员','finance_staff',6,0,'','2025-10-20 10:33:16','',NULL,NULL),
(7,'教职工/高层用户','staff_user',7,0,'','2025-10-20 10:33:16','',NULL,NULL),
(100,'测试经理','test_manager',0,0,'SYSTEM','2025-10-23 16:44:14','SYSTEM','2025-10-23 16:44:53',NULL);

/*Table structure for table `sys_role_menu` */

DROP TABLE IF EXISTS `sys_role_menu`;

CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '权限/菜单ID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色与权限关联表';

/*Data for the table `sys_role_menu` */

insert  into `sys_role_menu`(`role_id`,`menu_id`) values 
(1,1),
(1,2),
(1,3),
(1,101),
(1,102),
(1,201),
(1,301),
(1,302),
(1,303),
(1,304),
(1,403),
(1,404),
(1,405),
(1,500),
(1,501),
(1,502),
(1,503),
(1,504),
(1,510),
(1,512),
(1,513),
(1,514),
(1,601),
(1,602),
(1,701),
(1,702),
(1,801),
(1,802),
(1,803),
(1,804),
(1,805),
(1,806),
(1,807),
(1,808),
(1,809),
(1,810),
(1,811),
(1,812),
(1,813),
(1,814),
(1,815),
(1,816),
(1,817),
(1,820),
(1,821),
(1,825),
(1,826),
(1,827),
(1,828),
(1,830),
(1,831),
(1,832),
(1,833),
(1,834),
(1,835),
(1,840),
(1,841),
(1,842),
(1,843),
(1,851),
(1,852),
(1,855),
(1,860),
(1,861),
(1,862),
(1,863),
(1,901),
(2,2),
(2,201),
(2,701),
(2,702),
(2,816),
(2,817),
(2,820),
(2,821),
(2,825),
(2,826),
(2,830),
(2,831),
(2,851),
(2,852),
(2,855),
(2,901),
(3,1),
(3,2),
(3,201),
(3,701),
(3,702),
(3,817),
(3,831),
(3,855),
(3,901),
(4,403),
(4,817),
(4,827),
(4,828),
(4,850),
(4,901),
(5,401),
(5,402),
(5,814),
(5,817),
(5,901),
(6,500),
(6,501),
(6,502),
(6,503),
(6,504),
(6,510),
(6,512),
(6,513),
(6,514),
(6,601),
(6,602),
(6,817),
(6,901),
(7,817),
(7,827),
(7,828),
(7,901),
(100,101),
(100,102);

/*Table structure for table `sys_user` */

DROP TABLE IF EXISTS `sys_user`;

CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '登录账号/学号/工号',
  `password` varchar(100) NOT NULL COMMENT '密码 (统一：123456)',
  `role_id` bigint NOT NULL COMMENT '关联角色ID',
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名 (真实姓名)',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称 (显示名，可选)',
  `user_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '用户类型 (0: Admin, 1: Student, 2: DormManager)',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '性别 (0: 男, 1: 女)',
  `date_of_birth` date NOT NULL COMMENT '出生日期',
  `hometown` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '籍贯',
  `political_status` varchar(20) DEFAULT '群众' COMMENT '政治面貌 (党员, 团员, 群众等)',
  `ethnicity` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '汉族' COMMENT '民族',
  `home_address` varchar(255) DEFAULT NULL COMMENT '家庭住址',
  `phone_number` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号码',
  `email` varchar(100) DEFAULT NULL COMMENT '电子邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像URL',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '账号状态 (0: 正常, 1: 禁用)',
  `employment_status` char(1) NOT NULL DEFAULT '0' COMMENT '任职状态 (0: 在职, 1: 离职, 2: 停职)',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SYSTEM' COMMENT '创建者',
  `create_time` datetime NOT NULL,
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT NULL,
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除标志',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10013 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户基础信息表';

/*Data for the table `sys_user` */

insert  into `sys_user`(`user_id`,`username`,`password`,`role_id`,`real_name`,`nickname`,`user_type`,`sex`,`date_of_birth`,`hometown`,`political_status`,`ethnicity`,`home_address`,`phone_number`,`email`,`avatar`,`status`,`employment_status`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`) values 
(10001,'admin','$2a$10$d5GiecfeJMyeVSWWpN.uX.PQdO8U6dNXIeXiYCzpc7yemYyAdaRzW',1,'超级管理员','超级管理员','0','0','1988-08-08','北京','群众','汉族','北京市海淀区中关村大街1号','13000000001','admin@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-15 14:25:25','SYSTEM',NULL,0),
(10002,'manager01','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',2,'张筱雨','学工办张老师','0','0','1985-05-20','天津','党员','汉族','天津市南开区大学道10号','13000000002','zhang_manager@example.com','https://example.com/default-avatar.png',1,'1','SYSTEM','2025-10-15 14:25:25','SYSTEM',NULL,0),
(10003,'dorm_a1','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',3,'李晓凤','同学们好，我是宿管李阿姨','2','0','1975-11-15','河北保定','群众','汉族','学校家属区X栋Y单元Z室','13000000003','dorm_a1@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-15 14:25:25','SYSTEM',NULL,0),
(10004,'20230001','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',4,'李毅乐','快乐修狗','1','0','2004-05-15','上海','团员','回族','上海市徐汇区某某路100号','13000000004','li_student@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-15 14:25:25','SYSTEM','2025-10-28 20:57:08',0),
(10005,'20230002','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',4,'王小美','AI怎么用啊','1','1','2005-01-20','广东广州','团员','汉族','广州市天河区中山大道西','13000000005','wang_student@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-15 14:25:25','SYSTEM','2025-10-26 21:48:54',0),
(10006,'20228001','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',2,'李晓青','辅导员','0','0','1985-03-10','江苏南京','党员','汉族','南京市鼓楼区北京西路','13000000006','li_counselor@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-16 14:17:42','SYSTEM',NULL,0),
(10007,'20229001','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',3,'王武云','王后勤','2','1','1980-07-22','山东济南','群众','汉族','学校后勤宿舍区','13000000007','wang_staff@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-16 14:17:42','SYSTEM',NULL,0),
(10008,'new_manager','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',3,'王紫玲','宿管王阿姨','2','0','1982-10-01','河南郑州','群众','汉族','校外租房','13912345678','wang_dorm02@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-17 00:38:12','SYSTEM','2025-10-17 00:40:52',0),
(10009,'supervisor01','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',7,'陈军峰','逆徒太多怎么办','0','0','1970-12-05','浙江杭州','党员','汉族','学校教师公寓A栋','13998765432','chen_prof@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-20 09:07:59','SYSTEM',NULL,0),
(10010,'repair01','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',5,'王天琨','老王专业维修三十年','2','0','1978-02-18','四川成都','群众','苗族','学校后勤宿舍区B栋','13511112222','wang_repair@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-20 10:33:50',NULL,NULL,0),
(10011,'finance01','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',6,'李晓敏','李财务','0','1','1989-09-09','湖南长沙','群众','汉族','校外阳光小区','13633334444','li_finance@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-20 10:33:50',NULL,NULL,0),
(10012,'20240003','$2a$10$.qklIcEwtTHXGU0M7N4i4ewamsXS1ZuUOVJomFCXPul3vf0XoMIMm',4,'赵安利','悲伤的拨皮波','1','0','2005-08-01','江苏苏州','群众','汉族','苏州工业园','13011112222','zhao@example.com','https://example.com/default-avatar.png',0,'0','SYSTEM','2025-10-25 07:28:55',NULL,NULL,0);

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
