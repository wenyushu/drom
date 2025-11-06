package com.dormitory.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dormitory.entity.StuStudent; // 导入实体

/**
 * 学生信息视图对象 (包含所有关联信息)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StudentVO extends StuStudent {
    // 继承 StuStudent 以包含所有数据库字段
    // BaseEntity 的字段也会自动包含
    
    // 非数据库字段已在 StuStudent 中用 @TableField(exist = false) 定义
    // 例如: nickname, sex, phoneNumber, departmentName, campusName,
    // counselorName, supervisorName, bedInfo
}