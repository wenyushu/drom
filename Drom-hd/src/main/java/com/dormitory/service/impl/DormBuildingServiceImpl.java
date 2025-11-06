package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.DormBuildingQueryDTO;
import com.dormitory.entity.DormBuilding;
import com.dormitory.entity.DormFloor; // <-- 【修复】导入 DormFloor
import com.dormitory.entity.DormRoom;
import com.dormitory.entity.SysCampus;
import com.dormitory.entity.SysUser;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.DormBuildingMapper;
import com.dormitory.mapper.DormFloorMapper;
import com.dormitory.mapper.DormRoomMapper;
import com.dormitory.mapper.SysUserMapper;
import com.dormitory.mapper.SysCampusMapper;
import com.dormitory.service.IDormBuildingService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 楼栋业务服务实现类 (V2)
 * 1. 修复 deleteBuildingByIds 的校验逻辑 (Room 关联 Floor)
 * 2. 补全新增/修改时的唯一性和外键校验
 * 3. 优化分页查询的 N+1 问题
 */
@Service
public class DormBuildingServiceImpl extends ServiceImpl<DormBuildingMapper, DormBuilding> implements IDormBuildingService {
    
    @Autowired
    private DormRoomMapper roomMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private SysCampusMapper campusMapper;
    @Autowired
    private DormFloorMapper floorMapper;
    
    /**
     * 分页查询楼栋列表 (包含关联的校区和宿管信息)
     */
    @Override
    public Page<DormBuilding> selectBuildingPage(DormBuildingQueryDTO queryDTO) {
        
        LambdaQueryWrapper<DormBuilding> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotEmpty(queryDTO.getBuildingName()), DormBuilding::getBuildingName, queryDTO.getBuildingName())
                .eq(queryDTO.getCampusId() != null, DormBuilding::getCampusId, queryDTO.getCampusId())
                .eq(StrUtil.isNotEmpty(queryDTO.getGenderType()), DormBuilding::getGenderType, queryDTO.getGenderType())
                .orderByAsc(DormBuilding::getBuildingId);
        
        Page<DormBuilding> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        
        // --- 优化：调用辅助方法填充，避免 N+1 ---
        if (CollUtil.isNotEmpty(page.getRecords())) {
            fillBuildingVOInfo(page.getRecords());
        }
        
        return page;
    }
    
    /**
     * 新增楼栋 (【修复】补充校验)
     */
    @Override
    @Transactional
    public void addBuilding(DormBuilding building) {
        // 1. 校验校区是否存在
        if (building.getCampusId() == null || !campusMapper.exists(new LambdaQueryWrapper<SysCampus>().eq(SysCampus::getCampusId, building.getCampusId()))) {
            throw new BusinessException("新增失败：所属校区不存在");
        }
        // 2. 校验宿管ID是否存在 (如果提供了)
        if (building.getManagerId() != null && !userMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserId, building.getManagerId()))) {
            throw new BusinessException("新增失败：指定的宿管ID不存在");
        }
        // 3. 校验楼栋名称在同一校区内是否唯一
        if (checkBuildingNameUnique(building.getBuildingName(), building.getCampusId(), null)) {
            throw new BusinessException("新增失败：该校区下已存在同名楼栋");
        }
        
        this.save(building);
    }
    
    /**
     * 修改楼栋 (【修复】补充校验)
     */
    @Override
    @Transactional
    public void updateBuilding(DormBuilding building) {
        if (building.getBuildingId() == null) {
            throw new BusinessException("楼栋ID不能为空");
        }
        // 校验楼栋ID存在性
        DormBuilding oldBuilding = this.getById(building.getBuildingId());
        if (oldBuilding == null) {
            throw new BusinessException("修改失败：楼栋不存在");
        }
        
        // 1. 校验校区是否存在 (如果修改了)
        if (building.getCampusId() != null && !building.getCampusId().equals(oldBuilding.getCampusId())) {
            if (!campusMapper.exists(new LambdaQueryWrapper<SysCampus>().eq(SysCampus::getCampusId, building.getCampusId()))) {
                throw new BusinessException("修改失败：所属校区不存在");
            }
        }
        // 2. 校验宿管ID是否存在 (如果修改了)
        if (building.getManagerId() != null && !building.getManagerId().equals(oldBuilding.getManagerId())) {
            if (!userMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserId, building.getManagerId()))) {
                throw new BusinessException("修改失败：指定的宿管ID不存在");
            }
        }
        // 3. 校验楼栋名称在同一校区内是否唯一 (排除自己)
        Long campusId = (building.getCampusId() != null) ? building.getCampusId() : oldBuilding.getCampusId();
        if (StrUtil.isNotEmpty(building.getBuildingName()) && !building.getBuildingName().equals(oldBuilding.getBuildingName())) {
            if (checkBuildingNameUnique(building.getBuildingName(), campusId, building.getBuildingId())) {
                throw new BusinessException("修改失败：该校区下已存在同名楼栋");
            }
        }
        
        this.updateById(building);
    }
    
    /**
     * 批量删除楼栋 (【修复】校验逻辑)
     */
    @Override
    @Transactional
    public void deleteBuildingByIds(Long[] buildingIds) {
        if (ArrayUtil.isEmpty(buildingIds)) {
            return;
        }
        
        for (Long buildingId : buildingIds) {
            // --- 修复：修正校验逻辑 ---
            // 1. 查找该楼栋下的所有楼层ID
            List<Long> floorIds = floorMapper.selectList(
                    new LambdaQueryWrapper<DormFloor>().eq(DormFloor::getBuildingId, buildingId)
            ).stream().map(DormFloor::getFloorId).collect(Collectors.toList());
            
            if (CollUtil.isNotEmpty(floorIds)) {
                // 2. 检查这些楼层下是否有房间
                Long roomCount = roomMapper.selectCount(
                        new LambdaQueryWrapper<DormRoom>().in(DormRoom::getFloorId, floorIds)
                );
                
                if (roomCount != null && roomCount > 0) {
                    throw new BusinessException("楼栋ID " + buildingId + " 下仍存在 " + roomCount + " 个房间，删除失败！");
                }
            }
            
            // 3. 执行删除 (在确认没有房间后)
            // (如果数据库设置了外键约束，可能还需要先删除楼层)
            if (CollUtil.isNotEmpty(floorIds)) {
                // 确保先删除子表的楼层记录
                floorMapper.deleteBatchIds(floorIds);
            }
            // 再删除主表楼栋
            this.removeById(buildingId);
        }
    }
    
    /**
     * 辅助方法：校验楼栋名在校区内是否唯一
     */
    private boolean checkBuildingNameUnique(String buildingName, Long campusId, Long excludeBuildingId) {
        if (StrUtil.isBlank(buildingName) || campusId == null) {
            return false;
        }
        LambdaQueryWrapper<DormBuilding> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DormBuilding::getBuildingName, buildingName)
                .eq(DormBuilding::getCampusId, campusId);
        if (excludeBuildingId != null) {
            wrapper.ne(DormBuilding::getBuildingId, excludeBuildingId);
        }
        // 使用 baseMapper (由 ServiceImpl 注入) 或 this.exists()
        return this.exists(wrapper);
    }
    
    /**
     * 辅助方法：填充关联信息 (优化 N+1)
     */
    private void fillBuildingVOInfo(List<DormBuilding> buildingList) {
        if (CollUtil.isEmpty(buildingList)) {
            return;
        }
        
        // 1. 批量获取宿管ID
        List<Long> managerIds = buildingList.stream()
                .map(DormBuilding::getManagerId)
                .filter(id -> id != null && id != 0)
                .distinct()
                .collect(Collectors.toList());
        
        // 2. 批量获取校区ID
        List<Long> campusIds = buildingList.stream()
                .map(DormBuilding::getCampusId)
                .distinct()
                .collect(Collectors.toList());
        
        // 3. 批量查询 Map
        Map<Long, String> managerMap = managerIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(managerIds).stream()
                        .collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickname));
        
        Map<Long, String> campusMap = campusIds.isEmpty() ? Collections.emptyMap() :
                campusMapper.selectBatchIds(campusIds).stream()
                        .collect(Collectors.toMap(SysCampus::getCampusId, SysCampus::getCampusName));
        
        // 4. 填充数据
        buildingList.forEach(building -> {
            building.setCampusName(campusMap.get(building.getCampusId()));
            if (building.getManagerId() != null) {
                building.setManagerName(managerMap.get(building.getManagerId()));
            }
        });
    }
}