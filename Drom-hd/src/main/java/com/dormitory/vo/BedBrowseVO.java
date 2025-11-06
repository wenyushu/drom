package com.dormitory.vo;

import lombok.Data;

/**
 * 床位浏览视图 (包含住户姓名，有权限控制)
 */
@Data
public class BedBrowseVO {
    
    private Long bedId;
    private Long roomId;
    private String bedNumber; // "A", "B"
    private Integer isOccupied; // 0 或 1
    
    // --- 敏感信息 ---
    
    /** 住户姓名 (根据权限显示 "李毅乐" 或 "已占用") */
    private String occupantName;
    
    /** 住户性别 (根据权限显示 "0" 或 "1") */
    private String occupantSex;
}