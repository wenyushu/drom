package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.DormBed;
import org.apache.ibatis.annotations.Mapper;

/**
 * 床位 Mapper 接口
 */
@Mapper
public interface DormBedMapper extends BaseMapper<DormBed> {
}