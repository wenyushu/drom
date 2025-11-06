package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.ElectricAlertQueryDTO;
import com.dormitory.dto.AlertResolveDTO; // 导入 DTO
import com.dormitory.entity.BizElectricAlert;
import com.dormitory.service.IBizElectricAlertService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 违规用电告警控制器
 */
@Tag(name = "违规用电告警管理", description = "记录、查询和处理用电告警")
@RestController
@RequestMapping("/api/biz/electric-alert")
public class BizElectricAlertController {
    
    @Autowired private IBizElectricAlertService alertService;
    
    /**
     * 查询告警列表 (所有人可查)
     */
    @Operation(summary = "告警分页查询", description = "查询违规用电告警记录")
    // 无 SaCheckPermission，所有人可查
    @GetMapping("/list")
    public R<Page<BizElectricAlert>> list(@Valid ElectricAlertQueryDTO queryDTO) {
        Page<BizElectricAlert> page = alertService.selectAlertPage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 新增告警记录 (Admin 或系统自动触发)
     */
    @Operation(summary = "新增告警", description = "手动录入或系统触发新增告警记录")
    @SaCheckPermission("biz:electric-alert:add") // 只有 Admin 或特定服务有权限
    @PostMapping
    public R<Void> add(@Valid @RequestBody BizElectricAlert alert) {
        alertService.addAlert(alert);
        return R.ok("告警记录新增成功");
    }
    
    /**
     * 标记告警为已处理 (Admin/维修人员权限)
     */
    @Operation(summary = "处理告警", description = "将告警记录标记为已处理/复位")
    @SaCheckPermission("biz:electric-alert:resolve") // 处理权限
    @PutMapping("/resolve") // 修改为 PutMapping，接收 Body
    public R<Void> resolve(@Valid @RequestBody AlertResolveDTO resolveDTO) { // 使用 DTO
        alertService.resolveAlert(resolveDTO);
        return R.ok("告警已标记为处理完成");
    }
    
    /**
     * 删除告警记录 (Admin 专属)
     */
    @Operation(summary = "删除告警记录", description = "删除告警历史记录")
    @SaCheckPermission("biz:electric-alert:remove") // 删除权限
    @DeleteMapping("/{alertId}")
    public R<Void> delete(@PathVariable Long alertId) {
        alertService.removeById(alertId);
        return R.ok("告警记录删除成功");
    }
}