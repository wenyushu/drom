package com.dormitory.dto;

import lombok.Data;

/**
 * 用户查询请求参数 DTO
 */
@Data
public class UserQueryDTO {
    
    /** 用户名/账号 */
    private String username;
    
    /** 昵称/姓名 */
    private String nickname;
    
    /** 关联角色ID */
    private Long roleId;
    
    /** 账号状态 (0: 正常, 1: 禁用) */
    private Integer status;
    
    /** 当前页码 */
    private long current = 1;
    
    /** 每页大小 */
    private long size = 10;
}