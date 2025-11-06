package com.dormitory.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限拦截器配置 (已移除失效的 SaStrategy 配置)
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    
    /**
     * 注册 Sa-Token 拦截器，定义拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，并定义所有请求都要进行校验
        registry.addInterceptor(new SaInterceptor(handler -> {
            
            // --- 路由拦截规则 ---
            SaRouter.match("/**")
                    // 1. 排除开放接口 (无需登录)
                    .notMatch(
                            // 认证接口
                            "/auth/login",           // 登录接口
                            "/auth/info",            // Token 信息查看接口
                            "/auth/logout",          // 登出接口
                            "/error",               // 错误页
                            
                            // SpringDoc/Swagger UI 资源白名单 (核心修正)
                            "/swagger-ui.html",      // 主页面
                            "/swagger-ui/**",        // Swagger 界面, 依赖的JS/CSS等资源
                            "/v3/api-docs/**",       // OpenAPI 文档 JSON/YAML, Swagger JSON 数据
                            "/doc.html",             // 针对可能使用的旧版Knife4j
                            
                            // 其他静态资源
                            "/favicon.ico"  // 浏览器图标
                    )
                    // 2. 对匹配的路由执行登录校验
                    .check(StpUtil::checkLogin);
            
            // ... 此处未来可以扩展更细致的权限和角色校验
        })).addPathPatterns("/**"); // 拦截所有路径
    }
}