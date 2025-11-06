package com.dormitory.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.entity.BizBillingRate;
import com.dormitory.service.IBizBillingRateService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 计费费率管理控制器 (Admin 专属)
 */
@Tag(name = "水电费费率管理", description = "Admin 配置水电费单价和阶梯规则")
@RestController
@RequestMapping("/api/biz/billing/rate")
public class BizBillingRateController {
    
    @Autowired private IBizBillingRateService rateService;
    
    @Operation(summary = "查询所有费率", description = "获取所有费率配置，按生效日期排序")
    // @SaCheckPermission("biz:rate:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<List<BizBillingRate>> list() {
        // 实际中可能需要分页和按生效日期筛选
        return R.ok(rateService.list(new LambdaQueryWrapper<BizBillingRate>().orderByDesc(BizBillingRate::getValidFrom)));
    }
    
    @Operation(summary = "新增费率", description = "添加新的计费费率")
    @SaCheckPermission("biz:rate:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody BizBillingRate rate) {
        // TODO: Service 层需要校验费率的唯一性 (例如，同一类型、同一生效日期的费率不能重复)
        rateService.save(rate);
        return R.ok("费率新增成功");
    }
    
    @Operation(summary = "修改费率", description = "更新费率配置")
    @SaCheckPermission("biz:rate:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody BizBillingRate rate) {
        rateService.updateById(rate);
        return R.ok("费率修改成功");
    }
    
    @Operation(summary = "删除费率", description = "删除费率配置")
    @SaCheckPermission("biz:rate:remove")
    @DeleteMapping("/{rateId}")
    public R<Void> delete(@PathVariable Long rateId) {
        // TODO: Service 层需要校验是否有正在生效或已出账的账单关联此费率
        rateService.removeById(rateId);
        return R.ok("费率删除成功");
    }
}