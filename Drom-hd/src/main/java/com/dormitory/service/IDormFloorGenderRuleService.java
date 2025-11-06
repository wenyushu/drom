package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.entity.DormFloorGenderRule;

import java.util.List;

/**
 * 混合楼栋楼层性别规则业务服务接口
 */
public interface IDormFloorGenderRuleService extends IService<DormFloorGenderRule> {
    
    /**
     * 根据楼层ID获取规则
     * @param floorId 楼层ID
     * @return 规则列表
     */
    List<DormFloorGenderRule> getRulesByFloorId(Long floorId);
    
    /**
     * 保存或更新楼层规则 (含业务校验)
     * @param rule 楼层规则对象
     */
    void saveOrUpdateFloorRule(DormFloorGenderRule rule);
    
    /**
     * 根据规则ID删除楼层规则
     * @param ruleId 规则ID
     */
    void deleteFloorRuleById(Long ruleId);
}