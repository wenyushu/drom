package com.dormitory.vo;

import com.dormitory.entity.BizRoomChangeRequest; // 导入实体
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调宿申请视图对象 (包含关联信息)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomChangeRequestVO extends BizRoomChangeRequest {
    
    // 继承了 BizRoomChangeRequest 中的所有字段，
    // 包括 @TableField(exist = false) 标记的
    // studentName, studentUsername, currentBedInfo, targetBedInfo, approvalByName
}