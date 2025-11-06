package com.dormitory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 账单支付 DTO
 */
@Data
public class BillingPaymentDTO {
    @NotNull(message = "账单记录 ID 不能为空")
    private Long recordId;
    
    @NotNull(message = "支付金额不能为空")
    private BigDecimal paidAmount;
    
    // 支付方式、交易ID等字段可在此扩展
}