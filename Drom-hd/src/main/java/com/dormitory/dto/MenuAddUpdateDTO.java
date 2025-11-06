package com.dormitory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 菜单新增/修改请求参数 DTO
 */
@Data
public class MenuAddUpdateDTO {
    
    /** 菜单ID (修改时必传) */
    private Long menuId;
    
    /** 父菜单ID */
    @NotNull(message = "父菜单 ID 不能为空")
    private Long parentId;
    
    /** 菜单名称 */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;
    
    /** 类型 (M: 目录, C: 菜单, F: 按钮/权限) */
    @NotBlank(message = "菜单类型不能为空")
    private String menuType;
    
    /** 权限标识 (如果是按钮/权限，必填) */
    private String perms;
    
    /** 路由地址 (如果是菜单，必填) */
    private String path;
    
    /** 菜单图标 */
    private String icon;
}