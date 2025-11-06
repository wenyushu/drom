package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank; // 导入
import jakarta.validation.constraints.NotNull; // 导入
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial; // 导入

/**
 * 混合楼栋楼层性别规则实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("dorm_floor_gender_rule")
public class DormFloorGenderRule extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "rule_id", type = IdType.AUTO)
    private Long ruleId;
    
    /** 所属楼层ID (FK: dorm_floor) */
    @NotNull(message = "楼层 ID 不能为空")
    private Long floorId;
    
    /** 该楼层的性别限制 (0: 男, 1: 女) */
    @NotBlank(message = "性别不能为空") // 修改为 NotBlank
    private String genderType;
    
    // 移除 buildingId 和 floorNumber
}