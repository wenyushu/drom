package com.dormitory.exception; // 新建 exception 包

import lombok.Getter;

/**
 * 自定义业务异常类
 * 用一个自定义异常来封装所有业务逻辑错误。
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final int code;
    
    public BusinessException(String message) {
        // 默认使用 500 状态码
        super(message);
        this.code = 500;
    }
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}