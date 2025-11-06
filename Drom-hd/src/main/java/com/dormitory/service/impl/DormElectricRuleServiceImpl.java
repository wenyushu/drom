package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.entity.DormBuilding; // 导入 DormBuilding
import com.dormitory.entity.DormElectricRule;
import com.dormitory.entity.DormRoom; // 导入 DormRoom
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.DormBuildingMapper; // 导入 DormBuildingMapper
import com.dormitory.mapper.DormElectricRuleMapper;
import com.dormitory.mapper.DormRoomMapper; // 导入 DormRoomMapper
import com.dormitory.service.IDormElectricRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 导入 Transactional

import java.util.Collections; // 导入 Collections
import java.util.List;
import java.util.Map;
import java.util.Objects; // 导入 Objects
import java.util.stream.Collectors; // 导入 Collectors

/**
 * 宿舍用电规则业务服务实现类 (最终版)
 */
@Service
public class DormElectricRuleServiceImpl extends ServiceImpl<DormElectricRuleMapper, DormElectricRule> implements IDormElectricRuleService {
    
    // 注入依赖
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private DormBuildingMapper buildingMapper;
    
    /**
     * 查询规则列表 (含房间号和楼栋名)
     */
    @Override
    public List<DormElectricRule> selectRuleList(Long roomId, Long buildingId) {
        LambdaQueryWrapper<DormElectricRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, DormElectricRule::getRoomId, roomId)
                .eq(buildingId != null, DormElectricRule::getBuildingId, buildingId)
                .orderByAsc(DormElectricRule::getStartDate); // 按生效日期排序
        
        List<DormElectricRule> rules = this.list(wrapper);
        
        // 填充房间号和楼栋名 (优化 N+1)
        if (!rules.isEmpty()) {
            List<Long> roomIds = rules.stream().map(DormElectricRule::getRoomId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            List<Long> buildingIds = rules.stream().map(DormElectricRule::getBuildingId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            // 也包含通过 roomId 查到的 buildingId
            if (!roomIds.isEmpty()) {
                List<Long> buildingIdsFromRooms = roomMapper.selectBatchIds(roomIds).stream().map(DormRoom::getBuildingId).distinct().toList();
                buildingIds.addAll(buildingIdsFromRooms);
                buildingIds = buildingIds.stream().distinct().collect(Collectors.toList());
            }
            
            
            Map<Long, String> roomNumMap = roomIds.isEmpty() ? Collections.emptyMap() :
                    roomMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(DormRoom::getRoomId, DormRoom::getRoomNumber));
            Map<Long, String> buildingNameMap = buildingIds.isEmpty() ? Collections.emptyMap() :
                    buildingMapper.selectBatchIds(buildingIds).stream().collect(Collectors.toMap(DormBuilding::getBuildingId, DormBuilding::getBuildingName));
            
            rules.forEach(rule -> {
                // 填充房间号
                rule.setRoomNumber(roomNumMap.get(rule.getRoomId()));
                // 填充楼栋名 (优先通过 buildingId，其次通过 roomId 关联)
                if (rule.getBuildingId() != null) {
                    rule.setBuildingName(buildingNameMap.get(rule.getBuildingId()));
                } else if (rule.getRoomId() != null && roomNumMap.containsKey(rule.getRoomId())) {
                    // 通过 roomId 找到对应的 buildingId 来获取楼栋名 (逻辑需要调整，或直接在 room 实体加 buildingId)
                    // 简化：如果 buildingId 为空，则不填充楼栋名
                }
            });
        }
        return rules;
    }
    
    /**
     * 新增或更新用电规则 (含校验)
     */
    @Override
    @Transactional // 确保操作原子性
    public void saveOrUpdateRule(DormElectricRule rule) {
        // 1. 基础校验：至少关联房间或楼栋其一
        if (rule.getRoomId() == null && rule.getBuildingId() == null) {
            throw new BusinessException("规则必须关联到具体房间或楼栋");
        }
        // 2. 校验功率限制值是否合理 (例如 >= 0)
        if ((rule.getAcPowerLimit() != null && rule.getAcPowerLimit() < 0) ||
                (rule.getGeneralPowerLimit() != null && rule.getGeneralPowerLimit() < 0)) {
            throw new BusinessException("功率限制值不能为负数");
        }
        
        // 3. 校验 roomId 或 buildingId 是否存在
        if (rule.getRoomId() != null && !roomMapper.exists(new LambdaQueryWrapper<DormRoom>().eq(DormRoom::getRoomId, rule.getRoomId()))) {
            throw new BusinessException("关联的房间ID不存在");
        }
        if (rule.getBuildingId() != null && !buildingMapper.exists(new LambdaQueryWrapper<DormBuilding>().eq(DormBuilding::getBuildingId, rule.getBuildingId()))) {
            throw new BusinessException("关联的楼栋ID不存在");
        }
        
        // 4. 业务规则：一个房间/楼栋在某个生效日期后只能有一条规则（唯一性校验）
        LambdaQueryWrapper<DormElectricRule> uniqueCheckWrapper = new LambdaQueryWrapper<>();
        uniqueCheckWrapper.eq(rule.getRoomId() != null, DormElectricRule::getRoomId, rule.getRoomId())
                .eq(rule.getBuildingId() != null, DormElectricRule::getBuildingId, rule.getBuildingId())
                .ge(DormElectricRule::getStartDate, rule.getStartDate()); // 生效日期大于等于当前
        if (rule.getRuleId() != null) { // 如果是更新操作，排除自身
            uniqueCheckWrapper.ne(DormElectricRule::getRuleId, rule.getRuleId());
        }
        if (this.exists(uniqueCheckWrapper)) {
            throw new BusinessException("已存在相同或更新生效日期的规则，请勿重复添加");
        }
        
        
        // 5. 保存或更新
        boolean success = this.saveOrUpdate(rule);
        if (!success) {
            // saveOrUpdate 内部处理了 insert/update，如果失败通常是底层异常
            throw new BusinessException("用电规则保存失败，请检查参数或联系管理员");
        }
    }
    
    /**
     * 根据规则ID删除规则
     */
    @Override
    public void deleteRuleById(Long ruleId) {
        if (ruleId == null) {
            throw new BusinessException("规则ID不能为空");
        }
        boolean removed = this.removeById(ruleId);
        if (!removed) {
            throw new BusinessException("删除失败，规则ID可能不存在");
        }
    }
}