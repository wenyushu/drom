package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 学生离校/留校状态实体类
 */
@EqualsAndHashCode(callSuper = true) // 继承 BaseEntity
@Data
@TableName("stu_leave_status")
public class StuLeaveStatus extends BaseEntity { // 继承 BaseEntity
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /** 学生ID */
    @NotNull(message = "学生ID不能为空")
    private Long studentId;
    
    /** 当前状态 (0: 在校, 1: 假期离校, 2: 毕业离校, 3: 寒暑假留校, 4: 请假离校) */
    @NotNull(message = "状态类型不能为空")
    private String statusType;
    
    /** 状态开始时间 */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startDate;
    
    /** 状态预计结束时间 (可选) */
    private LocalDateTime endDate;
    
    /** 是否在寝室 (1: 是, 0: 否) */
    private Integer isInDorm;
    
    /** 备注 (如请假事由) */
    private String remark;
    
    // ---- 非数据库字段 (用于VO展示) ----
    
    /** 学生姓名 */
    @TableField(exist = false)
    private String studentName;
    
    /** 学生学号 */
    @TableField(exist = false)
    private String stuNumber;
}