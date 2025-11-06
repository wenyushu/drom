package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.RoomAssetQueryDTO;
import com.dormitory.entity.BizRepairOrder; // 导入报修单实体
import com.dormitory.entity.DormRoom;
import com.dormitory.entity.DormRoomAsset;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.BizRepairOrderMapper; // 导入报修单 Mapper
import com.dormitory.mapper.DormRoomAssetMapper;
import com.dormitory.mapper.DormRoomMapper;
import com.dormitory.service.IDormRoomAssetService;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房间固定资产业务服务实现类
 */
@Service
public class DormRoomAssetServiceImpl extends ServiceImpl<DormRoomAssetMapper, DormRoomAsset> implements IDormRoomAssetService {
    
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private BizRepairOrderMapper repairOrderMapper; // 注入报修单 Mapper
    
    /**
     * 分页查询房间资产列表 (含房间号)
     */
    @Override
    public Page<DormRoomAsset> selectAssetPage(RoomAssetQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<DormRoomAsset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getRoomId() != null, DormRoomAsset::getRoomId, queryDTO.getRoomId())
                .like(StrUtil.isNotEmpty(queryDTO.getAssetName()), DormRoomAsset::getAssetName, queryDTO.getAssetName())
                .eq(StrUtil.isNotEmpty(queryDTO.getAssetType()), DormRoomAsset::getAssetType, queryDTO.getAssetType())
                .eq(StrUtil.isNotEmpty(queryDTO.getStatus()), DormRoomAsset::getStatus, queryDTO.getStatus())
                .orderByAsc(DormRoomAsset::getRoomId)
                .orderByAsc(DormRoomAsset::getAssetId);
        
        Page<DormRoomAsset> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        
        // 2. 填充房间号
        if (!page.getRecords().isEmpty()) {
            List<Long> roomIds = page.getRecords().stream().map(DormRoomAsset::getRoomId).distinct().collect(Collectors.toList());
            Map<Long, String> roomNumberMap = roomMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(DormRoom::getRoomId, DormRoom::getRoomNumber));
            page.getRecords().forEach(asset -> asset.setRoomNumber(roomNumberMap.get(asset.getRoomId())));
        }
        
        return page;
    }
    
    /**
     * 新增资产 (含房间校验)
     */
    @Override
    public void addAsset(DormRoomAsset asset) {
        // 1. 校验所属房间是否存在
        if (!roomMapper.exists(new LambdaQueryWrapper<DormRoom>().eq(DormRoom::getRoomId, asset.getRoomId()))) {
            throw new BusinessException("新增资产失败，所属房间不存在");
        }
        // 2. 校验资产类型编码是否有效 (略)
        
        this.save(asset);
    }
    
    /**
     * 批量删除资产 (需校验是否关联未完成的报修单)
     */
    @Override
    @Transactional
    public void deleteAssetByIds(Long[] assetIds) {
        if (ArrayUtil.isEmpty(assetIds)) return;
        
        // 核心约束校验：检查是否有未完成的报修单关联此资产
        LambdaQueryWrapper<BizRepairOrder> repairWrapper = new LambdaQueryWrapper<>();
        repairWrapper.in(BizRepairOrder::getAssetId, Arrays.asList(assetIds))
                // 状态不为 '已完成'(2) 或 '无法修复'(3)
                .notIn(BizRepairOrder::getOrderStatus, "2", "3");
        
        Long pendingRepairCount = repairOrderMapper.selectCount(repairWrapper);
        if (pendingRepairCount > 0) {
            throw new BusinessException("删除失败：所选资产中存在未完成的报修工单，请先处理工单。");
        }
        
        // 执行删除
        this.removeBatchByIds(Arrays.asList(assetIds));
    }
}