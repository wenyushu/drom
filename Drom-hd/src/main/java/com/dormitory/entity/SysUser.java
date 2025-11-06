package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户实体类
 * 对应数据库表 sys_user
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    
    /** 登录账号/学号/工号 */
    private String username;
    
    /** 密码 (注意：实际项目中应存储加密后的密文) */
    private String password;
    
    /** 关联角色ID */
    private Long roleId;
    
    /** 昵称 */
    private String nickname;
    
    /** 真实姓名 */
    private String realName; // 姓名 (真实姓名)
    
    /** 用户类型 (0: Admin, 1: Student, 2: DormManager) */
    private String userType;
    
    /** 性别 (0: 男, 1: 女) */
    private String sex;
    
    /** 手机号码 */
    private String phoneNumber;
    
    /** 任职状态 (0: 在职, 1: 离职, 2: 停职) */
    private String employmentStatus;
    
    /** 电子邮箱 */
    private String email;
    
    /** 用户头像 URL */
    private String avatar;
    
    /** 出生日期 */
    private LocalDate dateOfBirth; // 使用 LocalDate 映射 DATE 类型
    
    /** 籍贯 */
    private String hometown;
    
    /** 政治面貌 (党员, 团员, 群众等) */
    private String politicalStatus;
    
    /** 民族 */
    private String ethnicity;
    
    /** 家庭住址 */
    private String homeAddress;
    
    /** 账号状态 (0: 正常, 1: 禁用) */
    private Integer status;
    
    /** 逻辑删除标志 (0: 未删除, 1: 已删除) */
    @TableLogic
    private Integer deleted;
}