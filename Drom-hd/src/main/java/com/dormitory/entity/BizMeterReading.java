package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 水/电表读数记录实体类
 */
@EqualsAndHashCode(callSuper = true) // 继承 BaseEntity
@Data
@TableName("biz_meter_reading")
public class BizMeterReading extends BaseEntity { // 继承 BaseEntity
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "reading_id", type = IdType.AUTO)
    private Long readingId;
    
    /** 关联的电表/水表资产ID */
    @NotNull(message = "关联的表计ID不能为空")
    private Long meterId;
    
    /** 计量类型 (1: 电, 2: 水) */
    @NotNull(message = "计量类型不能为空")
    private String meterType;
    
    /** 本次采集的累计读数 */
    @NotNull(message = "读数不能为空")
    private BigDecimal readingValue;
    
    /** 读数采集时间 */
    @NotNull(message = "读数时间不能为空")
    private LocalDateTime readingDate;
    
    /** 关联的计费周期ID (可选) */
    private Long chargeCycleId;
    
    // ---- 非数据库字段 (用于VO展示) ----
    
    /** 表计编号 (meterCode) */
    @TableField(exist = false)
    private String meterCode;
    
    /** 房间号 */
    @TableField(exist = false)
    private String roomNumber;
}