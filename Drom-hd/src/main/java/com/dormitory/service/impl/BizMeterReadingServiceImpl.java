package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.MeterReadingQueryDTO;
import com.dormitory.entity.BizMeterReading;
import com.dormitory.entity.DormMeterElectric; // 电表实体
import com.dormitory.entity.DormMeterWater; // 水表实体
import com.dormitory.entity.DormRoom;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.BizMeterReadingMapper;
import com.dormitory.mapper.DormMeterElectricMapper; // 电表Mapper
import com.dormitory.mapper.DormMeterWaterMapper; // 水表Mapper
import com.dormitory.mapper.DormRoomMapper;
import com.dormitory.service.IBizMeterReadingService;
import cn.hutool.core.collection.CollUtil;

// 导入 Collections 用于 emptyMap
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 水/电表读数记录业务服务实现类
 */
@Service
public class BizMeterReadingServiceImpl extends ServiceImpl<BizMeterReadingMapper, BizMeterReading> implements IBizMeterReadingService {
    
    // 注入所需 Mapper
    @Autowired private DormMeterElectricMapper electricMapper;
    @Autowired private DormMeterWaterMapper waterMapper;
    @Autowired private DormRoomMapper roomMapper;
    
    /**
     * 分页查询读数记录
     * 修复房间 ID 反查
     */
    @Override
    public Page<BizMeterReading> selectReadingPage(MeterReadingQueryDTO queryDTO) {
        
        // 1. 构建基础查询条件
        LambdaQueryWrapper<BizMeterReading> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getMeterId() != null, BizMeterReading::getMeterId, queryDTO.getMeterId());
        
        // =========================================================
        // 修复：实现房间 ID 反查逻辑
        // =========================================================
        if (queryDTO.getRoomId() != null) {
            // 用户希望按房间ID筛选
            List<Long> meterIdsToFilter = new ArrayList<>();
            String meterType = queryDTO.getMeterType();
            
            // 1.1 如果指定了类型 "1" (电) 或未指定类型
            if (meterType == null || "1".equals(meterType)) {
                List<DormMeterElectric> eMeters = electricMapper.selectList(new LambdaQueryWrapper<DormMeterElectric>()
                        .eq(DormMeterElectric::getRoomId, queryDTO.getRoomId())
                        .select(DormMeterElectric::getMeterId));
                if (CollUtil.isNotEmpty(eMeters)) {
                    meterIdsToFilter.addAll(eMeters.stream().map(DormMeterElectric::getMeterId).collect(Collectors.toList()));
                }
            }
            
            // 1.2 如果指定了类型 "2" (水) 或未指定类型
            if (meterType == null || "2".equals(meterType)) {
                List<DormMeterWater> wMeters = waterMapper.selectList(new LambdaQueryWrapper<DormMeterWater>()
                        .eq(DormMeterWater::getRoomId, queryDTO.getRoomId())
                        .select(DormMeterWater::getMeterId));
                if (CollUtil.isNotEmpty(wMeters)) {
                    meterIdsToFilter.addAll(wMeters.stream().map(DormMeterWater::getMeterId).collect(Collectors.toList()));
                }
            }
            
            // 1.3 如果该房间没有任何表计，直接返回空
            if (CollUtil.isEmpty(meterIdsToFilter)) {
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            
            // 1.4 应用表计ID过滤
            wrapper.in(BizMeterReading::getMeterId, meterIdsToFilter);
            
            // 1.5 如果 DTO 中也传了 meterType，需要再次过滤
            if(meterType != null) {
                wrapper.eq(BizMeterReading::getMeterType, meterType);
            }
            
        } else {
            // 如果没传 roomId，才使用 DTO 中的 meterType 进行过滤
            wrapper.eq(queryDTO.getMeterType() != null, BizMeterReading::getMeterType, queryDTO.getMeterType());
        }
        // =========================================================
        
        
        // 条件化添加日期范围查询
        if (queryDTO.getStartDate() != null) {
            wrapper.ge(BizMeterReading::getReadingDate, queryDTO.getStartDate().atStartOfDay());
        }
        if (queryDTO.getEndDate() != null) {
            wrapper.le(BizMeterReading::getReadingDate, queryDTO.getEndDate().plusDays(1).atStartOfDay());
        }
        
        wrapper.orderByDesc(BizMeterReading::getReadingDate);
        
        Page<BizMeterReading> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        
        // 2. 填充关联信息 (表计编号、房间号)
        if (!page.getRecords().isEmpty()) {
            List<BizMeterReading> records = page.getRecords();
            
            // 分别获取电表和水表 ID 列表
            List<Long> electricMeterIds = records.stream()
                    .filter(r -> "1".equals(r.getMeterType()))
                    .map(BizMeterReading::getMeterId).distinct().collect(Collectors.toList());
            List<Long> waterMeterIds = records.stream()
                    .filter(r -> "2".equals(r.getMeterType()))
                    .map(BizMeterReading::getMeterId).distinct().collect(Collectors.toList());
            
            final Map<Long, DormMeterElectric> electricMeterMap = electricMeterIds.isEmpty() ?
                    Collections.emptyMap() :
                    electricMapper.selectBatchIds(electricMeterIds).stream()
                            .collect(Collectors.toMap(DormMeterElectric::getMeterId, m -> m));
            
            final Map<Long, DormMeterWater> waterMeterMap = waterMeterIds.isEmpty() ?
                    Collections.emptyMap() :
                    waterMapper.selectBatchIds(waterMeterIds).stream()
                            .collect(Collectors.toMap(DormMeterWater::getMeterId, m -> m));
            
            // 获取所有涉及的房间ID
            List<Long> roomIds = records.stream().map(reading -> {
                if ("1".equals(reading.getMeterType())) {
                    DormMeterElectric meter = electricMeterMap.get(reading.getMeterId());
                    return meter != null ? meter.getRoomId() : null;
                } else { // 假设只有 1 和 2 两种类型
                    DormMeterWater meter = waterMeterMap.get(reading.getMeterId());
                    return meter != null ? meter.getRoomId() : null;
                }
            }).filter(id -> id != null).distinct().collect(Collectors.toList());
            
            // 批量查询房间号 (增加空列表校验)
            final Map<Long, String> roomNumberMap = roomIds.isEmpty() ?
                    Collections.emptyMap() :
                    roomMapper.selectBatchIds(roomIds).stream()
                            .collect(Collectors.toMap(DormRoom::getRoomId, DormRoom::getRoomNumber));
            
            // 填充数据 (使用 final Map)
            records.forEach(reading -> {
                Long roomId = null;
                if ("1".equals(reading.getMeterType()) && electricMeterMap.containsKey(reading.getMeterId())) {
                    DormMeterElectric meter = electricMeterMap.get(reading.getMeterId());
                    reading.setMeterCode(meter.getMeterCode());
                    roomId = meter.getRoomId();
                } else if ("2".equals(reading.getMeterType()) && waterMeterMap.containsKey(reading.getMeterId())) {
                    DormMeterWater meter = waterMeterMap.get(reading.getMeterId());
                    reading.setMeterCode(meter.getMeterCode());
                    roomId = meter.getRoomId();
                }
                if (roomId != null) {
                    // 安全地从 Map 获取房间号
                    reading.setRoomNumber(roomNumberMap.get(roomId));
                }
            });
        }
        return page;
    }
    
    /**
     * 新增读数记录，并增加读数校验
     */
    @Override
    public void addReading(BizMeterReading reading) {
        // 1. 校验 meterId 和 meterType 是否匹配存在
        boolean meterExists = false;
        if ("1".equals(reading.getMeterType())) {
            meterExists = electricMapper.exists(new LambdaQueryWrapper<DormMeterElectric>().eq(DormMeterElectric::getMeterId, reading.getMeterId()));
        } else if ("2".equals(reading.getMeterType())) {
            meterExists = waterMapper.exists(new LambdaQueryWrapper<DormMeterWater>().eq(DormMeterWater::getMeterId, reading.getMeterId()));
        }
        if (!meterExists) {
            throw new BusinessException("新增读数失败：关联的表计资产不存在。");
        }
        
        // 2. 修复：业务校验：本次读数不能小于上次读数
        // 查询该 meterId 的最新一条读数记录
        BizMeterReading lastReading = this.getOne(new LambdaQueryWrapper<BizMeterReading>()
                .eq(BizMeterReading::getMeterId, reading.getMeterId())
                .eq(BizMeterReading::getMeterType, reading.getMeterType())
                .orderByDesc(BizMeterReading::getReadingDate)
                .last("LIMIT 1")
        );
        
        if (lastReading != null && reading.getReadingValue().compareTo(lastReading.getReadingValue()) < 0) {
            throw new BusinessException("新增读数失败：本次读数 " + reading.getReadingValue() +
                    " 不能小于上次读数 " + lastReading.getReadingValue());
        }
        // =========================================================
        
        // 3. 自动填充创建时间等 (由 BaseEntity 和 MyMetaObjectHandler 完成)
        this.save(reading);
    }
}