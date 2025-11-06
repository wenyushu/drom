package com.dormitory.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学生查询参数 DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StudentQueryDTO extends PageDTO {
    
    /** 学号 */
    private String stuNumber;
    
    /** 姓名/昵称 (需要联查 sys_user) */
    private String nickname;
    
    /** 院系ID */
    private Long departmentId;
    
    /** 专业名称 */
    private String majorName;
    
    /** 年级级别 */
    private Integer currentGradeLevel;
    
    /** 校区ID */
    private Long currentCampusId;
    
    /** 学籍状态 */
    private String academicStatus;
}