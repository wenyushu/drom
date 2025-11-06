package com.dormitory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 告警处理 DTO
 */
@Data
public class AlertResolveDTO {
    @NotNull(message = "告警的 ID 不能为空")
    private Long alertId;
    private String resolveRemark; // 处理备注 (可选, 如果需要存数据库需加字段)
}