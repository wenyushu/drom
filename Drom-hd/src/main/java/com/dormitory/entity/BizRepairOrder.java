package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 宿舍报修工单实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("biz_repair_order")
public class BizRepairOrder extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;
    
    /** 报修房间ID */
    private Long roomId;
    
    /** 关联的资产ID (dorm_room_asset.asset_id) */
    private Long assetId;
    
    /** 申请人ID */
    private Long applicantUserId;
    
    /** 报修标题 */
    private String orderTitle;
    
    /** 损坏详情描述 */
    private String description;
    
    /** 联系电话/方式 */
    private String contactInfo;
    
    /** 工单状态 (0: 待分配, 1: 处理中, 2: 已完成, 3: 无法修复) */
    private String orderStatus;
    
    /** 提交时间 */
    private LocalDateTime submitTime;
    
    /** 分配的维修人员ID */
    private Long handlerUserId;
    
    /** 完工时间 */
    private LocalDateTime finishTime;
    
    /** 维修结果总结 */
    private String repairResult;
    
    // ---- 非数据库字段 (用于VO展示) ----
    
    /** 房间号+楼栋名 */
    @TableField(exist = false)
    private String roomFullName;
    
    /** 资产名称 */
    @TableField(exist = false)
    private String assetName;
    
    /** 申请人姓名 */
    @TableField(exist = false)
    private String applicantName;
}