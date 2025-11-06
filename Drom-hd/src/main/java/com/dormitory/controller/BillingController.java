package com.dormitory.controller;

import com.dormitory.service.IBillingService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 水电费计费与系统操作控制器
 */
@Tag(name = "计费核心操作", description = "Admin/系统触发账单生成和支付模拟")
@RestController
@RequestMapping("/api/biz/billing/core")
public class BillingController {
    
    @Autowired private IBillingService billingService;
    
    /**
     * 1. 触发批量生成账单 (Admin 权限)
     */
    @Operation(summary = "触发周期计费", description = "生成上一个计费周期的水电费账单")
    @SaCheckPermission("biz:billing:generate") // 新增权限
    @PostMapping("/generate-bills")
    public R<String> generateBills(@RequestParam LocalDate cycleEndDate) {
        if (cycleEndDate == null) {
            cycleEndDate = LocalDate.now().minusDays(1);
        }
        String result = billingService.generateMonthlyBills(cycleEndDate);
        return R.ok(result);
    }
    
    /**
     * 2. 模拟支付成功 (Admin/财务权限)
     */
    @Operation(summary = "模拟支付成功", description = "Admin手动标记某张账单已支付（模拟网关回调）")
    @SaCheckPermission("biz:billing:simulate-pay") // 新增权限
    @PostMapping("/simulate-payment")
    public R<Void> simulatePayment(@RequestParam Long recordId, @RequestParam BigDecimal paidAmount) {
        // 触发核心支付处理逻辑
        billingService.processPaymentSuccess(recordId, paidAmount);
        return R.ok("账单 " + recordId + " 支付状态模拟更新成功");
    }
}