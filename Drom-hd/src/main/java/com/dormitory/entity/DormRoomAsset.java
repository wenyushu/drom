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
 * 房间固定资产实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("dorm_room_asset")
public class DormRoomAsset extends BaseEntity {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "asset_id", type = IdType.AUTO)
    private Long assetId;
    
    /** 所属房间ID */
    private Long roomId;
    
    /** 资产名称 (如：空调A, 衣柜1) */
    private String assetName;
    
    /** 资产类型 (KT-空调, YG-衣柜, WS-卫生, CD-插座, MT-马桶, LX-淋浴, WL-网络, WX-窗户, MM-门, SL-晾衣绳, RP-热水管道) */
    private String assetType;
    
    /** 资产序列号 (如有) */
    private String serialNumber;
    
    /** 资产状态 (0: 正常, 1: 损坏/待修, 2: 报废) */
    private String status;
    
    /** 采购日期 */
    private LocalDate purchaseDate;
    
    /** 备注 */
    private String remark;
    
    // ---- 非数据库字段 (用于VO展示) ----
    
    /** 房间门牌号 */
    @TableField(exist = false)
    private String roomNumber;
}