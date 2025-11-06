package com.dormitory.controller;

import com.dormitory.entity.SysDepartment;
import com.dormitory.service.ISysDepartmentService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门/院系管理控制器 (Admin 专属)
 */
@Tag(name = "部门院系管理", description = "Admin对部门/院系信息进行维护")
@RestController
@RequestMapping("/api/sys/department")
public class SysDepartmentController {
    
    @Autowired private ISysDepartmentService departmentService;
    
    /**
     * 查询部门/院系列表 (树形结构)
     * (此接口现在会自动按 deptSort 排序)
     */
    @Operation(summary = "查询部门/院系列表", description = "获取所有部门/院系信息 (树形结构)")
    // @SaCheckPermission("sys:dept:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<List<SysDepartment>> list() {
        return R.ok(departmentService.selectDeptTree());
    }
    
    /**
     * 新增部门
     */
    @Operation(summary = "新增部门/院系", description = "添加新的部门/院系")
    @SaCheckPermission("sys:dept:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody SysDepartment department) {
        // department 对象中现在可以包含 deptSort 字段
        departmentService.save(department);
        return R.ok("部门/院系新增成功");
    }
    
    /**
     * 修改部门
     */
    @Operation(summary = "修改部门/院系", description = "更新部门/院系信息")
    @SaCheckPermission("sys:dept:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody SysDepartment department) {
        // department 对象中现在可以包含 deptSort 字段
        departmentService.updateById(department);
        return R.ok("部门/院系修改成功");
    }
    
    // 删除部门
    @Operation(summary = "删除部门/院系", description = "删除部门/院系信息")
    @SaCheckPermission("sys:dept:remove")
    @DeleteMapping("/{deptId}")
    public R<Void> delete(@PathVariable Long deptId) {
        departmentService.deleteDepartment(deptId);
        return R.ok("部门/院系删除成功");
    }
}