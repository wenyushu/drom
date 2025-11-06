package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 房间电表资产实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("dorm_meter_electric")
public class DormMeterElectric extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 电表ID */
    @TableId(value = "meter_id", type = IdType.AUTO)
    private Long meterId;
    
    /** 关联房间ID */
    private Long roomId;
    
    /** 电表编号/资产编号 */
    private String meterCode;
    
    /** 型号规格 */
    private String model;
    
    /** 安装日期 */
    private LocalDate installationDate; // 使用 LocalDate 映射数据库的 DATE 类型
    
    /** 状态 (0: 正常, 1: 故障, 2: 停用) */
    private String status;
    
    // ---- 非数据库字段 (用于VO展示) ----
    
    /** 房间门牌号 */
    @TableField(exist = false)
    private String roomNumber;
}