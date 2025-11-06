package com.dormitory.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教职工信息查询参数 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StaffInfoQueryDTO extends PageDTO {
    
    /** 按工号 (username) 查询 */
    private String username;
    
    /** 按姓名/昵称 (realName/nickname) 查询 */
    private String nickname;
    
    /** 部门 ID */
    private Long departmentId;
    
    /** 职位/职称 */
    private String jobTitle;
    
    /** 住宿意愿 (0: 校外, 1: 校内) */
    private Integer isOnCampusResident;
}