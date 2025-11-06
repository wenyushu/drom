package com.dormitory.dto;

import lombok.Data;

/**
 * 基础分页查询参数 DTO
 * 这个是所有分页查询的基础父类
 */
@Data
public class PageDTO {
    
    /** 当前页码，默认从 1 开始 */
    private long current = 1;
    
    /** 每页大小，默认 10 */
    private long size = 10;
}