package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.UserAddUpdateDTO;
import com.dormitory.dto.UserQueryDTO;
import com.dormitory.entity.SysUser;
import com.dormitory.service.ISysUserService;
import com.dormitory.utils.R;
import com.dormitory.vo.UserVO;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统用户管理控制器
 * 我们使用 @SaCheckPermission 注解来保护接口，确保只有 Admin 账户（拥有所有权限）能进行操作。
 */
@Tag(name = "系统用户管理", description = "Admin对用户(包括学生、宿管等)进行管理")
@RestController
@RequestMapping("/api/sys/user")
public class SysUserController {
    
    @Autowired
    private ISysUserService userService;
    
    /**
     * 分页查询用户列表
     */
    @Operation(summary = "用户分页查询", description = "根据条件查询系统用户列表")
    @SaCheckPermission("sys:user:query")
    @GetMapping("/list")
    public R<Page<UserVO>> list(@Valid UserQueryDTO queryDTO) {
        Page<UserVO> page = userService.selectUserPage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 新增用户
     */
    @Operation(summary = "新增用户", description = "新增系统用户，并分配角色")
    @SaCheckPermission("sys:user:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody UserAddUpdateDTO addDTO) {
        userService.addUser(addDTO);
        return R.ok("用户新增成功");
    }
    
    /**
     * 修改用户
     */
    @Operation(summary = "修改用户", description = "修改系统用户信息和角色")
    @SaCheckPermission("sys:user:edit")
    @PutMapping
    public R<Void> edit(@Valid @RequestBody UserAddUpdateDTO updateDTO) {
        userService.updateUser(updateDTO);
        return R.ok("用户修改成功");
    }
    
    /**
     * 批量删除用户 (逻辑删除)
     */
    @Operation(summary = "批量删除用户", description = "根据用户 ID 批量逻辑删除用户")
    @SaCheckPermission("sys:user:remove")
    @DeleteMapping("/{userIds}")
    public R<Void> remove(@PathVariable Long[] userIds) {
        userService.deleteUserByIds(userIds);
        return R.ok("用户删除成功");
    }
    
    /**
     * 获取用户详细信息
     */
    @Operation(summary = "获取用户详细信息", description = "根据用户 ID 获取单个用户的详细信息")
    @SaCheckPermission("sys:user:query")
    @GetMapping("/{userId}")
    public R<SysUser> getInfo(@PathVariable Long userId) {
        return R.ok(userService.getById(userId));
    }
}