package com.dormitory.controller;

import com.dormitory.entity.DormElectricRule;
import com.dormitory.service.IDormElectricRuleService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宿舍用电规则控制器
 */
@Tag(name = "宿舍用电规则管理", description = "Admin 配置房间或楼栋的用电功率限制")
@RestController
@RequestMapping("/api/dorm/electric-rule")
public class DormElectricRuleController {
    
    @Autowired private IDormElectricRuleService ruleService;
    
    /**
     * 查询规则列表
     */
    @Operation(summary = "查询用电规则", description = "获取所有或特定房间/楼栋的用电规则")
    @GetMapping("/list")
    public R<List<DormElectricRule>> list(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long buildingId) {
        
        // 【【【【【 2. 核心修改：调用正确的 Service 方法 】】】】】
        // 错误调用：ruleService.list(wrapper);
        // 正确调用：
        List<DormElectricRule> rules = ruleService.selectRuleList(roomId, buildingId);
        
        return R.ok(rules);
    }
    
    
    /**
     * 新增或更新规则 (Admin 专属)
     */
    @Operation(summary = "新增或更新规则", description = "设置或修改房间/楼栋的功率限制")
    @SaCheckPermission("dorm:electric-rule:edit")
    @PostMapping
    public R<Void> saveOrUpdate(@Valid @RequestBody DormElectricRule rule) {
        ruleService.saveOrUpdateRule(rule);
        return R.ok("用电规则保存成功");
    }
    
    
    /**
     * 删除规则 (Admin 专属)
     */
    @Operation(summary = "删除规则", description = "根据规则 ID 删除用电规则")
    @SaCheckPermission("dorm:electric-rule:remove")
    @DeleteMapping("/{ruleId}")
    public R<Void> delete(@PathVariable Long ruleId) {
        ruleService.deleteRuleById(ruleId);
        return R.ok("用电规则删除成功");
    }
}