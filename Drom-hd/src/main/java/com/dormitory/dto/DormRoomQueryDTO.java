package com.dormitory.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DormRoomQueryDTO extends PageDTO {
    
    /** 按楼栋 ID 查询 */
    private Long buildingId;
    
    /** 按楼层 ID 查询 */
    private Long floorId;
    
    /** 房间门牌号 */
    private String roomNumber;
    
    /** 房间用途 */
    private String roomPurposeType;
    
    /** 房间状态 */
    private String roomStatus;
}