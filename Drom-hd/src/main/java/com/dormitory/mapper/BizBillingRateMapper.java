package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.BizBillingRate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 计费费率配置 Mapper 接口
 */
@Mapper
public interface BizBillingRateMapper extends BaseMapper<BizBillingRate> {
}