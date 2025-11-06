package com.dormitory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 调宿申请审批 DTO (管理端)
 */
@Data
public class RoomChangeApprovalDTO {
    
    @NotNull(message = "申请 ID 不能为空")
    private Long requestId;
    
    /** 新状态 (1: 批准, 2: 驳回) */
    @NotBlank(message = "审批状态不能为空")
    private String status;
    
    /** 审批意见 */
    private String approvalOpinion;
    
    /**
     * 【重要】如果批准，且学生未指定目标床位，
     * 管理员必须在此处指定一个空闲床位ID。
     */
    private Long targetBedId;
}