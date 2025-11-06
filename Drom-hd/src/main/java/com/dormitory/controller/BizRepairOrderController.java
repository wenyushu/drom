package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.RepairOrderQueryDTO;
import com.dormitory.dto.RepairOrderUpdateStatusDTO;
import com.dormitory.entity.BizRepairOrder;
import com.dormitory.service.IBizRepairOrderService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 报修工单管理控制器
 */
@Tag(name = "报修工单管理", description = "学生提交、维修人员/Admin处理工单")
@RestController
@RequestMapping("/api/biz/repair")
public class BizRepairOrderController {
    
    @Autowired private IBizRepairOrderService orderService;
    
    /**
     * 1. 提交报修 (学生/宿管权限)
     */
    @Operation(summary = "提交报修", description = "学生或宿管提交新的维修工单")
    // 学生和宿管都有权限提交，权限标识可以命名为 biz:repair:submit
    @SaCheckPermission("biz:repair:submit")
    @PostMapping
    public R<Void> submit(@Valid @RequestBody BizRepairOrder order) {
        // Service 层会自动填充 applicantUserId 和 submitTime
        orderService.submitOrder(order);
        return R.ok("报修工单提交成功，请等待分配！");
    }
    
    /**
     * 2. 查询工单列表 (Admin/宿管/维修人员/学生)
     */
    @Operation(summary = "工单分页查询", description = "查询所有工单或个人工单")
    // @SaCheckPermission("biz:repair:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<Page<BizRepairOrder>> list(@Valid RepairOrderQueryDTO queryDTO) {
        // 业务逻辑应在 Service 层限制学生只能查自己的工单
        Page<BizRepairOrder> page = orderService.selectOrderPage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 3. 状态流转/分配 (维修人员/Admin权限)
     */
    @Operation(summary = "更新工单状态", description = "将工单状态流转至处理中、已完成或无法修复")
    @SaCheckPermission("biz:repair:handle")
    @PutMapping("/status")
    public R<Void> updateStatus(@Valid @RequestBody RepairOrderUpdateStatusDTO updateDTO) {
        orderService.updateOrderStatus(updateDTO);
        return R.ok("工单状态更新成功");
    }
}