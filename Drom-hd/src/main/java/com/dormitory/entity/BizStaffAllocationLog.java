package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 教职工/后勤分配日志实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_staff_allocation_log")
public class BizStaffAllocationLog extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;
    
    /** 教职工用户ID (FK: sys_user) */
    private Long userId;
    
    /** 床位 ID (FK: dorm_bed) */
    private Long bedId;
    
    /** 动作类型 (0: 入住/分配, 1: 调宿, 2: 迁出/离职) */
    private String actionType;
    
    /** 原因 (如: 管理员手动分配, 离职) */
    private String reason;
    
    /** 生效时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /** 操作人ID (Admin/人事) */
    private Long operatorId;
}