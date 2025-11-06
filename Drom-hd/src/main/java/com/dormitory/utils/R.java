package com.dormitory.utils;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一接口响应类
 * 为了给前端返回统一规范的 JSON 格式，我们创建一个通用的响应封装类。
 * @param <T> 数据类型
 */
@Data
public class R<T> implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 状态码 */
    private int code;
    
    /** 响应消息 */
    private String msg;
    
    /** 返回数据 */
    private T data;
    
    /** 成功状态码 */
    private static final int SUCCESS_CODE = 200;
    /** 失败状态码 */
    private static final int ERROR_CODE = 500;
    
    // --- 构造器私有化 ---
    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    
    // --- 静态成功方法 ---
    public static <T> R<T> ok() {
        return new R<>(SUCCESS_CODE, "操作成功", null);
    }
    
    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS_CODE, "操作成功", data);
    }
    
    public static <T> R<T> ok(String msg, T data) {
        return new R<>(SUCCESS_CODE, msg, data);
    }
    
    /**
     * 静态成功方法 (只返回自定义消息，不返回数据)
     */
    public static <T> R<T> ok(String msg) {
        // 这里将 data 设为 null，T 类型推断为 Void 或 Object 均可，但实际使用时通常被期望为 R<Void>
        return new R<>(SUCCESS_CODE, msg, null);
    }
    
    // --- 静态失败方法 ---
    public static <T> R<T> fail(String msg) {
        return new R<>(ERROR_CODE, msg, null);
    }
    
    public static <T> R<T> fail(int code, String msg) {
        return new R<>(code, msg, null);
    }
}