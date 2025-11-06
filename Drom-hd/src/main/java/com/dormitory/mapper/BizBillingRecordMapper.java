package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.BizBillingRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 水电费账单记录 Mapper 接口
 */
@Mapper
public interface BizBillingRecordMapper extends BaseMapper<BizBillingRecord> {
}