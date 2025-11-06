package com.dormitory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * 用户新增/修改请求参数 DTO
 */
@Data
public class UserAddUpdateDTO {
    
    /** 用户ID (修改时必传) */
    private Long userId;
    
    /** 登录账号/学号/工号 */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    /** 密码 (新增时必传，修改时可选) */
    private String password;
    
    /** 关联角色ID */
    @NotNull(message = "角色 ID 不能为空")
    private Long roleId;
    
    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    private String nickname;
    
    /**
     * 新增字段：真实姓名
     * 区分昵称和真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;
    
    /** 用户类型 (0: Admin, 1: Student, 2: DormManager...) */
    @NotBlank(message = "用户类型不能为空")
    private String userType;
    
    /** 性别 (0: 男, 1: 女) */
    private String sex;
    
    /** 手机号码 */
    private String phoneNumber;
    
    // --- 基础信息字段 ---
    private String email;
    private LocalDate dateOfBirth;
    private String hometown;
    private String politicalStatus;
    private String homeAddress;
    
    /** 账号状态 (0: 正常, 1: 禁用) */
    @NotNull(message = "账号状态不能为空")
    private Integer status;
    
    /** 任职状态 (0: 在职, 1: 离职, 2: 停职) - 职工用 */
    private String employmentStatus;
    
    /**
     * (学生必填) 当前所在校区ID
     * (在 UserType=1 时, 此字段必须由前端提供)
     */
    private Long currentCampusId;
    
    /**
     * (学生必填) 入学日期
     * (在 UserType=1 时, 此字段必须由前端提供)
     */
    private LocalDate enterDate;
    
    /**
     * (教职工必填) 入职日期
     * (在 UserType=2,3,4... 时, 此字段必须由前端提供)
     */
    private LocalDate hireDate;
    
}