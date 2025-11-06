package com.dormitory.dto;

import lombok.Data;
import lombok.EqualsAndHashCode; // 引入继承所需的注解
import java.time.LocalDate;

/**
 * 学生离校/留校状态查询参数 DTO
 */
@EqualsAndHashCode(callSuper = true) // 继承 PageDTO
@Data
public class LeaveStatusQueryDTO extends PageDTO {
    
    /** 学生ID */
    private Long studentId;
    
    /** 学号 (用于查询) */
    private String stuNumber;
    
    /** 状态类型 */
    private String statusType;
    
    /** 是否在寝室 */
    private Integer isInDorm;
    
    /** 日期范围 - 开始 */
    private LocalDate startDate;
    
    /** 日期范围 - 结束 */
    private LocalDate endDate;
}