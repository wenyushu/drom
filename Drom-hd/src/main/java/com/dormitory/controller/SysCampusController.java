package com.dormitory.controller;

import com.dormitory.entity.SysCampus;
import com.dormitory.service.ISysCampusService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校区管理控制器 (Admin 专属)
 */
@Tag(name = "校区管理", description = "Admin对校区信息进行维护")
@RestController
@RequestMapping("/api/sys/campus")
public class SysCampusController {
    
    @Autowired private ISysCampusService campusService;
    
    
    @Operation(summary = "查询校区列表", description = "获取所有校区信息")
    // @SaCheckPermission("sys:campus:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<List<SysCampus>> list() {
        return R.ok(campusService.list());
    }
    
    
    @Operation(summary = "新增校区", description = "添加新的校区")
    @SaCheckPermission("sys:campus:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysCampus campus) {
        // Service 层应增加 campusCode 唯一性校验
        campusService.save(campus);
        return R.ok("校区新增成功");
    }
    
    
    @Operation(summary = "修改校区", description = "更新校区信息")
    @SaCheckPermission("sys:campus:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysCampus campus) {
        campusService.updateById(campus);
        return R.ok("校区修改成功");
    }
    
    
    @Operation(summary = "删除校区", description = "删除校区信息（含业务校验）")
    @SaCheckPermission("sys:campus:remove")
    @DeleteMapping("/{campusId}")
    public R<Void> delete(@PathVariable Long campusId) {
        // Service 层应增加校验：如果校区下有关联楼栋，则不允许删除
        // campusService.removeById(campusId);
        campusService.deleteCampus(campusId); // 调用新方法
        return R.ok("校区删除成功");
    }
}