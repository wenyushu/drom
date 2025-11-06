package com.dormitory.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 水电费核心计费服务接口 (用于计算和生成账单)
 */
public interface IBillingService {
    
    /**
     * 执行周期性计费，生成所有房间的水电费账单
     * @param cycleEndDate 计费周期截止日期
     * @return 计费结果摘要
     */
    String generateMonthlyBills(LocalDate cycleEndDate);
    
    /**
     * 处理账单支付成功后的状态更新
     * @param recordId 账单ID
     * @param paidAmount 支付金额 (用于核对)
     */
    void processPaymentSuccess(Long recordId, BigDecimal paidAmount);
}