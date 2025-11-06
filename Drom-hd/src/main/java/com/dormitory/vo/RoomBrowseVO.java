package com.dormitory.vo;

import com.dormitory.entity.DormRoom;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoomBrowseVO extends DormRoom {
    // 这个 VO 继承 DormRoom
    // 而 DormRoom 已经包含了需要的所有字段
    // (roomNumber, roomPurposeType, roomCapacity, occupiedBeds)
}