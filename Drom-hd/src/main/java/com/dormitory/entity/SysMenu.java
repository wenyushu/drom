package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单/权限实体类
 * 对应数据库表 sys_menu
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    /** 菜单ID */
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;
    
    /** 父菜单ID */
    private Long parentId;
    
    /** 菜单名称 */
    private String menuName;
    
    /** 菜单排序 */
    private Integer menuSort; // 增加一个排序字段，便于前端显示
    
    /** 类型 (M: 目录, C: 菜单, F: 按钮/权限) */
    private String menuType;
    
    /** 权限标识 (如：sys:user:query) */
    private String perms;
    
    /** 路由地址 */
    private String path;
    
    /** 菜单图标 */
    private String icon;
    
    /** 备注 */
    private String remark;
    
    // ---- 额外字段 (用于树形结构，非数据库字段) ----
    
    /** 子菜单列表 */
    // MyBatis-Plus 默认忽略 @TableField(exist = false)
    @TableField(exist = false) // <-- 关键修正：明确告诉 MyBatis-Plus 忽略此字段
    private java.util.List<SysMenu> children;
}