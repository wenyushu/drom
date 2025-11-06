package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.DormBuilding;
import org.apache.ibatis.annotations.Mapper;

/**
 * 楼栋 Mapper 接口
 */
@Mapper
public interface DormBuildingMapper extends BaseMapper<DormBuilding> {
}