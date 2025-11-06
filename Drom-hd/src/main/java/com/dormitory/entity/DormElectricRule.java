package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;
import java.util.Objects; // 导入 Objects 用于 equals 比较

/**
 * 宿舍用电规则实体类
 */
@EqualsAndHashCode(callSuper = true) // 继承 BaseEntity
@Data
@TableName("dorm_electric_rule")
public class DormElectricRule extends BaseEntity { // 继承 BaseEntity
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 规则ID */
    @TableId(value = "rule_id", type = IdType.AUTO)
    private Long ruleId;
    
    /** 关联房间ID (可选，优先级高于楼栋) */
    private Long roomId;
    
    /** 关联楼栋ID (可选) */
    private Long buildingId;
    
    /** 空调电路功率限制 (W) */
    private Integer acPowerLimit;
    
    /** 普通电路功率限制 (W) */
    private Integer generalPowerLimit;
    
    /** 规则生效日期 */
    @NotNull(message = "生效日期不能为空")
    private LocalDate startDate;
    
    
    // --- 非数据库字段 (用于VO展示) ---
    /** 房间门牌号 */
    @TableField(exist = false)
    private String roomNumber;
    
    /** 楼栋名称 */
    @TableField(exist = false)
    private String buildingName;
}