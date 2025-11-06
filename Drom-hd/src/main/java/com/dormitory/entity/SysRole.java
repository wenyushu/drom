package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体类
 * SysRole.java (角色实体) 对应数据库表 sys_role
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role")
public class SysRole extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;
    
    /** 角色名称 */
    private String roleName;
    
    /** 权限关键字 */
    private String roleKey;
    
    /** 排序 */
    private Integer roleSort;
    
    /** 状态 (0: 正常, 1: 停用) */
    private Integer status;
    
    /** 备注 */
    private String remark;
}