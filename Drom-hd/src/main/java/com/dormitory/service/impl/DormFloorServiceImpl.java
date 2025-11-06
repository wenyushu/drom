package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.DormFloorQueryDTO;
import com.dormitory.entity.DormBuilding;
import com.dormitory.entity.DormFloor;
import com.dormitory.entity.DormFloorGenderRule;
import com.dormitory.entity.DormRoom;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.DormBuildingMapper;
import com.dormitory.mapper.DormFloorGenderRuleMapper;
import com.dormitory.mapper.DormFloorMapper;
import com.dormitory.mapper.DormRoomMapper;
import com.dormitory.service.IDormFloorService;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DormFloorServiceImpl extends ServiceImpl<DormFloorMapper, DormFloor> implements IDormFloorService {
    
    @Autowired private DormBuildingMapper buildingMapper;
    
    @Autowired private DormRoomMapper roomMapper;
    
    @Autowired private DormFloorGenderRuleMapper floorGenderRuleMapper;
    
    
    @Override
    public Page<DormFloor> selectFloorPage(DormFloorQueryDTO queryDTO) {
        // 1. 创建 Page 对象
        Page<DormFloor> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 2. 构建查询条件
        LambdaQueryWrapper<DormFloor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getBuildingId() != null, DormFloor::getBuildingId, queryDTO.getBuildingId());
        wrapper.orderByAsc(DormFloor::getBuildingId).orderByAsc(DormFloor::getFloorNumber);
        
        // 3. 执行分页查询
        this.page(page, wrapper);
        
        // 4. 填充楼栋名
        if (!page.getRecords().isEmpty()) {
            fillBuildingNames(page.getRecords());
        }
        return page;
    }
    
    
    @Override
    public List<DormFloor> getFloorsByBuildingId(Long buildingId) {
        if (buildingId == null) {
            return Collections.emptyList();
        }
        List<DormFloor> floors = this.list(new LambdaQueryWrapper<DormFloor>()
                .eq(DormFloor::getBuildingId, buildingId)
                .orderByAsc(DormFloor::getFloorNumber));
        
        fillBuildingNames(floors);
        return floors;
    }
    
    
    @Override
    public void addFloor(DormFloor floor) {
        // 1. 校验楼栋是否存在
        DormBuilding building = buildingMapper.selectById(floor.getBuildingId());
        if (building == null) {
            throw new BusinessException("所属楼栋不存在");
        }
        // 2. 校验楼层是否超出楼栋总层数
        if (floor.getFloorNumber() > building.getTotalFloors()) {
            throw new BusinessException("楼层号 " + floor.getFloorNumber() + " 超出了楼栋总层数 " + building.getTotalFloors());
        }
        // 3. 校验唯一性 (已通过 uk_building_floor_number 约束)
        this.save(floor);
    }
    
    
    @Override
    @Transactional
    public void deleteFloor(Long floorId) {
        // 1. 校验楼层下是否有房间
        if (roomMapper.exists(new LambdaQueryWrapper<DormRoom>().eq(DormRoom::getFloorId, floorId))) {
            throw new BusinessException("删除失败：该楼层下仍有关联的房间！");
        }
        
        // 5. 校验楼层性别规则表
        if (floorGenderRuleMapper.exists(new LambdaQueryWrapper<DormFloorGenderRule>()
                .eq(DormFloorGenderRule::getFloorId, floorId))) {
            throw new BusinessException("删除失败：请先移除该楼层设置的[混合楼栋性别规则]！");
        }
        
        this.removeById(floorId);
    }
    
    /**
     * 辅助方法：填充楼栋名称
     */
    private void fillBuildingNames(List<DormFloor> floors) {
        if (CollUtil.isEmpty(floors)) {
            return;
        }
        List<Long> buildingIds = floors.stream().map(DormFloor::getBuildingId).distinct().collect(Collectors.toList());
        Map<Long, String> buildingNameMap = buildingIds.isEmpty() ? Collections.emptyMap() :
                buildingMapper.selectBatchIds(buildingIds).stream().collect(Collectors.toMap(DormBuilding::getBuildingId, DormBuilding::getBuildingName));
        
        floors.forEach(floor -> {
            floor.setBuildingName(buildingNameMap.get(floor.getBuildingId()));
        });
    }
}