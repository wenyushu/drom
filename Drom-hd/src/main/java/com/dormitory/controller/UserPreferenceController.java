package com.dormitory.controller;

import com.dormitory.entity.UserPreference;
import com.dormitory.service.IUserPreferenceService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通用用户住宿偏好管理控制器
 */
@Tag(name = "通用用户住宿偏好管理", description = "用户和管理员对住宿偏好进行管理")
@RestController
@RequestMapping("/api/user/preference")
public class UserPreferenceController {
    
    @Autowired
    private IUserPreferenceService preferenceService;
    
    // --- 用户自助接口 (适用于所有登录用户) ---
    @Operation(summary = "用户获取【自己】的偏好", description = "查询自己的住宿偏好设置")
    @SaCheckPermission("user:preference:query:self")
    @GetMapping("/my")
    public R<UserPreference> getMyPreference() {
        Long loginId = StpUtil.getLoginIdAsLong();
        UserPreference preference = preferenceService.getMyPreference(loginId);
        return R.ok(preference);
    }
    
    @Operation(summary = "用户保存【自己】的偏好", description = "新增或修改自己的住宿偏好设置")
    @SaCheckPermission("user:preference:edit:self")
    @PostMapping("/my")
    public R<Void> saveMyPreference(@Valid @RequestBody UserPreference preference) {
        Long loginId = StpUtil.getLoginIdAsLong();
        preferenceService.saveMyPreference(preference, loginId);
        return R.ok("住宿偏好保存成功！");
    }
    
    
    // --- 管理员接口 ---
    @Operation(summary = "管理员获取【指定用户】的偏好", description = "管理员根据用户ID查询住宿偏好")
    @SaCheckPermission("user:preference:query:admin")
    @GetMapping("/{userId}")
    public R<UserPreference> getPreferenceByUserId(@PathVariable Long userId) {
        UserPreference preference = preferenceService.getPreferenceByUserId(userId);
        return R.ok(preference);
    }
    
    @Operation(summary = "管理员保存【指定用户】的偏好", description = "管理员修改指定用户的住宿偏好")
    @SaCheckPermission("user:preference:edit:admin")
    @PostMapping("/admin")
    public R<Void> savePreferenceByAdmin(@Valid @RequestBody UserPreference preference) {
        preferenceService.savePreferenceByAdmin(preference);
        return R.ok("管理员保存偏好成功");
    }
}