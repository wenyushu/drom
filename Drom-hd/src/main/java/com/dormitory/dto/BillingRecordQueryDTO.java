package com.dormitory.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 账单记录查询参数 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BillingRecordQueryDTO extends PageDTO {
    
    /** 房间ID */
    private Long roomId;
    
    /** 计量类型 (1: 电, 2: 水) */
    private String meterType;
    
    /** 账单状态 (0: 未出账, 1: 已出账, 2: 已支付, 3: 逾期) */
    private String status;
    
    /** 是否已支付 */
    private Integer isPaid;
    
    /** 周期开始日期 (筛选) */
    private LocalDate cycleStartDate;
}