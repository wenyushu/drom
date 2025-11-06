package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.BizMeterReading;
import org.apache.ibatis.annotations.Mapper;

/**
 * 水/电表读数记录 Mapper 接口
 */
@Mapper
public interface BizMeterReadingMapper extends BaseMapper<BizMeterReading> {
}