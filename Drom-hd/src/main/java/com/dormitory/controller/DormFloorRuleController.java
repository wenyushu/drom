package com.dormitory.controller;

import com.dormitory.entity.DormFloorGenderRule;
import com.dormitory.service.IDormFloorGenderRuleService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 楼层性别规则管理控制器
 */
@Tag(name = "楼层性别规则管理", description = "Admin 对混合楼栋的楼层性别进行配置")
@RestController
@RequestMapping("/api/dorm/floor-rule")
public class DormFloorRuleController {
    
    @Autowired private IDormFloorGenderRuleService floorRuleService;
    
    @Operation(summary = "获取楼层的性别规则")
    // @SaCheckPermission("dorm:floor-rule:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/{floorId}") // <-- 按 floorId 查询
    public R<List<DormFloorGenderRule>> getRules(@PathVariable Long floorId) {
        return R.ok(floorRuleService.getRulesByFloorId(floorId));
    }
    
    @Operation(summary = "设置/更新楼层规则")
    @SaCheckPermission("dorm:floor-rule:edit")
    @PostMapping
    public R<Void> setRule(@Valid @RequestBody DormFloorGenderRule rule) {
        floorRuleService.saveOrUpdateFloorRule(rule);
        return R.ok("楼层性别规则设置成功");
    }
    
    @Operation(summary = "删除楼层规则")
    @SaCheckPermission("dorm:floor-rule:remove")
    @DeleteMapping("/{ruleId}")
    public R<Void> deleteRule(@PathVariable Long ruleId) {
        floorRuleService.deleteFloorRuleById(ruleId);
        return R.ok("楼层性别规则删除成功");
    }
}