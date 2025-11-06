package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.ClassQueryDTO;
import com.dormitory.entity.BizClass;
import com.dormitory.service.IBizClassService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 班级信息管理控制器 (Admin 专属)
 */
@Tag(name = "班级管理", description = "Admin 对班级信息进行维护")
@RestController
@RequestMapping("/api/sys/class") // 归属到系统配置下
public class BizClassController {
    
    @Autowired private IBizClassService classService;
    
    @Operation(summary = "查询班级列表", description = "获取所有班级信息，含院系和辅导员")
    // @SaCheckPermission("sys:class:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<Page<BizClass>> list(@Valid ClassQueryDTO queryDTO) {
        Page<BizClass> page = classService.selectClassPage(queryDTO);
        return R.ok(page);
    }
    
    @Operation(summary = "新增班级", description = "添加新的班级")
    @SaCheckPermission("sys:class:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody BizClass bizClass) {
        classService.addClass(bizClass);
        return R.ok("班级新增成功");
    }
    
    @Operation(summary = "修改班级", description = "更新班级信息")
    @SaCheckPermission("sys:class:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody BizClass bizClass) {
        classService.updateClass(bizClass);
        return R.ok("班级修改成功");
    }
    
    @Operation(summary = "删除班级", description = "删除班级信息")
    @SaCheckPermission("sys:class:remove")
    @DeleteMapping("/{classId}")
    public R<Void> delete(@PathVariable Long classId) {
        classService.deleteClassById(classId);
        return R.ok("班级删除成功");
    }
}