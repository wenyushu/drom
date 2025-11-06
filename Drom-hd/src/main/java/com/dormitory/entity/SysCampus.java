package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 校区实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_campus")
public class SysCampus extends BaseEntity { // 继承 BaseEntity 确保有时间字段
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "campus_id", type = IdType.AUTO)
    private Long campusId;
    
    /** 校区名称 */
    private String campusName;
    
    /** 校区编码 */
    private String campusCode;
    
    /** 状态 (0: 启用, 1: 停用) */
    private Integer status;
}