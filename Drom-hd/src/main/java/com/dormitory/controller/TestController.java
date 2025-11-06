package com.dormitory.controller;

import com.dormitory.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 权限测试控制器
 */
@Tag(name = "测试模块", description = "用于权限和登录测试")
@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Operation(summary = "需要登录的接口", description = "访问此接口必须携带有效的 Token")
    @GetMapping("/needLogin")
    public R<String> needLogin() {
        return R.ok("您已成功登录并访问了受保护的接口！");
    }
}