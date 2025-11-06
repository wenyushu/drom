package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.BizElectricAlert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 违规用电告警记录 Mapper 接口
 */
@Mapper
public interface BizElectricAlertMapper extends BaseMapper<BizElectricAlert> {
}