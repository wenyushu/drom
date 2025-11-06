package com.dormitory.dto;

import lombok.Data;

/**
 * 登录请求 DTO
 * 为了规范接口参数，我们创建一个专门用于接收登录请求的 DTO。
 */
@Data
public class LoginDTO {
    
    /** 用户名 */
    private String username;
    
    /** 密码 */
    private String password;
    
    /** 验证码 (未来扩展用) */
    // private String code;
    
    /** 验证码 UUID (未来扩展用) */
    // private String uuid;
}