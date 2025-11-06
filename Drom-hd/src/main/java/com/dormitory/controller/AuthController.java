package com.dormitory.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.dormitory.dto.LoginDTO;
import com.dormitory.service.IAuthService;
import com.dormitory.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证与授权控制器 (登录、登出)
 * 创建一个 AuthController 来处理登录和登出请求。
 */
@Tag(name = "认证授权模块", description = "用户登录、登出及 Token 相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private IAuthService authService;
    
    /**
     * 用户登录接口
     */
    @Operation(summary = "用户登录", description = "通过用户名和密码获取 Token")
    @PostMapping("/login")
    public R<String> login(@RequestBody LoginDTO loginDTO) {
    
//        try {
//            String token = authService.login(loginDTO.getUsername(), loginDTO.getPassword());
//            return R.ok("登录成功", token);
//        } catch (Exception e) {
//            // 在实际项目中，这里应该捕获自定义的业务异常
//            return R.fail(e.getMessage());
//        }
        
        // 直接调用，异常会被 GlobalExceptionHandler 捕获
        String token = authService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return R.ok("登录成功", token);
    }
    
    /**
     * 用户登出接口
     */
    @Operation(summary = "用户登出", description = "注销当前 Token")
    @PostMapping("/logout")
    public R<Void> logout() {
        StpUtil.logout();
        // 现在 R.ok("登出成功") 调用的是新加的 R.ok(String msg) 方法
        return R.ok("登出成功");
    }
    
    /**
     * 获取当前 Token 信息 (测试用)
     */
    @Operation(summary = "获取 Token 信息", description = "获取当前会话的 Token 信息和有效期")
    @GetMapping("/info")
    public R<Object> getLoginInfo() {
        return R.ok("当前会话信息", StpUtil.getTokenInfo());
    }
}