package com.dormitory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 调宿申请提交 DTO (学生端)
 */
@Data
public class RoomChangeRequestDTO {
    
    /** 目标床位 ID (可选) */
    private Long targetBedId;
    
    /** 申请原因 */
    @NotBlank(message = "调宿原因不能为空")
    private String reason;
}