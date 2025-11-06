package com.dormitory.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate; // <-- 新增：导入 LocalDate
import java.time.LocalDateTime;

/**
 * 用户信息视图对象
 */
@Data
public class UserVO {
    
    /** 登录账号/学号/工号 */
    private String username;
    
    /** 密码 (注意：实际项目中应存储加密后的密文) */
    private String password;
    // 密码字段不应返回给前端
    
    /** 关联角色ID */
    private Long roleId;
    
    /** 昵称 */
    private String nickname;
    
    /** 真实姓名 */
    private String realName; // 姓名 (真实姓名)
    
    /** 用户类型 (0: Admin, 1: Student, 2: DormManager) */
    private String userType;
    
    private String sex;
    private String phoneNumber;
    
    /** 账号状态 (0: 正常, 1: 禁用) */
    private Integer status;
    
    /** 任职状态 (0: 在职, 1: 离职, 2: 停职) */
    private String employmentStatus;
    
    // --- 新增：详细基础信息字段 ---
    private String email;
    private LocalDate dateOfBirth; // 出生日期 (LocalDate 对应 DATE)
    private String hometown; // 籍贯
    private String politicalStatus; // 政治面貌
    private String ethnicity; // 民族
    private String homeAddress; // 家庭住址
    // --- 新增 END ---
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /** 角色名称 (需要联表或二次查询获取) */
    private String roleName;
    
    // 可以根据需要添加 updateTime 等 BaseEntity 字段
}