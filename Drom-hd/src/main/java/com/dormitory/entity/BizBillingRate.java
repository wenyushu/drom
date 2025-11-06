package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 计费费率配置实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_billing_rate")
public class BizBillingRate extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "rate_id", type = IdType.AUTO)
    private Long rateId;
    
    /** 费率名称 (如：学生电费-阶梯1) */
    @NotBlank(message = "费率名称不能为空")
    private String rateName;
    
    /** 计量类型 (1: 电, 2: 水) */
    @NotBlank(message = "计量类型不能为空")
    private String meterType;
    
    /** 单价/基础费率 */
    @NotNull(message = "费率单价不能为空")
    @DecimalMin(value = "0.001", message = "费率必须大于零")
    private BigDecimal unitPrice;
    
    /** 阶梯阈值 (例如，水费超过此值应用下一阶梯) */
    private BigDecimal threshold;
    
    /** 生效日期 */
    @NotNull(message = "生效日期不能为空")
    private LocalDate validFrom;
}