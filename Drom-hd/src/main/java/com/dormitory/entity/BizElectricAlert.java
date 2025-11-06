package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 违规用电告警记录实体类
 */
@EqualsAndHashCode(callSuper = true) // 继承 BaseEntity
@Data
@TableName("biz_electric_alert")
public class BizElectricAlert extends BaseEntity { // 继承 BaseEntity
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "alert_id", type = IdType.AUTO)
    private Long alertId;
    
    /** 发生告警的房间ID */
    @NotNull(message = "房间ID不能为空")
    private Long roomId;
    
    /** 告警类型 (1: 超功率跳闸, 2: 违规电器告警) */
    @NotNull(message = "告警类型不能为空")
    private String alertType;
    
    /** 实时测得的功率值 (W, 可选) */
    private Integer measuredValue;
    
    /** 告警发生时间 */
    @NotNull(message = "告警时间不能为空")
    private LocalDateTime alertTime;
    
    /** 是否已处理/复位 (0: 否, 1: 是) */
    private Integer isResolved;
    
    // ---- 非数据库字段 (用于VO展示) ----
    
    /** 房间号 */
    @TableField(exist = false)
    private String roomNumber;
}