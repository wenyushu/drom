package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List; // 导入 List 供后续使用

/**
 * 床位信息实体类
 * 补充 M-M 查询字段
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("dorm_bed")
public class DormBed extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 床位ID (主键) */
    @TableId(value = "bed_id", type = IdType.AUTO)
    private Long bedId;
    
    /** 所属房间ID */
    private Long roomId;
    
    /** 床位编号 (A, B, C...) */
    private String bedNumber;
    
    /** 是否被占用 (0: 空闲, 1: 已占用) */
    private Integer isOccupied;
    
    /** 当前入住的用户ID (关联 sys_user) */
    private Long occupantUserId;
    
    /** 入住人类型 (1: 学生, 2: 教职工) */
    private String occupantType;
    
    // ---- 非数据库字段 (用于VO展示) ----
    
    /** 房间门牌号 */
    @TableField(exist = false)
    private String roomNumber;
    
    /** 房间所属楼栋名称 */
    @TableField(exist = false)
    private String buildingName;
    
    // --- 【【【【【 修复：新增 M-M 查询字段 】】】】】 ---
    
    /** 房间容量 (由 DormAllocationMapper 填充) */
    @TableField(exist = false)
    private Integer roomCapacity;
    
    /** 房间已住人数 (由 DormAllocationMapper 填充) */
    @TableField(exist = false)
    private Integer roomOccupiedBeds;
    
    /** 楼栋ID (由 DormAllocationMapper 填充) */
    @TableField(exist = false)
    private Long buildingId;
}