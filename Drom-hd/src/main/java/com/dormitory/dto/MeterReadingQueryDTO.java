package com.dormitory.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 水电表读数查询参数 DTO
 */
@Data
public class MeterReadingQueryDTO extends PageDTO {
    
    /** 关联的表计ID */
    private Long meterId;
    
    /** 计量类型 (1: 电, 2: 水) */
    private String meterType;
    
    /** 房间ID (用于反查) */
    private Long roomId;
    
    /** 读数日期范围 - 开始 */
    private LocalDate startDate;
    
    /** 读数日期范围 - 结束 */
    private LocalDate endDate;
}