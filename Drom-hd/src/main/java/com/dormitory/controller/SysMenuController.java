package com.dormitory.controller;

import com.dormitory.dto.MenuAddUpdateDTO;
import com.dormitory.dto.MenuQueryDTO;
import com.dormitory.entity.SysMenu;
import com.dormitory.service.ISysMenuService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单/权限管理控制器
 */
@Tag(name = "系统菜单权限管理", description = "Admin对系统菜单和权限标识进行管理")
@RestController
@RequestMapping("/api/sys/menu")
public class SysMenuController {
    
    @Autowired
    private ISysMenuService menuService;
    
    /**
     * 获取菜单列表 (树形结构)
     */
    @Operation(summary = "菜单树形列表查询", description = "查询所有菜单和权限，以树形结构返回")
    @SaCheckPermission("sys:menu:query")
    @GetMapping("/list")
    public R<List<SysMenu>> list(@Valid MenuQueryDTO queryDTO) {
        List<SysMenu> list = menuService.selectMenuList(queryDTO);
        return R.ok(list);
    }
    
    /**
     * 新增菜单/权限
     */
    @Operation(summary = "新增菜单/权限", description = "新增目录、菜单或按钮权限")
    @SaCheckPermission("sys:menu:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody MenuAddUpdateDTO addDTO) {
        menuService.addMenu(addDTO);
        return R.ok("菜单/权限新增成功");
    }
    
    /**
     * 修改菜单/权限
     */
    @Operation(summary = "修改菜单/权限", description = "修改菜单信息")
    @SaCheckPermission("sys:menu:edit")
    @PutMapping
    public R<Void> edit(@Valid @RequestBody MenuAddUpdateDTO updateDTO) {
        menuService.updateMenu(updateDTO);
        return R.ok("菜单/权限修改成功");
    }
    
    /**
     * 删除菜单/权限
     */
    @Operation(summary = "删除菜单/权限", description = "根据菜单ID删除菜单或权限")
    @SaCheckPermission("sys:menu:remove")
    @DeleteMapping("/{menuId}")
    public R<Void> remove(@PathVariable Long menuId) {
        menuService.deleteMenuById(menuId);
        return R.ok("菜单/权限删除成功");
    }
    
    /**
     * 获取单个菜单信息
     */
    @Operation(summary = "获取菜单详细信息", description = "根据ID获取菜单信息")
    @SaCheckPermission("sys:menu:query")
    @GetMapping("/{menuId}")
    public R<SysMenu> getInfo(@PathVariable Long menuId) {
        return R.ok(menuService.getById(menuId));
    }
}