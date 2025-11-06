package com.dormitory.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClassQueryDTO extends PageDTO {
    private String className;
    private Long departmentId;
    private String majorName;
    private Long counselorUserId;
    private String enrollmentYear;
}