package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List; // 用于树形结构

/**
 * 部门/院系实体类
 */
@EqualsAndHashCode(callSuper = true) // 继承 BaseEntity
@Data
@TableName("sys_department")
public class SysDepartment extends BaseEntity { // 继承 BaseEntity
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 部门/院系 ID */
    @TableId(value = "dept_id", type = IdType.AUTO)
    private Long deptId;
    
    /** 父级部门 ID */
    private Long parentId;
    
    /** 部门/院系名称 */
    @NotBlank(message = "部门名称不能为空")
    private String deptName;
    
    /** 部门编码 */
    private String deptCode;
    
    /** 部门负责人 ID (关联 sys_user) */
    private Long leaderId;
    
    /** 新增字段：排序号 (数字越小，越靠前) */
    private Integer deptSort;
    
    
    // ---- 非数据库字段 (用于树形结构) ----
    
    /** 子部门列表 */
    @TableField(exist = false)
    private List<SysDepartment> children;
}