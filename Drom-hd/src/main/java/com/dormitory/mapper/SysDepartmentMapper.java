package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.SysDepartment; // 确保 SysDepartment 实体已创建
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门/院系 Mapper 接口
 */
@Mapper
public interface SysDepartmentMapper extends BaseMapper<SysDepartment> {
}