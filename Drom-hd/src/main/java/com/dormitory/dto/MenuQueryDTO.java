package com.dormitory.dto;

import lombok.Data;

/**
 * 菜单查询请求参数 DTO
 */
@Data
public class MenuQueryDTO {
    
    /** 菜单名称 */
    private String menuName;
    
    /** 菜单类型 (M: 目录, C: 菜单, F: 按钮) */
    private String menuType;
    
    /** 权限标识 */
    private String perms;
    
    // 菜单查询通常不需要分页，因为数据量不大，一般返回全部树形结构
    // 但为了 API 规范，保留分页参数（可选，这里先不加）
}