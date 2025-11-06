package com.dormitory.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 电表分页查询 DTO (目前仅用于分页)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DormMeterElectricQueryDTO extends PageDTO {
    // 未来可在这里添加筛选字段
}