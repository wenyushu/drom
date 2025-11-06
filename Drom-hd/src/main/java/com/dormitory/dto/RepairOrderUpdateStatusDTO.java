package com.dormitory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 报修工单状态更新 DTO (用于维修人员)
 */
@Data
public class RepairOrderUpdateStatusDTO {
    
    /** 工单ID */
    @NotNull(message = "工单ID不能为空")
    private Long orderId;
    
    /** 新状态 (1: 处理中, 2: 已完成, 3: 无法修复) */
    @NotBlank(message = "工单状态不能为空")
    private String newStatus;
    
    /** 分配/处理人员ID (可选) */
    private Long handlerUserId;
    
    /** 维修结果总结/备注 (完成或无法修复时必填) */
    private String repairResult;
}