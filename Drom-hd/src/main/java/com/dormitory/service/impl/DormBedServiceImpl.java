package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.entity.DormBed;
import com.dormitory.entity.DormBuilding;
import com.dormitory.entity.DormFloor;
import com.dormitory.entity.DormRoom;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.DormBedMapper;
import com.dormitory.mapper.DormBuildingMapper;
import com.dormitory.mapper.DormFloorMapper;
import com.dormitory.mapper.DormRoomMapper;
import com.dormitory.service.IDormBedService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.dormitory.dto.DormBedQueryDTO;

/**
 * 床位业务服务实现类
 */
@Service
public class DormBedServiceImpl extends ServiceImpl<DormBedMapper, DormBed> implements IDormBedService {
    
    @Autowired
    private DormRoomMapper roomMapper;
    
    // --- 9. 注入联查所需的 Mapper ---
    @Autowired
    private DormFloorMapper floorMapper;
    @Autowired
    private DormBuildingMapper buildingMapper;
    
    
    /**
     * 根据房间容量批量创建床位 (A, B, C...)
     */
    @Override
    @Transactional
    public void batchAddBeds(Long roomId, Integer capacity) {
        if (capacity == null || capacity <= 0 || roomId == null) {
            throw new BusinessException("房间 ID 和容量必须有效");
        }
        
        // 1. 校验房间是否存在
        DormRoom room = roomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("指定房间不存在，无法创建床位");
        }
        
        // 2. 校验容量上限 (如果房间容量小于要创建的床位数量，则不允许)
        if (room.getRoomCapacity() < capacity) {
            throw new BusinessException(room.getRoomNumber() + " 房间容量上限为 " + room.getRoomCapacity() + "，不能创建 " + capacity + " 个床位");
        }
        
        // 新增校验：检查已存在床位
        Long existingBedCount = this.count(
                new LambdaQueryWrapper<DormBed>().eq(DormBed::getRoomId, roomId)
        );
        
        if (existingBedCount >= capacity) {
            throw new BusinessException("创建失败：该房间已有 " + existingBedCount + " 个床位，无需重复创建。");
        }
        
        int startIdx = existingBedCount.intValue(); // 从已有床位数量开始编号
        // 校验结束
        
        // 3. 批量生成床位记录 (使用 A, B, C... 编号)
        List<DormBed> beds = new java.util.ArrayList<>();
        for (int i = 0; i < capacity; i++) {
            DormBed bed = new DormBed();
            bed.setRoomId(roomId);
            // Hutool 工具类：int 转换为 char (0 -> A, 1 -> B)
            String bedCode = String.valueOf((char) ('A' + i));
            bed.setBedNumber(bedCode);
            bed.setIsOccupied(0);
            beds.add(bed);
        }
        
        // 4. 批量保存
        this.saveBatch(beds);
    }
    
    /**
     * 批量删除床位 (含业务约束校验：is_occupied)
     */
    @Override
    @Transactional
    public void deleteBedByIds(Long[] bedIds) {
        if (ArrayUtil.isEmpty(bedIds)) {
            return;
        }
        
        // 核心修正点：将 listBatchByIds 替换为标准的 listByIds
        List<DormBed> beds = this.listByIds(Arrays.asList(bedIds));
        
        for (DormBed bed : beds) {
            // 核心业务约束校验：不能删除有住户的床位
            if (bed.getIsOccupied() == 1) {
                // 查找房间号进行友好提示
                DormRoom room = roomMapper.selectById(bed.getRoomId());
                String roomName = room != null ? room.getRoomNumber() : "未知房间";
                throw new BusinessException("床位 " + roomName + "-" + bed.getBedNumber() + " 仍有人居住，无法删除！");
            }
        }
        
        // 执行删除
        this.removeBatchByIds(Arrays.asList(bedIds));
    }
    
    /**
     * 分页查询床位列表 (V2: 补全联查实现)
     */
    @Override
    public Page<DormBed> selectBedPage(DormBedQueryDTO queryDTO) {
        
        // 1. 核心修正：在这里创建 Page 对象
        Page<DormBed> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 2. 构建查询条件
        LambdaQueryWrapper<DormBed> wrapper = new LambdaQueryWrapper<>();
        
        // TODO: (可选) 如果 DormBedQueryDTO 中有 roomId 或 isOccupied 等筛选字段，在这里添加 .eq()
        // wrapper.eq(queryDTO.getRoomId() != null, DormBed::getRoomId, queryDTO.getRoomId());
        
        // 3. 执行分页查询
        this.page(page, wrapper);
        
        // 4. 填充关联信息
        if (CollUtil.isNotEmpty(page.getRecords())) {
            fillBedRelatedInfo(page.getRecords());
        }
        
        return page;
    }
    
    /**
     * 辅助方法：为床位列表填充 房间号 和 楼栋名
     * @param bedList 床位列表
     */
    private void fillBedRelatedInfo(List<DormBed> bedList) {
        // 1. 批量获取 房间ID 列表
        List<Long> roomIds = bedList.stream()
                .map(DormBed::getRoomId)
                .distinct()
                .collect(Collectors.toList());
        if (roomIds.isEmpty()) return;
        
        // 2. 批量查询 房间 Map (Key: roomId, Value: DormRoom)
        Map<Long, DormRoom> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                .collect(Collectors.toMap(DormRoom::getRoomId, r -> r));
        
        // 3. 批量获取 楼层ID 列表
        List<Long> floorIds = roomMap.values().stream()
                .map(DormRoom::getFloorId)
                .distinct()
                .collect(Collectors.toList());
        if (floorIds.isEmpty()) return;
        
        // 4. 批量查询 楼层 Map (Key: floorId, Value: DormFloor)
        Map<Long, DormFloor> floorMap = floorMapper.selectBatchIds(floorIds).stream()
                .collect(Collectors.toMap(DormFloor::getFloorId, f -> f));
        
        // 5. 批量获取 楼栋ID 列表
        List<Long> buildingIds = floorMap.values().stream()
                .map(DormFloor::getBuildingId)
                .distinct()
                .collect(Collectors.toList());
        if (buildingIds.isEmpty()) return;
        
        // 6. 批量查询 楼栋 Map (Key: buildingId, Value: 楼栋名)
        Map<Long, String> buildingNameMap = buildingMapper.selectBatchIds(buildingIds).stream()
                .collect(Collectors.toMap(DormBuilding::getBuildingId, DormBuilding::getBuildingName));
        
        // 7. 遍历填充
        for (DormBed bed : bedList) {
            DormRoom room = roomMap.get(bed.getRoomId());
            if (room != null) {
                // 填充 房间号
                bed.setRoomNumber(room.getRoomNumber());
                
                DormFloor floor = floorMap.get(room.getFloorId());
                if (floor != null) {
                    // 填充 楼栋名
                    bed.setBuildingName(buildingNameMap.get(floor.getBuildingId()));
                }
            }
        }
        
        // (注意：DormBed 实体没有 occupantName 字段，所以这里不填充住户姓名。
        //  如果需要住户姓名，应该使用 VO (如 BedBrowseVO) 并修改 Controller 返回类型)
    }
}