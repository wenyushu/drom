package com.dormitory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 浏览-房间分页查询 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomBrowseQueryDTO extends PageDTO {
    
    /**
     * 按楼层 ID 查询 (必填)
     */
    @NotNull(message = "楼层 ID (floorId) 不能为空")
    private Long floorId;
}