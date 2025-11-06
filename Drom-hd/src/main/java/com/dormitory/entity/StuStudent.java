package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List; // 确保导入 List

/**
 * 学生详细信息实体类
 * (映射 stu_student 表)
 */
@EqualsAndHashCode(callSuper = true) // 继承 BaseEntity
@Data
@TableName("stu_student")
public class StuStudent extends BaseEntity { // 继承 BaseEntity
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 学生表主键ID */
    @TableId(value = "student_id", type = IdType.AUTO)
    private Long studentId;
    
    /** 关联的用户ID (SysUser) */
    @NotNull(message = "关联用户 ID 不能为空")
    private Long userId;
    
    
    //    /** 学号 */
    //    @NotNull(message = "学号不能为空")
    //    private String stuNumber;
    // 由于 stuNumber 字段已从数据库移除，仅作为 VO 填充使用，因此必须标记为非数据库字段
    @TableField(exist = false)
    private String stuNumber;
    
    
    /** 入学年级 (例如: 2023) */
    private String enrollmentYear;
    
    /** 学历层次 (如：本科, 研究生) */
    private String educationLevel;
    
    /** 标准学制年限 (如：4) */
    private Integer standardDuration;
    
    /** 当前年级级别 (1, 2, 3...) */
    private Integer currentGradeLevel;
    
    /** 累计留级年数 */
    private Integer yearsHeldBack;
    
    // 关联班级
    /** 所属班级ID (FK: biz_class) */
    private Long classId;
    
    /** 导师用户ID (FK: sys_user) (研究生/博士生使用) */
    private Long supervisorUserId;
    
    /** 当前所在校区ID (FK: sys_campus) */
    @NotNull(message = "当前校区不能为空")
    private Long currentCampusId;
    
    /** 入学日期 */
    @NotNull(message = "入学日期不能为空")
    private LocalDate enterDate;
    
    /** 预计毕业日期 */
    private LocalDate graduationDate;
    
    /** 学籍状态 (0: 正常在校, 1: 休学, 2: 毕业, 3: 退学) */
    private String academicStatus;
    
    /** 当前住宿意愿/状态 (0: 校外, 1: 校内) */
    private Integer isOnCampusResident;
    
    /** 当前床位ID (FK: dorm_bed) */
    private Long currentBedId;
    
    // =========================================================
    // ---- 非数据库字段 (用于VO展示, @TableField(exist = false)) ----
    // =========================================================
    
    // (来自 SysUser 表)
    @TableField(exist = false)
    private String username; // 学号
    
    /** 昵称 */
    @TableField(exist = false)
    private String nickname; // 昵称
    
    /** 真实姓名 */
    @TableField(exist = false)
    private String realName; // 姓名 (真实姓名)
    
    @TableField(exist = false)
    private String sex; // 性别
    
    @TableField(exist = false)
    private String phoneNumber; // 电话
    
    @TableField(exist = false)
    private String avatar; // 头像
    
    @TableField(exist = false)
    private String email; // 电子邮箱
    
    @TableField(exist = false)
    private LocalDate dateOfBirth; // 出生日期
    
    @TableField(exist = false)
    private String hometown; // 籍贯
    
    @TableField(exist = false)
    private String politicalStatus; // 政治面貌
    
    @TableField(exist = false)
    private String ethnicity; // 民族
    
    @TableField(exist = false)
    private String homeAddress; // 家庭住址
    
    
    // (来自 BizClass 表及其关联)
    @TableField(exist = false)
    private String className; // 班级名称
    
    @TableField(exist = false)
    private String majorName; // 专业名称
    
    @TableField(exist = false)
    private String departmentName; // 院系名称
    
    @TableField(exist = false)
    private String counselorName; // 辅导员姓名
    
    
    // (来自其他关联)
    @TableField(exist = false)
    private String campusName; // 校区名称
    
    @TableField(exist = false)
    private String supervisorName; // 导师姓名
    
    @TableField(exist = false)
    private String bedInfo; // 床位信息 (楼栋-房间-床号)
}