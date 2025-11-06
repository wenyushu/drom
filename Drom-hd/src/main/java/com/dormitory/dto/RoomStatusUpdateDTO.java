package com.dormitory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 房间状态更新 DTO (宿管/辅导员专用)
 */
@Data
public class RoomStatusUpdateDTO {
    
    @NotNull(message = "房间 ID 不能为空")
    private Long roomId;
    
    /** 房间状态 (0: 正常, 1: 待维修/暂时禁用, 2: 重新装修/长期禁用) */
    @NotBlank(message = "房间状态不能为空")
    private String roomStatus;
    
    /** 备注 (可选) */
    private String remark;
}