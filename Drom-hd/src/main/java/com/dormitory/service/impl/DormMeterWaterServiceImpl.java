package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.DormMeterWaterQueryDTO; // 【修改】
import com.dormitory.entity.BizMeterReading; // 【【【【【 新增导入 】】】】】
import com.dormitory.entity.DormMeterWater;
import com.dormitory.entity.DormRoom;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.BizMeterReadingMapper; // 【【【【【 新增导入 】】】】】
import com.dormitory.mapper.DormMeterWaterMapper;
import com.dormitory.mapper.DormRoomMapper;
import com.dormitory.service.IDormMeterWaterService;
import cn.hutool.core.util.ArrayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房间水表业务服务实现类
 */
@Service
public class DormMeterWaterServiceImpl extends ServiceImpl<DormMeterWaterMapper, DormMeterWater> implements IDormMeterWaterService {
    
    @Autowired
    private DormRoomMapper roomMapper;
    
    // 新增注入 (用于级联删除)
    @Autowired
    private BizMeterReadingMapper readingMapper;
    
    /**
     * 分页查询水表列表 (实现 IDormMeterWaterService 接口)
     */
    @Override
    public Page<DormMeterWater> selectMeterPage(DormMeterWaterQueryDTO queryDTO) { // 【修改】
        
        Page<DormMeterWater> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize()); // 【修改】
        
        Page<DormMeterWater> meterPage = this.page(page); // 【修改】
        
        // 填充房间号 roomNumber
        if (!meterPage.getRecords().isEmpty()) {
            List<Long> roomIds = meterPage.getRecords().stream()
                    .map(DormMeterWater::getRoomId)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Long, String> roomNumberMap = roomMapper.selectBatchIds(roomIds).stream()
                    .collect(Collectors.toMap(DormRoom::getRoomId, DormRoom::getRoomNumber));
            
            meterPage.getRecords().forEach(meter -> {
                meter.setRoomNumber(roomNumberMap.get(meter.getRoomId()));
            });
        }
        return meterPage;
    }
    
    /**
     * 新增水表 (核心约束：一房一表)
     */
    @Override
    public void addMeter(DormMeterWater meter) {

        Long count = this.count(new LambdaQueryWrapper<DormMeterWater>()
                .eq(DormMeterWater::getRoomId, meter.getRoomId()));
        
        if (count > 0) {
            throw new BusinessException("该房间已配置水表，请勿重复添加。");
        }
        
        this.save(meter);
    }
    
    /**
     * 批量删除水表，已修改：增加级联删除
     */
    @Override
    @Transactional
    public void deleteMeterByIds(Long[] meterIds) {
        if (ArrayUtil.isEmpty(meterIds)) {
            return;
        }
        
        // 1. 新增：级联删除关联的读数记录
        // 删除所有类型为 "2" (水) 且 MeterId 在列表中的读数
        readingMapper.delete(new LambdaQueryWrapper<BizMeterReading>()
                .in(BizMeterReading::getMeterId, Arrays.asList(meterIds))
                .eq(BizMeterReading::getMeterType, "2") // 2=水
        );
        
        // 2. 删除水表自身
        this.removeBatchByIds(Arrays.asList(meterIds));
    }
}