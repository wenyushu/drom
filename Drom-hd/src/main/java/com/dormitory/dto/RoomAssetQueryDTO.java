package com.dormitory.dto;

import lombok.Data;

/**
 * 房间固定资产查询参数 DTO
 */
@Data
public class RoomAssetQueryDTO extends PageDTO {
    
    /** 所属房间ID */
    private Long roomId;
    
    /** 资产名称 */
    private String assetName;
    
    /** 资产类型 */
    private String assetType;
    
    /** 资产状态 */
    private String status;
}