package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.DormFloorGenderRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 混合楼栋楼层性别规则 Mapper 接口
 */
@Mapper
public interface DormFloorGenderRuleMapper extends BaseMapper<DormFloorGenderRule> {
}