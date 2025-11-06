package com.dormitory.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.DormMeterElectricQueryDTO;
import com.dormitory.entity.BizBillingRecord;
import com.dormitory.entity.BizMeterReading;
import com.dormitory.entity.DormMeterElectric;
import com.dormitory.entity.DormRoom;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.BizBillingRecordMapper;
import com.dormitory.mapper.BizMeterReadingMapper;
import com.dormitory.mapper.DormMeterElectricMapper;
import com.dormitory.mapper.DormRoomMapper;
import com.dormitory.service.IDormMeterElectricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房间电表业务服务实现类
 */
@Service
public class DormMeterElectricServiceImpl extends ServiceImpl<DormMeterElectricMapper, DormMeterElectric> implements IDormMeterElectricService {
    
    @Autowired
    private DormRoomMapper roomMapper;
    
    @Autowired
    private BizBillingRecordMapper billingRecordMapper;
    
    // 用于级联删除
    @Autowired
    private BizMeterReadingMapper readingMapper;
    
    // @Autowired
    // private BizElectricAlertMapper alertMapper; // 告警是按 RoomID 关联的，不是 MeterID，所以删除电表不应删除告警历史
    
    /**
     * 分页查询电表列表 (实现 IDormMeterElectricService 接口)
     */
    @Override
    public Page<DormMeterElectric> selectMeterPage(DormMeterElectricQueryDTO queryDTO) { // 【修改】
        
        Page<DormMeterElectric> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize()); // 【修改】
        
        // 1. 执行分页查询
        Page<DormMeterElectric> meterPage = this.page(page); // 【修改】
        
        // 2. 填充房间号 roomNumber (避免 N+1 查询问题)
        if (!meterPage.getRecords().isEmpty()) {
            List<Long> roomIds = meterPage.getRecords().stream()
                    .map(DormMeterElectric::getRoomId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 批量查询房间信息
            Map<Long, String> roomNumberMap = roomMapper.selectBatchIds(roomIds).stream()
                    .collect(Collectors.toMap(DormRoom::getRoomId, DormRoom::getRoomNumber));
            
            // 填充到电表实体中
            meterPage.getRecords().forEach(meter -> {
                meter.setRoomNumber(roomNumberMap.get(meter.getRoomId()));
            });
        }
        return meterPage;
    }
    
    
    /**
     * 新增电表 (含一房一表约束校验)
     */
    @Override
    public void addMeter(DormMeterElectric meter) {

        Long count = this.count(new LambdaQueryWrapper<DormMeterElectric>()
                .eq(DormMeterElectric::getRoomId, meter.getRoomId()));
        
        if (count > 0) {
            throw new BusinessException("该房间已配置电表，请勿重复添加。");
        }
        
        this.save(meter);
    }
    
    
    /**
     * 批量删除电表 (已修改：增加级联删除)
     */
//    @Override
//    public void deleteMeterByIds(Long[] meterIds) {
//
//        // 1. 新增：级联删除关联的读数记录
//        // 删除所有类型为 "1" (电) 且 MeterId 在列表中的读数
//        if (meterIds != null && meterIds.length > 0) {
//            readingMapper.delete(new LambdaQueryWrapper<BizMeterReading>()
//                    .in(BizMeterReading::getMeterId, Arrays.asList(meterIds))
//                    .eq(BizMeterReading::getMeterType, "1") // 1=电
//            );
//
//            // 2. 删除电表自身
//            this.removeBatchByIds(Arrays.asList(meterIds));
//        }
//
//        // TODO: 实际生产中，还应检查是否有未出账的账单（biz_billing_record），
//        // 但账单是按 RoomId 关联的，删除表计资产通常不删除历史账单。
//    }
        /**
         * 批量删除电表 (修复：增加未付账单校验)
         */
        @Override
        @Transactional
        public void deleteMeterByIds(Long[] meterIds) {
            
            if (meterIds == null || meterIds.length == 0) {
                return;
            }
            List<Long> meterIdList = Arrays.asList(meterIds);
            
            // --- 【【【【【 新增校验：检查未付账单 】】】】】 ---
            // 1. 查找这些电表关联的 RoomID
            List<Long> roomIds = this.listByIds(meterIdList).stream()
                    .map(DormMeterElectric::getRoomId)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (CollUtil.isNotEmpty(roomIds)) {
                // 2. 检查这些 RoomID 是否存在 "电费(1)" 且 "未支付(0)" 的账单
                boolean hasUnpaidBills = billingRecordMapper.exists(
                        new LambdaQueryWrapper<BizBillingRecord>()
                                .in(BizBillingRecord::getRoomId, roomIds)
                                .eq(BizBillingRecord::getMeterType, "1") // 1=电
                                .eq(BizBillingRecord::getIsPaid, 0) // 0=未支付
                );
                if (hasUnpaidBills) {
                    throw new BusinessException("删除失败：所选电表关联的房间中存在未支付的电费账单，请先处理账单。");
                }
            }
            // --- 【校验结束】 ---
            
            // 3. 级联删除关联的读数记录
            readingMapper.delete(new LambdaQueryWrapper<BizMeterReading>()
                    .in(BizMeterReading::getMeterId, meterIdList)
                    .eq(BizMeterReading::getMeterType, "1") // 1=电
            );
            
            // 4. 删除电表自身
            this.removeBatchByIds(meterIdList);
        }
}