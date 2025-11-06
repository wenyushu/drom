package com.dormitory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 角色新增/修改请求参数 DTO
 */
@Data
public class RoleAddUpdateDTO {
    
    /** 角色ID (修改时必传) */
    private Long roleId;
    
    /** 角色名称 */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    
    /** 权限关键字 */
    @NotBlank(message = "权限关键字不能为空")
    private String roleKey;
    
    /** 状态 (0: 正常, 1: 停用) */
    @NotNull(message = "角色状态不能为空")
    private Integer status;
    
    /** 备注 */
    private String remark;
    
    /** 关联的菜单/权限ID列表 */
    private List<Long> menuIds;
}