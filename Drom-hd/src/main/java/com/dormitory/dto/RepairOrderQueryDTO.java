package com.dormitory.dto;

import lombok.Data;

/**
 * 报修工单查询参数 DTO
 */
@Data
public class RepairOrderQueryDTO extends PageDTO {
    
    /** 房间ID */
    private Long roomId;
    
    /** 申请人用户ID */
    private Long applicantUserId;
    
    /** 工单状态 (0: 待分配, 1: 处理中, 2: 已完成, 3: 无法修复) */
    private String orderStatus;
    
    /** 紧急程度 (用于过滤) */
    private String urgencyLevel;
}