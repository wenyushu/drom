package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 学生调宿申请实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_room_change_request")
public class BizRoomChangeRequest extends BaseEntity { // 仍然继承 BaseEntity (用于 createBy/updateBy)
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "request_id", type = IdType.AUTO)
    private Long requestId;
    
    /** 申请学生ID (FK: stu_student) */
    @NotNull
    private Long studentId;
    
    /** 当前床位ID (FK: dorm_bed) */
    @NotNull
    private Long currentBedId;
    
    /** 目标床位ID (可选) */
    private Long targetBedId;
    
    /** 申请原因 */
    @NotBlank
    private String reason;
    
    /** 状态 (0: 待审核, 1: 辅导员批准, 2: 驳回, 3: 已执行) */
    private String status;
    
    /** 【修正】提交时间 (对应数据库 submit_time) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;
    
    /** 审批人用户ID (FK: sys_user) */
    private Long approvalBy;
    
    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvalTime;
    
    /** 审批意见/备注 */
    private String approvalOpinion;
    
    /** 【修正】完工时间 (对应数据库 finish_time) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;
    
    // --- 非数据库字段 (用于VO) ---
    @TableField(exist = false)
    private String studentName;
    @TableField(exist = false)
    private String studentUsername;
    @TableField(exist = false)
    private String currentBedInfo;
    @TableField(exist = false)
    private String targetBedInfo;
    @TableField(exist = false)
    private String approvalByName;
}