package com.dormitory.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.Data;

/**
 * 楼栋查询请求参数 DTO
 */
@Data
public class DormBuildingQueryDTO extends PageDTO {
    
    /** 楼栋名称 */
    private String buildingName;
    
    /** 所属校区ID */
    private Long campusId;
    
    /** 性别限制 */
    private String genderType;
}