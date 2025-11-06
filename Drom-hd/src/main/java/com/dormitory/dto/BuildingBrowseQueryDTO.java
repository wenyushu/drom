package com.dormitory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 浏览-楼栋分页查询 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BuildingBrowseQueryDTO extends PageDTO {
    
    /**
     * 按校区 ID 查询 (必填)
     */
    @NotNull(message = "校区 ID (campusId) 不能为空")
    private Long campusId;
}