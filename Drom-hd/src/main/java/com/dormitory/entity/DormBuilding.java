package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 楼栋实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("dorm_building")
public class DormBuilding extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "building_id", type = IdType.AUTO)
    private Long buildingId;
    
    /** 楼栋名称 */
    private String buildingName;
    
    /** 所属校区ID */
    private Long campusId;
    
    /** 总楼层数 */
    private Integer totalFloors;
    
    /** 性别限制 (0: 男, 1: 女, 2: 混合) */
    private String genderType;
    
    /** 用途类型 (0: 学生寝室, 1: 教师/职工宿舍) */
    private String purposeType;
    
    /** 分配的宿管用户ID */
    private Long managerId;
    
    /** 楼栋状态 (0: 正常, 1: 维修/关闭) */
    private Integer status;
    
    // ---- 非数据库字段 (用于VO展示) ----
    
    /** 校区名称 */
    @TableField(exist = false)
    private String campusName;
    
    /** 宿管姓名 */
    @TableField(exist = false)
    private String managerName;
}