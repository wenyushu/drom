package com.dormitory.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调宿申请查询 DTO (管理端)
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RoomChangeQueryDTO extends PageDTO {
    
    // 未来可在此处添加筛选条件
    // private String studentName;
    // private String status;
}