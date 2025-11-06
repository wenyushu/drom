package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat; // 导入 JsonFormat
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 宿舍分配/调动记录实体类
 */
@EqualsAndHashCode(callSuper = true) // 继承 BaseEntity
@Data
@TableName("dorm_allocation_log")
public class DormAllocationLog extends BaseEntity { // 继承 BaseEntity
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 记录ID */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
    
    /** 学生ID (FK) */
    @NotNull(message = "学生 ID 不能为空")
    private Long studentId;
    
    /** 床位ID (FK) */
    @NotNull(message = "床位 ID 不能为空")
    private Long bedId;
    
    /** 动作类型 (0: 入住/分配, 1: 调宿, 2: 主动退宿, 3: 强制离校, 4: 休学退宿) */
    @NotNull(message = "动作类型不能为空")
    private String actionType;
    
    /** 变动原因 (如：转专业, 校外申请, 新生分配, 毕业, 休学) */
    private String reasonType;
    
    /** 流程状态 (0: 待审, 1: 已批, 2: 已执行, 3: 已拒) */
    private String flowStatus;
    
    /** 生效时间 (入住/迁出时间) */
    @NotNull(message = "生效时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /** 结束时间 (记录失效时间，例如调出时间) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /** 目标房间ID (调宿申请用) */
    private Long targetRoomId;
    
    /** 当前是否处于此记录状态 (1: 是, 0: 否) */
    private Integer isActive;
    
    /** 操作员/系统 */
    private String operator;
    
    // BaseEntity 字段: createBy, createTime, updateBy, updateTime
}