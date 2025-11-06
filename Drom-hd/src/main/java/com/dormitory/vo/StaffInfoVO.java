package com.dormitory.vo;

import com.dormitory.entity.BizStaffInfo; // 导入实体
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教职工信息视图对象 (包含所有关联信息)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StaffInfoVO extends BizStaffInfo {
    // 继承 BizStaffInfo 以包含所有数据库字段和非数据库字段
    // BaseEntity 的字段也会自动包含
}