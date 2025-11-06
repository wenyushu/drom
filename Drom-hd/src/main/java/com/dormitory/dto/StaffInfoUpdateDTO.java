package com.dormitory.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 教职工信息更新 DTO (只包含可修改字段，用于 PUT 请求)
 */
@Data
public class StaffInfoUpdateDTO {
    
    /** 关联的用户ID (主键) */
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;
    
    // --- 档案信息 ---
    
    /** 职位名称 (可选) */
    private String jobTitle;
    
    /** 职称名称 (可选) */
    private String titleName;
    
    /** 合同/任期类型 (可选) */
    private String contractType; // <-- 新增：合同类型
    
    /** 合同年限（年） (可选) */
    private Integer contractDurationYears; // <-- 新增：合同年限
    
    /** 预计离职/搬离日期 (系统自动计算，但 DTO 需包含以便更新) */
    private LocalDate expectedLeaveDate; // <-- DTO中用于传递计算结果
    
    /** 住宿意愿 (0: 校外, 1: 校内 - 核心修改项) */
    @Min(value = 0, message = "住宿意愿值无效")
    @Max(value = 1, message = "住宿意愿值无效")
    private Integer isOnCampusResident;
    
    /** 所属部门ID (可选) */
    private Long departmentId;
    
    // 注意：hireDate 不在此处修改，它应通过专门的人事流程录入
}