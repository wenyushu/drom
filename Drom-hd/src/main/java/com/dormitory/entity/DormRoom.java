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

/**
 * 宿舍房间实体类 (V5 重构版)
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("dorm_room")
public class DormRoom extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "room_id", type = IdType.AUTO)
    private Long roomId;
    
    /** 【修正】所属楼层ID (FK: dorm_floor) */
    @NotNull(message = "所属楼层ID不能为空")
    private Long floorId;
    
    /** 房间门牌号 (如：101) */
    @NotBlank(message = "房间门牌号不能为空")
    private String roomNumber;
    
    /** 房间用途 (房间用途 (00:学生宿舍, 01:宿管用房, 02:物资房, 03:教职工用房, 04:单人间, 05:预留房)) */
    @NotBlank(message = "房间用途不能为空")
    private String roomPurposeType;
    
    /** 房间的固定物理容量 */
    @NotNull(message = "房间容量不能为空")
    private Integer roomCapacity;
    
    /** 房间状态 (0: 正常, 1: 待维修/封禁, 2: 重新装修/长期禁用) */
    private String roomStatus; // 默认值在数据库 DDL 中设置
    
    /** 已入住床位数 */
    private Integer occupiedBeds; // 默认值在数据库 DDL 中设置
    
    /** 备注 */
    private String remark;
    
    // --- 非数据库字段 (用于VO展示) ---
    @TableField(exist = false)
    private String floorName; // 楼层名
    @TableField(exist = false)
    private Integer floorNumber; // 楼层号
    @TableField(exist = false)
    private String buildingName; // 楼栋名
    @TableField(exist = false)
    private Long buildingId; // 楼栋ID
}