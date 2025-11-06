package com.dormitory.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 水表分页查询 DTO (目前仅用于分页)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DormMeterWaterQueryDTO extends PageDTO {
    // 未来可在这里添加筛选字段，如
    // private String meterCode;
    // private String status;
}