package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 班级信息实体类
 * (映射 biz_class 表)
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_class")
public class BizClass extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 班级ID */
    @TableId(value = "class_id", type = IdType.AUTO)
    private Long classId;
    
    /** 班级名称 (如：计算机科学23-1班) */
    @NotBlank(message = "班级名称不能为空")
    private String className;
    
    /** 所属院系ID (FK: sys_department) */
    @NotNull(message = "所属院系不能为空")
    private Long departmentId;
    
    /** 专业名称 */
    @NotBlank(message = "专业名称不能为空")
    private String majorName;
    
    /** 辅导员用户ID (FK: sys_user) */
    private Long counselorUserId; // 可以为空，后续再分配
    
    /** 入学年级 (例如: 2023) */
    @NotBlank(message = "入学年级不能为空")
    private String enrollmentYear;
    
    /** 班级人数 (冗余字段，可选) */
    private Integer studentCount;
    
    // --- 非数据库字段 (用于VO展示) ---
    
    /** 院系名称 */
    @TableField(exist = false)
    private String departmentName;
    
    /** 辅导员姓名 */
    @TableField(exist = false)
    private String counselorName;
}