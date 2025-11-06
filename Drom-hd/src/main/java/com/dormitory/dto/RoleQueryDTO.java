package com.dormitory.dto;

import lombok.Data;

/**
 * 角色查询请求参数 DTO
 */
@Data
public class RoleQueryDTO {
    
    /** 角色名称 */
    private String roleName;
    
    /** 角色权限关键字 */
    private String roleKey;
    
    /** 角色状态 (0: 正常, 1: 停用) */
    private Integer status;
    
    /** 当前页码 */
    private long current = 1;
    
    /** 每页大小 */
    private long size = 10;
}