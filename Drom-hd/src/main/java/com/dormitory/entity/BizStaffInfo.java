package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 教职工/后勤住宿信息实体类
 */
@EqualsAndHashCode(callSuper = true) // 继承 BaseEntity
@Data
@TableName("biz_staff_info")
public class BizStaffInfo extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    // ===================================
    // --- 数据库字段 ---
    // ===================================
    
    /** 用户ID (主键, 关联 sys_user) */
    @TableId(value = "user_id")
    @NotNull(message = "关联的用户 ID 不能为空")
    private Long userId;
    
    /** 所属部门ID (FK: sys_department) */
    private Long departmentId;
    
    /** 职位名称 */
    private String jobTitle;
    
    /** 入职日期 */
    @NotNull(message = "入职日期不能为空")
    private LocalDate hireDate;
    
    /** 职称名称 (如：教授, 处长) */
    private String titleName;
    
    /** 合同/任期类型 */
    private String contractType;
    
    /** 合同年限（年） */
    private Integer contractDurationYears;
    
    /** 住宿意愿 (0: 校外, 1: 校内) */
    private Integer isOnCampusResident;
    
    /** 当前床位ID (FK: dorm_bed) */
    private Long currentBedId;
    
    /** 分配类型 (1: 长期, 2: 短期) */
    private String allocationType;
    
    /** 预计离职/搬离日期 */
    private LocalDate expectedLeaveDate;
    
    // ===================================
    // ---- 非数据库字段 (用于VO展示) ----
    // ===================================
    
    // (来自 SysUser 表)
    @TableField(exist = false)
    private String username; // 工号 (for setUsername)
    
    @TableField(exist = false)
    private String realName; // 真实姓名
    
    @TableField(exist = false)
    private String nickname; // 昵称
    
    @TableField(exist = false)
    private String sex;
    
    @TableField(exist = false)
    private String phoneNumber;
    @TableField(exist = false)
    private String email;
    @TableField(exist = false)
    private LocalDate dateOfBirth;
    
    @TableField(exist = false)
    private String hometown;
    
    @TableField(exist = false)
    private String politicalStatus;
    
    @TableField(exist = false)
    private String homeAddress;
    
    @TableField(exist = false)
    private String ethnicity; /** 民族 */
    
    @TableField(exist = false)
    private String avatar; /** 头像 */
    
    // (来自 SysDepartment 表)
    @TableField(exist = false)
    private String departmentName;
    
    // (来自 DormBed 关联查询)
    @TableField(exist = false)
    private String bedInfo; // (楼栋-房间-床号)
}