package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.BillingRecordQueryDTO;
import com.dormitory.dto.BillingPaymentDTO; // 导入 DTO
import com.dormitory.entity.BizBillingRecord;
import com.dormitory.service.IBizBillingRecordService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 水电费账单记录控制器
 */
@Tag(name = "水电费账单管理", description = "生成、查询账单和处理支付")
@RestController
@RequestMapping("/api/biz/billing/record")
public class BizBillingRecordController {
    
    @Autowired private IBizBillingRecordService recordService;
    
    /**
     * 查询账单列表 (财务/水电管理员、学生本人)
     */
    @Operation(summary = "账单分页查询", description = "获取水电费账单列表")
    // @SaCheckPermission("biz:billing:query") // 财务/Admin/学生
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<Page<BizBillingRecord>> list(@Valid BillingRecordQueryDTO queryDTO) {
        Page<BizBillingRecord> page = recordService.selectRecordPage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 确认支付 (学生/外部系统回调)
     */
    @Operation(summary = "确认支付", description = "标记账单为已支付，需校验金额")
    @SaCheckPermission("biz:billing:pay") // 学生或系统支付权限
    @PostMapping("/pay")
    public R<Void> pay(@Valid @RequestBody BillingPaymentDTO paymentDTO) {
        recordService.processPayment(paymentDTO);
        return R.ok("支付成功，账单状态已更新");
    }
    
    // --- Admin/系统内部接口 ---
    
    /**
     * 新增账单记录 (仅限系统内部或计费服务调用)
     */
    @Operation(summary = "新增账单", description = "由计费服务生成新的账单记录")
    @SaCheckPermission("biz:billing:add") // Admin/系统权限
    @PostMapping
    public R<Void> add(@Valid @RequestBody BizBillingRecord record) {
        recordService.save(record);
        return R.ok("账单记录新增成功");
    }
}