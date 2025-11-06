package com.dormitory.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.dormitory.exception.BusinessException;
import com.dormitory.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 这个类会捕获所有 Controller 抛出的异常，并将其封装成统一的 R 格式返回给前端。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 1. 处理自定义业务异常 (BusinessException)
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }
    
    // ---------------------- Sa-Token 权限异常处理 ----------------------
    
    /**
     * 2. 处理 Sa-Token：未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLoginException(NotLoginException e) {
        log.warn("认证失败：用户未登录或Token过期. 类型: {}", e.getType());
        return R.fail(401, "认证失败，请重新登录");
    }
    
    /**
     * 3. 处理 Sa-Token：缺少权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public R<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("权限不足：缺少权限标识 {}", e.getCode());
        return R.fail(403, "权限不足，请联系管理员");
    }
    
    /**
     * 4. 处理 Sa-Token：缺少角色异常
     */
    @ExceptionHandler(NotRoleException.class)
    public R<Void> handleNotRoleException(NotRoleException e) {
        log.warn("权限不足：缺少角色 {}", e.getRole());
        return R.fail(403, "权限不足，您不具备该角色");
    }
    
    // ---------------------- 通用异常处理 ----------------------
    
    /**
     * 5. 处理所有未被捕获的通用异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleOtherException(Exception e) {
        log.error("系统发生未知错误: ", e);
        return R.fail(500, "系统内部错误，请联系管理员");
    }
}