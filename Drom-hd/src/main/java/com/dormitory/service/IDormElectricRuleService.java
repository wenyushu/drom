package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.entity.DormElectricRule;

import java.util.List; // 导入 List

/**
 * 宿舍用电规则业务服务接口
 */
public interface IDormElectricRuleService extends IService<DormElectricRule> {
    
    /**
     * 查询规则列表 (含房间号和楼栋名)
     * @param roomId 房间ID (可选)
     * @param buildingId 楼栋ID (可选)
     * @return 规则列表
     */
    List<DormElectricRule> selectRuleList(Long roomId, Long buildingId);
    
    /**
     * 新增或更新用电规则 (含校验)
     * @param rule 规则对象
     */
    void saveOrUpdateRule(DormElectricRule rule);
    
    /**
     * 根据规则ID删除规则
     * @param ruleId 规则ID
     */
    void deleteRuleById(Long ruleId);
}