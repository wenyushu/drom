package com.dormitory.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 违规用电告警查询参数 DTO
 */
@Data
public class ElectricAlertQueryDTO extends PageDTO {
    
    /** 房间ID */
    private Long roomId;
    
    /** 告警类型 */
    private String alertType;
    
    /** 是否已处理 */
    private Integer isResolved;
    
    /** 告警日期范围 - 开始 */
    private LocalDate startDate;
    
    /** 告警日期范围 - 结束 */
    private LocalDate endDate;
}