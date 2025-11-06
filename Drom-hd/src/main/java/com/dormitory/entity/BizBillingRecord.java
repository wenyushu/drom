package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 水电费账单记录实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_billing_record")
public class BizBillingRecord extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;
    
    /** 关联房间ID */
    @NotNull private Long roomId;
    
    /** 计量类型 (1: 电, 2: 水) */
    private String meterType;
    
    /** 计费周期开始日期 */
    private LocalDate cycleStartDate;
    
    /** 计费周期结束日期 */
    private LocalDate cycleEndDate;
    
    /** 本周期消耗量 (度/吨) */
    private BigDecimal unitConsumed;
    
    /** 总金额 (元) */
    private BigDecimal totalAmount;
    
    /** 是否已支付 (0: 否, 1: 是) */
    private Integer isPaid;
    
    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidTime;
    
    /** 账单状态 (0: 未出账, 1: 已出账, 2: 已支付, 3: 逾期) */
    private String status;
    
    // ---- 非数据库字段 (用于VO展示) ----
    
    /** 房间号 */
    @TableField(exist = false)
    private String roomNumber;
    
    /** 楼栋名 */
    @TableField(exist = false)
    private String buildingName;
}