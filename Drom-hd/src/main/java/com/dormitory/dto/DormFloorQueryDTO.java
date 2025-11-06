package com.dormitory.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 楼层分页查询 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DormFloorQueryDTO extends PageDTO {
    
    /**
     * 按楼栋 ID 筛选 (非必填)
     */
    private Long buildingId;
}