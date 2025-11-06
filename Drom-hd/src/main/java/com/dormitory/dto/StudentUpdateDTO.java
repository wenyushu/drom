package com.dormitory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学生信息更新 DTO (由管理员操作)
 */
@Data
public class StudentUpdateDTO {
    
    /** 学生表主键ID */
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    
    // --- 关联 SysUser 的基础信息 (允许管理员更新) ---
    private String nickname; // 姓名/昵称
    private String sex; // 性别
    private String phoneNumber; // 手机号
    private String email; // 电子邮箱
    private LocalDate dateOfBirth; // 出生日期 (虽然在 sys_user，但管理员在此处更新)
    private String hometown; // 籍贯
    private String politicalStatus; // 政治面貌
    private String homeAddress; // 家庭住址
    
    // --- StuStudent 的核心学籍/住宿信息 (允许管理员更新) ---
    private String enrollmentYear; // 入学年级
    private String educationLevel; // 学历层次
    private Integer standardDuration; // 标准学制
    private Integer currentGradeLevel; // 当前年级
    private Integer yearsHeldBack; // 留级年数
    private Long departmentId; // 院系ID
    private String majorName; // 专业名称
    private Long counselorUserId; // 辅导员ID
    private Long supervisorUserId; // 导师ID
    private Long currentCampusId; // 校区ID
    private LocalDate enterDate; // 入学日期
    private LocalDate graduationDate; // 毕业日期
    private String academicStatus; // 学籍状态
    private Integer isOnCampusResident; // 住宿意愿
    
    // 注意：currentBedId (当前床位ID) 通常不由管理员直接在此处修改，
    // 它应该由宿舍分配/调宿的专门业务流程来更新，以确保数据一致性。
}