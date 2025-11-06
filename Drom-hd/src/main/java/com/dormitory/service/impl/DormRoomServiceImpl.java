package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.DormRoomQueryDTO;
import com.dormitory.dto.RoomStatusUpdateDTO;
import com.dormitory.entity.*;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.*;
import com.dormitory.service.IDormBedService;
import com.dormitory.service.IDormRoomService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 宿舍房间业务服务实现类
 */
@Service
public class DormRoomServiceImpl extends ServiceImpl<DormRoomMapper, DormRoom> implements IDormRoomService {
    
    @Autowired private DormFloorMapper floorMapper;
    @Autowired private DormBuildingMapper buildingMapper;
    @Autowired private DormBedMapper bedMapper;
    @Autowired private IDormBedService bedService;
    @Autowired private DormRoomAssetMapper assetMapper;
    @Autowired private BizRepairOrderMapper repairOrderMapper;
    @Autowired private DormMeterElectricMapper electricMapper;
    @Autowired private DormMeterWaterMapper waterMapper;
    
    
    /**
     * 【重构】分页查询房间列表 (含楼栋名和楼层号)
     */
    @Override
    public Page<DormRoom> selectRoomPage(DormRoomQueryDTO queryDTO) {
        
        // 1. 【联查】处理按楼栋ID(buildingId)查询
        List<Long> floorIdsToFilter = null;
        if (queryDTO.getBuildingId() != null) {
            LambdaQueryWrapper<DormFloor> floorWrapper = new LambdaQueryWrapper<>();
            floorWrapper.eq(DormFloor::getBuildingId, queryDTO.getBuildingId())
                    .select(DormFloor::getFloorId);
            List<Object> floorIdsObj = floorMapper.selectObjs(floorWrapper);
            floorIdsToFilter = floorIdsObj.stream().map(o -> (Long) o).collect(Collectors.toList());
            if (CollUtil.isEmpty(floorIdsToFilter)) {
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
        }
        
        // 2. 构建基础查询条件
        LambdaQueryWrapper<DormRoom> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(queryDTO.getFloorId() != null, DormRoom::getFloorId, queryDTO.getFloorId());
        
        if (queryDTO.getFloorId() == null && CollUtil.isNotEmpty(floorIdsToFilter)) {
            wrapper.in(DormRoom::getFloorId, floorIdsToFilter);
        }
        
        wrapper.like(StrUtil.isNotEmpty(queryDTO.getRoomNumber()), DormRoom::getRoomNumber, queryDTO.getRoomNumber())
                .eq(StrUtil.isNotEmpty(queryDTO.getRoomPurposeType()), DormRoom::getRoomPurposeType, queryDTO.getRoomPurposeType())
                .eq(StrUtil.isNotEmpty(queryDTO.getRoomStatus()), DormRoom::getRoomStatus, queryDTO.getRoomStatus())
                .orderByAsc(DormRoom::getFloorId)
                .orderByAsc(DormRoom::getRoomNumber);
        
        // 3. 执行分页查询
        Page<DormRoom> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        
        // 4. 填充关联信息 (楼栋名、楼层号)
        if (!page.getRecords().isEmpty()) {
            fillRoomVOInfo(page.getRecords());
        }
        return page;
    }
    
    /**
     * 【重构】新增房间 (Admin 专属)
     */
    @Override
    @Transactional
    public void addRoom(DormRoom room) {
        // 1. 校验所属楼层是否存在
        DormFloor floor = floorMapper.selectById(room.getFloorId());
        if (floor == null) {
            throw new BusinessException("新增房间失败，所属楼层不存在");
        }
        
        // 2. 校验容量 (1 <= 容量 <= 10)
        if (room.getRoomCapacity() == null || room.getRoomCapacity() < 1 || room.getRoomCapacity() > 10) {
            throw new BusinessException("房间容量必须在 1 到 10 个床位之间。");
        }
        
        // 3. 校验房间号唯一性 (数据库 uk_floor_room 约束会处理，但最好前置校验)
        if (this.exists(new LambdaQueryWrapper<DormRoom>()
                .eq(DormRoom::getFloorId, room.getFloorId())
                .eq(DormRoom::getRoomNumber, room.getRoomNumber()))) {
            throw new BusinessException("新增房间失败：该楼层下已存在此房间号");
        }
        
        this.save(room);
        
        // 4. 业务联动：如果是学生宿舍(00)，自动创建床位
        if ("00".equals(room.getRoomPurposeType())) {
            bedService.batchAddBeds(room.getRoomId(), room.getRoomCapacity());
        }
    }
    
    /**
     * 【Admin 专属】更新房间所有信息 (包括容量)
     */
    @Override
    @Transactional
    public void updateRoomByAdmin(DormRoom room) {
        if (room.getRoomId() == null) {
            throw new BusinessException("房间ID不能为空");
        }
        
        DormRoom oldRoom = this.getById(room.getRoomId());
        if (oldRoom == null) {
            throw new BusinessException("要修改的房间不存在");
        }
        
        // 2. 核心校验：容量调整约束
        Integer newCapacity = room.getRoomCapacity();
        if (newCapacity != null && !newCapacity.equals(oldRoom.getRoomCapacity())) {
            
            if (newCapacity < 1 || newCapacity > 10) {
                throw new BusinessException("房间容量必须在 1 到 10 个床位之间。");
            }
            
            Long existingBedCount = bedMapper.selectCount(
                    new LambdaQueryWrapper<DormBed>().eq(DormBed::getRoomId, room.getRoomId())
            );
            Integer occupiedBeds = oldRoom.getOccupiedBeds();
            
            if (newCapacity < oldRoom.getRoomCapacity()) {
                if (occupiedBeds > newCapacity) {
                    throw new BusinessException("容量缩减失败：当前房间已入住 " + occupiedBeds + " 人...");
                }
                if (existingBedCount > newCapacity) {
                    throw new BusinessException("容量缩减失败：请先删除多余的 " + (existingBedCount - newCapacity) + " 个空闲床位记录！");
                }
            }
            
            if (newCapacity > oldRoom.getRoomCapacity() && newCapacity > existingBedCount) {
                batchCreateMissingBeds(room.getRoomId(), newCapacity, existingBedCount);
            }
        }
        
        // 3. 执行更新 (Admin 可以更新所有字段)
        this.updateById(room);
    }
    
    /**
     * 【宿管/辅导员】更新房间状态 (封禁/解封)
     */
    @Override
    @Transactional
    public void updateRoomStatus(RoomStatusUpdateDTO dto) {
        DormRoom room = this.getById(dto.getRoomId());
        if (room == null) {
            throw new BusinessException("房间不存在");
        }
        
        if (("1".equals(dto.getRoomStatus()) || "2".equals(dto.getRoomStatus())) &&
                room.getOccupiedBeds() > 0) {
            throw new BusinessException("封禁失败：房间内仍有 " + room.getOccupiedBeds() + " 名住户，请先执行迁出！");
        }
        
        // 仅更新 DTO 中包含的字段 (status, remark)
        DormRoom roomToUpdate = new DormRoom();
        BeanUtil.copyProperties(dto, roomToUpdate);
        
        this.updateById(roomToUpdate);
    }
    
    /**
     * 批量删除房间 (Admin 专属)
     */
    @Override
    @Transactional
    public void deleteRoomByIds(Long[] roomIds) {
        if (ArrayUtil.isEmpty(roomIds)) return;
        
        for (Long roomId : roomIds) {
            DormRoom room = this.getById(roomId);
            if (room == null) continue;
            
            if (room.getOccupiedBeds() > 0) {
                throw new BusinessException("房间 " + room.getRoomNumber() + " 尚有住户，无法删除！");
            }
            
            List<Object> assetIds = assetMapper.selectObjs(new LambdaQueryWrapper<DormRoomAsset>()
                    .eq(DormRoomAsset::getRoomId, roomId).select(DormRoomAsset::getAssetId));
            if (CollUtil.isNotEmpty(assetIds)) {
                if (repairOrderMapper.exists(new LambdaQueryWrapper<BizRepairOrder>()
                        .in(BizRepairOrder::getAssetId, assetIds)
                        .notIn(BizRepairOrder::getOrderStatus, "2", "3"))) {
                    throw new BusinessException("房间 " + room.getRoomNumber() + " 尚有关联的未完成报修单，无法删除！");
                }
            }
            
            // 级联删除
            bedMapper.delete(new LambdaQueryWrapper<DormBed>().eq(DormBed::getRoomId, roomId));
            assetMapper.delete(new LambdaQueryWrapper<DormRoomAsset>().eq(DormRoomAsset::getRoomId, roomId));
            electricMapper.delete(new LambdaQueryWrapper<DormMeterElectric>().eq(DormMeterElectric::getRoomId, roomId));
            waterMapper.delete(new LambdaQueryWrapper<DormMeterWater>().eq(DormMeterWater::getRoomId, roomId));
            
            this.removeById(roomId);
        }
    }
    
    /**
     * 辅助方法：批量创建缺失的床位
     */
    private void batchCreateMissingBeds(Long roomId, Integer newCapacity, Long existingBedCount) {
        List<DormBed> newBeds = new java.util.ArrayList<>();
        int startIdx = existingBedCount != null ? existingBedCount.intValue() : 0;
        
        for (int i = startIdx; i < newCapacity; i++) {
            DormBed bed = new DormBed();
            bed.setRoomId(roomId);
            String bedCode = String.valueOf((char) ('A' + i));
            bed.setBedNumber(bedCode);
            bed.setIsOccupied(0);
            newBeds.add(bed);
        }
        
        if (!newBeds.isEmpty()) {
            bedService.saveBatch(newBeds);
        }
    }
    
    /**
     * 【重构】辅助方法：填充房间的关联信息 (楼栋名、楼层号)
     */
    private void fillRoomVOInfo(List<DormRoom> roomList) {
        if (CollUtil.isEmpty(roomList)) {
            return;
        }
        
        // 1. 批量获取楼层信息
        List<Long> floorIds = roomList.stream().map(DormRoom::getFloorId).distinct().collect(Collectors.toList());
        final Map<Long, DormFloor> floorMap = floorIds.isEmpty() ? Collections.emptyMap() :
                floorMapper.selectBatchIds(floorIds).stream().collect(Collectors.toMap(DormFloor::getFloorId, f -> f));
        
        // 2. 批量获取楼栋信息
        List<Long> buildingIds = floorMap.values().stream().map(DormFloor::getBuildingId).distinct().collect(Collectors.toList());
        final Map<Long, String> buildingNameMap = buildingIds.isEmpty() ? Collections.emptyMap() :
                buildingMapper.selectBatchIds(buildingIds).stream().collect(Collectors.toMap(DormBuilding::getBuildingId, DormBuilding::getBuildingName));
        
        // 3. 填充数据
        roomList.forEach(room -> {
            DormFloor floor = floorMap.get(room.getFloorId());
            if (floor != null) {
                // 填充非数据库字段
                room.setFloorNumber(floor.getFloorNumber());
                room.setFloorName(floor.getFloorName());
                room.setBuildingId(floor.getBuildingId());
                room.setBuildingName(buildingNameMap.get(floor.getBuildingId()));
            }
        });
    }
}