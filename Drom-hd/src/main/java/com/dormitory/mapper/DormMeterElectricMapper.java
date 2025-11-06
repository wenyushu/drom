package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.DormMeterElectric;
import org.apache.ibatis.annotations.Mapper;

/**
 * 电表 Mapper 接口
 */
@Mapper
public interface DormMeterElectricMapper extends BaseMapper<DormMeterElectric> {
}