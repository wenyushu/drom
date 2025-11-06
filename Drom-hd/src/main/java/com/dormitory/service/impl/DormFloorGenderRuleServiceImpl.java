package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.entity.DormBuilding;
import com.dormitory.entity.DormFloor;
import com.dormitory.entity.DormFloorGenderRule;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.DormBuildingMapper;
import com.dormitory.mapper.DormFloorGenderRuleMapper;
import com.dormitory.mapper.DormFloorMapper;
import com.dormitory.service.IDormFloorGenderRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 混合楼栋楼层性别规则业务服务实现类
 */
@Service
public class DormFloorGenderRuleServiceImpl extends ServiceImpl<DormFloorGenderRuleMapper, DormFloorGenderRule> implements IDormFloorGenderRuleService {
    
    @Autowired private DormBuildingMapper buildingMapper;
    @Autowired private DormFloorMapper floorMapper;
    
    @Override
    public List<DormFloorGenderRule> getRulesByFloorId(Long floorId) { // <-- 【修正】
        return this.list(new LambdaQueryWrapper<DormFloorGenderRule>()
                .eq(DormFloorGenderRule::getFloorId, floorId));
    }
    
    @Override
    public void saveOrUpdateFloorRule(DormFloorGenderRule rule) {
        // 1. 校验楼层
        DormFloor floor = floorMapper.selectById(rule.getFloorId());
        if (floor == null) {
            throw new BusinessException("指定的楼层不存在");
        }
        
        // 2. 校验楼栋是否为混合楼栋
        DormBuilding building = buildingMapper.selectById(floor.getBuildingId());
        if (building == null || !"2".equals(building.getGenderType())) { // '2' 代表混合
            throw new BusinessException("该楼栋不是混合性别楼栋，无法设置楼层规则");
        }
        
        // 3. 校验性别值
        if (!"0".equals(rule.getGenderType()) && !"1".equals(rule.getGenderType())) {
            throw new BusinessException("性别限制值无效 (必须为 '0' 或 '1')");
        }
        
        // 4. 校验唯一性 (使用 saveOrUpdate，依赖数据库 uk_floor_id 唯一约束)
        boolean success = this.saveOrUpdate(rule);
        if (!success) {
            LambdaQueryWrapper<DormFloorGenderRule> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DormFloorGenderRule::getFloorId, rule.getFloorId());
            if (this.exists(wrapper)) {
                throw new BusinessException("楼层 " + floor.getFloorName() + " 已存在性别规则，请勿重复添加。");
            } else {
                throw new BusinessException("楼层性别规则保存失败，请检查参数。");
            }
        }
    }
    
    @Override
    public void deleteFloorRuleById(Long ruleId) {
        if (!this.removeById(ruleId)) {
            throw new BusinessException("删除失败，规则 ID 可能不存在");
        }
    }
}