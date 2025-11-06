package com.dormitory.service;

/**
 * 认证服务接口
 */
public interface IAuthService {
    
    /**
     * 用户登录并返回 Token
     * @param username 用户名
     * @param password 密码 (未加密)
     * @return 登录成功后的 Sa-Token
     */
    String login(String username, String password);
}