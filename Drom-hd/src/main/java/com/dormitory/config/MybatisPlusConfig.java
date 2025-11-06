package com.dormitory.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.dormitory.handler.MyMetaObjectHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * MyBatis Plus 配置类
 * 确保分页插件和自动填充处理器生效
 */
@Configuration
public class MybatisPlusConfig {
    
    /**
     * 注册 MyBatis Plus 拦截器链
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 1. 分页插件 (非常关键！解决 total 为 0 的问题)
        // 指定数据库类型为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        // 2. 注册其他插件，例如乐观锁，但目前我们只需要分页
        
        return interceptor;
    }
    
    /**
     * 注册自动填充处理器 (确保 BaseEntity 的 createTime/updateTime 自动填充生效)
     * 虽然 MyMetaObjectHandler 已经有 @Component，但这里显式注册确保优先级
     */
    @Bean
    public MyMetaObjectHandler myMetaObjectHandler() {
        return new MyMetaObjectHandler();
    }
    
    
    /**
     * 注册 PasswordEncoder (BCrypt)
     * 将 BCrypt 密码编码器注入 Spring 容器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}