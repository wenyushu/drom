package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.DormElectricRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 宿舍用电规则 Mapper 接口
 */
@Mapper
public interface DormElectricRuleMapper extends BaseMapper<DormElectricRule> {
    // MyBatis-Plus 提供了基本的 CRUD，无需额外方法
}