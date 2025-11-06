package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 楼层实体类 (映射 dorm_floor)
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("dorm_floor")
public class DormFloor extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "floor_id", type = IdType.AUTO)
    private Long floorId;
    
    /** 所属楼栋ID (FK: dorm_building) */
    @NotNull(message = "所属楼栋不能为空")
    private Long buildingId;
    
    /** 楼层编号 (物理层数，如 1, 2, 3) */
    @NotNull(message = "楼层编号不能为空")
    private Integer floorNumber;
    
    /** 楼层显示名称 (例如: 一层, G层) */
    private String floorName;
    
    /** 该楼层房间数 (冗余) */
    private Integer roomCount;
    
    /** 该楼层负责人ID (精细化管理, 可选) */
    private Long managerId;
    
    // --- 非数据库字段 (用于VO展示) ---
    @TableField(exist = false)
    private String buildingName; // 所属楼栋名称
    @TableField(exist = false)
    private String managerName; // 负责人姓名
}