package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.RoleAddUpdateDTO;
import com.dormitory.dto.RoleQueryDTO;
import com.dormitory.entity.SysRole;
import com.dormitory.service.ISysRoleService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统角色管理控制器
 * 我们将使用 @SaCheckPermission("sys:role:*") 来保护所有角色管理接口。
 */
@Tag(name = "系统角色管理", description = "Admin对角色信息和权限进行管理")
@RestController
@RequestMapping("/api/sys/role")
public class SysRoleController {
    
    @Autowired
    private ISysRoleService roleService;
    
    /**
     * 分页查询角色列表
     */
    @Operation(summary = "角色分页查询", description = "查询系统角色列表")
    @SaCheckPermission("sys:role:query")
    @GetMapping("/list")
    public R<Page<SysRole>> list(@Valid RoleQueryDTO queryDTO) {
        Page<SysRole> page = roleService.selectRolePage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 新增角色
     */
    @Operation(summary = "新增角色", description = "新增角色并分配菜单权限")
    @SaCheckPermission("sys:role:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody RoleAddUpdateDTO addDTO) {
        roleService.addRole(addDTO);
        return R.ok("角色新增成功");
    }
    
    /**
     * 修改角色
     */
    @Operation(summary = "修改角色", description = "修改角色信息及菜单权限")
    @SaCheckPermission("sys:role:edit")
    @PutMapping
    public R<Void> edit(@Valid @RequestBody RoleAddUpdateDTO updateDTO) {
        roleService.updateRole(updateDTO);
        return R.ok("角色修改成功");
    }
    
    /**
     * 批量删除角色
     */
    @Operation(summary = "批量删除角色", description = "根据角色 ID 批量删除角色")
    @SaCheckPermission("sys:role:remove")
    @DeleteMapping("/{roleIds}")
    public R<Void> remove(@PathVariable Long[] roleIds) {
        roleService.deleteRoleByIds(roleIds);
        return R.ok("角色删除成功");
    }
    
    /**
     * 获取单个角色信息 (通常用于回显)
     */
    @Operation(summary = "获取角色信息", description = "根据角色 ID 获取角色详细信息")
    @SaCheckPermission("sys:role:query")
    @GetMapping("/{roleId}")
    public R<SysRole> getInfo(@PathVariable Long roleId) {
        return R.ok(roleService.getById(roleId));
    }
}