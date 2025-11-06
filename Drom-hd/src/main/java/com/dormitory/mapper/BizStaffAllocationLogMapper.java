package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.BizStaffAllocationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教职工/后勤分配日志 Mapper 接口
 */
@Mapper
public interface BizStaffAllocationLogMapper extends BaseMapper<BizStaffAllocationLog> {
}