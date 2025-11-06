package com.dormitory.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger (SpringDoc) 配置类
 * 用于配置 OpenAPI 文档的元数据和安全机制。
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * Sa-Token 配置的 Token 名称，通常是 satoken
     */
    private static final String SECURITY_SCHEME_NAME = "satoken";
    
    /**
     * 配置 OpenAPI 信息和安全方案
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("大学学生宿舍管理系统 API 文档")
                        .version("v1.0")
                        .description("基于 Spring Boot 3 + Sa-Token 的宿舍管理系统接口"))
                
                // --- 核心安全配置 ---
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        // 修正点 1: 使用 Type.APIKEY
                                        .type(SecurityScheme.Type.APIKEY)
                                        // 修正点 2: 使用 In.HEADER
                                        .in(SecurityScheme.In.HEADER)
                                        .description("Sa-Token 认证, 请在 Header 中输入 Token 值!")
                        )
                )
                // 确保所有受保护接口默认要求携带 Token
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}