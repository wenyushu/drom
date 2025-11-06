package com.dormitory.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.dto.BillingPaymentDTO;
import com.dormitory.entity.*; // 导入所有实体
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.*; // 导入所有 Mapper
import com.dormitory.service.IBillingService;
import com.dormitory.service.IBizBillingRecordService;
import org.slf4j.Logger; // 导入日志
import org.slf4j.LoggerFactory; // 导入日志
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 水电费核心计费服务实现类
 */
@Service
public class BillingServiceImpl implements IBillingService {
    
    private static final Logger log = LoggerFactory.getLogger(BillingServiceImpl.class);
    
    // --- 注入所有需要的 Mapper 和 Service ---
    
    @Autowired private BizMeterReadingMapper readingMapper;
    @Autowired private BizBillingRateMapper rateMapper;
    @Autowired private IBizBillingRecordService recordService;
    
    // 【新增】注入表计 Mapper
    @Autowired private DormMeterElectricMapper electricMapper;
    @Autowired private DormMeterWaterMapper waterMapper;
    
    // 【新增】注入账单 Mapper (用于查重)
    @Autowired private BizBillingRecordMapper recordMapper;
    
    
    /**
     * 【V2.0 重构】
     * 执行周期性计费，生成所有房间的水电费账单
     * @param cycleEndDate 计费周期截止日期 (例如 2024-10-31)
     * @return 计费结果摘要
     */
    @Override
    @Transactional // 确保所有账单生成操作的原子性
    public String generateMonthlyBills(LocalDate cycleEndDate) {
        
        // 1. 定义计费周期
        LocalDate cycleStartDate = cycleEndDate.withDayOfMonth(1); // 假设从每月1号开始
        
        log.info("【开始周期性计费】周期: {} 到 {}", cycleStartDate, cycleEndDate);
        
        // 2. 获取所有有效的费率 (简化：获取当期费率)
        List<BizBillingRate> rates = rateMapper.selectList(new LambdaQueryWrapper<BizBillingRate>()
                .le(BizBillingRate::getValidFrom, cycleEndDate)
                .orderByDesc(BizBillingRate::getValidFrom) // 优先用最新的
        );
        Map<String, List<BizBillingRate>> rateMap = rates.stream().collect(Collectors.groupingBy(BizBillingRate::getMeterType));
        
        if (rateMap.isEmpty()) {
            throw new BusinessException("无法生成账单：当前周期无有效费率配置。");
        }
        
        int generatedCount = 0;
        String meterTypeElectric = "1"; // "1" 代表电
        String meterTypeWater = "2";    // "2" 代表水
        
        // --- 3. 批量处理【电费】账单 ---
        List<BizBillingRate> electricRates = rateMap.getOrDefault(meterTypeElectric, Collections.emptyList());
        if (electricRates.isEmpty()) {
            log.warn("【计费警告】未找到 '电' (Type=1) 的有效费率，跳过电费账单生成。");
        } else {
            // 3.1 查询所有电表
            List<DormMeterElectric> electricMeters = electricMapper.selectList(null);
            log.info("发现 {} 个电表，开始处理...", electricMeters.size());
            
            for (DormMeterElectric meter : electricMeters) {
                try {
                    // 3.2 为每个表计生成账单
                    BizBillingRecord record = generateBillForMeter(
                            meter.getRoomId(),
                            meter.getMeterId(),
                            meterTypeElectric,
                            cycleStartDate,
                            cycleEndDate,
                            electricRates
                    );
                    
                    // 3.3 保存账单
                    if (record != null) {
                        recordService.save(record); // 使用 Service 保存
                        generatedCount++;
                    }
                } catch (Exception e) {
                    // 单个表计失败不应中断整个批处理
                    log.error("【计费失败】生成电费账单失败 (MeterID: {}): {}", meter.getMeterId(), e.getMessage());
                }
            }
        }
        
        // --- 4. 批量处理【水费】账单 ---
        List<BizBillingRate> waterRates = rateMap.getOrDefault(meterTypeWater, Collections.emptyList());
        if (waterRates.isEmpty()) {
            log.warn("【计费警告】未找到 '水' (Type=2) 的有效费率，跳过水费账单生成。");
        } else {
            // 4.1 查询所有水表
            List<DormMeterWater> waterMeters = waterMapper.selectList(null);
            log.info("发现 {} 个水表，开始处理...", waterMeters.size());
            
            for (DormMeterWater meter : waterMeters) {
                try {
                    // 4.2 为每个表计生成账单
                    BizBillingRecord record = generateBillForMeter(
                            meter.getRoomId(),
                            meter.getMeterId(),
                            meterTypeWater,
                            cycleStartDate,
                            cycleEndDate,
                            waterRates
                    );
                    
                    // 4.3 保存账单
                    if (record != null) {
                        recordService.save(record);
                        generatedCount++;
                    }
                } catch (Exception e) {
                    log.error("【计费失败】生成水费账单失败 (MeterID: {}): {}", meter.getMeterId(), e.getMessage());
                }
            }
        }
        
        if (generatedCount == 0) {
            log.warn("【计费完成】未生成任何有效账单，请检查读数和费率。");
            return "未生成任何有效账单，请检查读数和费率。";
        }
        
        log.info("【计费完成】成功生成 {} 张账单，周期从 {} 到 {}", generatedCount, cycleStartDate, cycleEndDate);
        return "成功生成 " + generatedCount + " 张账单，周期从 " + cycleStartDate + " 到 " + cycleEndDate;
    }
    
    /**
     * 辅助方法：为单个表计生成账单
     * @param roomId 房间ID
     * @param meterId 表计ID
     * @param meterType 类型 (1或2)
     * @param cycleStartDate 周期开始
     * @param cycleEndDate 周期结束
     * @param rates 费率列表
     * @return 账单实体 (如果无需生成则返回 null)
     */
    private BizBillingRecord generateBillForMeter(Long roomId, Long meterId, String meterType,
                                                  LocalDate cycleStartDate, LocalDate cycleEndDate,
                                                  List<BizBillingRate> rates) {
        
        LocalDateTime startDateTime = cycleStartDate.atStartOfDay();
        // 结束时间需要包含 cycleEndDate 当天 23:59:59，所以查询时用 cycleEndDate + 1 天 00:00:00
        LocalDateTime endDateTime = cycleEndDate.plusDays(1).atStartOfDay();
        
        // 1. 幂等性校验：检查该账单是否已存在
        boolean exists = recordMapper.exists(new LambdaQueryWrapper<BizBillingRecord>()
                .eq(BizBillingRecord::getRoomId, roomId)
                .eq(BizBillingRecord::getMeterType, meterType)
                .eq(BizBillingRecord::getCycleEndDate, cycleEndDate)
        );
        
        if (exists) {
            log.warn("【跳过】账单 (Room: {}, Meter: {}, Type: {}, Period: {}) 已存在。",
                    roomId, meterId, meterType, cycleEndDate);
            return null;
        }
        
        // 2. 查找读数
        // 2.1 本次读数：周期内（含）的最新读数
        BizMeterReading currentReading = readingMapper.selectOne(new LambdaQueryWrapper<BizMeterReading>()
                .eq(BizMeterReading::getMeterId, meterId)
                .eq(BizMeterReading::getMeterType, meterType) // <-- 这里是 BizMeterReading
                .le(BizMeterReading::getReadingDate, endDateTime) // 小于等于周期结束
                .orderByDesc(BizMeterReading::getReadingDate)
                .last("LIMIT 1")
        );
        
        // 2.2 上次读数：周期开始（不含）之前的最新读数
        BizMeterReading previousReading = readingMapper.selectOne(new LambdaQueryWrapper<BizMeterReading>()
                .eq(BizMeterReading::getMeterId, meterId)
                //
                // --- 【【【【【 错误点修正 】】】】】 ---
                //
                // 错误代码: .eq(BizBillingRecord::getMeterType, meterType)
                // 正确代码:
                .eq(BizMeterReading::getMeterType, meterType) // <-- 这里必须使用 BizMeterReading
                //
                // --- 【【【【【 修正完毕 】】】】】 ---
                //
                .lt(BizMeterReading::getReadingDate, startDateTime) // 严格小于周期开始
                .orderByDesc(BizMeterReading::getReadingDate)
                .last("LIMIT 1")
        );
        
        // 3. 校验读数
        if (currentReading == null) {
            log.warn("【跳过】(MeterID: {}) 在周期 {} 内没有找到任何读数。", meterId, cycleEndDate);
            return null;
        }
        
        BigDecimal startValue = (previousReading == null) ? BigDecimal.ZERO : previousReading.getReadingValue();
        BigDecimal endValue = currentReading.getReadingValue();
        
        if (endValue.compareTo(startValue) < 0) {
            log.error("【跳过】(MeterID: {}) 读数错误：当前读数 {} 小于上次读数 {}。",
                    meterId, endValue, startValue);
            return null;
        }
        
        BigDecimal consumption = endValue.subtract(startValue);
        
        // 4. 计算金额 (调用简化的计费方法)
        BigDecimal amount = calculateAmount(consumption, meterType, rates);
        
        // 5. 创建账单实体
        BizBillingRecord record = new BizBillingRecord();
        record.setRoomId(roomId);
        record.setMeterType(meterType);
        record.setCycleStartDate(cycleStartDate);
        record.setCycleEndDate(cycleEndDate);
        record.setUnitConsumed(consumption); // 本周期消耗量
        record.setTotalAmount(amount);       // 总金额
        record.setIsPaid(0);                 // 0: 未支付
        record.setStatus("1");               // 1: 已出账 (待支付)
        
        return record;
    }
    
    
    /**
     * 计费核心：计算金额 (简化版，未实现阶梯)
     * 【保持原样】
     */
//    private BigDecimal calculateAmount(BigDecimal consumption, String meterType, List<BizBillingRate> rates) {
//        if (consumption.compareTo(BigDecimal.ZERO) <= 0 || rates == null || rates.isEmpty()) {
//            return BigDecimal.ZERO;
//        }
//        // 简化：使用第一个基础费率计算
//        BizBillingRate rateToUse = rates.get(0);
//        BigDecimal unitPrice = rateToUse.getUnitPrice();
//
//        // TODO: 实际生产中，这里应实现阶梯计费逻辑
//        // (例如，根据 consumption 和 rate 列表中的 threshold 进行复杂计算)
//
//        return consumption.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
//    }
    /**
     * 计费核心：计算金额 (V2: 实现阶梯计费)
     *
     * @param consumption 消耗量 (度/吨)
     * @param meterType   类型 (1或2)
     * @param rates       适用于该类型的所有费率规则
     * @return 总金额
     */
    private BigDecimal calculateAmount(BigDecimal consumption, String meterType, List<BizBillingRate> rates) {
        if (consumption.compareTo(BigDecimal.ZERO) <= 0 || CollUtil.isEmpty(rates)) {
            return BigDecimal.ZERO;
        }
        
        // 1. 按阶梯阈值升序排序 (确保 0, 100, 200... 的顺序)
        List<BizBillingRate> sortedRates = rates.stream()
                .filter(rate -> Objects.equals(rate.getMeterType(), meterType))
                // 确保 null 阈值 (基础费率) 排在最前面
                .sorted(Comparator.comparing(BizBillingRate::getThreshold, Comparator.nullsFirst(BigDecimal::compareTo)))
                .collect(Collectors.toList());
        
        if (CollUtil.isEmpty(sortedRates)) {
            log.warn("【计费警告】类型 {} 找不到匹配的费率，返回 0。", meterType);
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal remainingConsumption = consumption;
        BigDecimal lastThreshold = BigDecimal.ZERO;
        
        // 2. 遍历阶梯
        for (BizBillingRate rate : sortedRates) {
            BigDecimal currentThreshold = rate.getThreshold();
            BigDecimal unitPrice = rate.getUnitPrice();
            
            // 2.1 如果是基础费率 (threshold 为 null) 或用量未达阶梯
            if (currentThreshold == null) {
                // 如果是唯一的费率（非阶梯）
                if (sortedRates.size() == 1) {
                    totalAmount = consumption.multiply(unitPrice);
                    break; // 计算完毕
                }
                // 如果是阶梯的第1档（基础费率）
                lastThreshold = BigDecimal.ZERO;
                continue; // 继续到第一个有阈值的阶梯
            }
            
            // 2.2 计算本阶梯的用量
            // 本阶梯可用的量 = (当前阈值 - 上一阈值)
            BigDecimal tierConsumptionLimit = currentThreshold.subtract(lastThreshold);
            
            // 实际用量 = min(剩余用量, 本阶梯限量)
            BigDecimal tierConsumption = remainingConsumption.min(tierConsumptionLimit);
            
            // 2.3 累加金额
            totalAmount = totalAmount.add(tierConsumption.multiply(unitPrice));
            
            // 2.4 扣除已计算用量
            remainingConsumption = remainingConsumption.subtract(tierConsumption);
            lastThreshold = currentThreshold;
            
            // 2.5 如果剩余用量为 0，提前结束
            if (remainingConsumption.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }
        
        // 3. 处理超出最高阶梯的用量
        // 如果循环结束后仍有用量剩余，说明用量超过了最高阶梯，
        // 此时使用最后一个（即最高）费率来计算剩余部分。
        if (remainingConsumption.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal lastRatePrice = sortedRates.get(sortedRates.size() - 1).getUnitPrice();
            totalAmount = totalAmount.add(remainingConsumption.multiply(lastRatePrice));
        }
        
        return totalAmount.setScale(2, RoundingMode.HALF_UP);
    }
    
    
    /**
     * 处理账单支付成功后的状态更新
     * (此方法由 BizBillingRecordServiceImpl 调用，保持不变)
     */
    @Override
    @Transactional
    public void processPaymentSuccess(Long recordId, BigDecimal paidAmount) {
        // 调用 BizBillingRecordService 的支付处理方法
        BillingPaymentDTO paymentDTO = new BillingPaymentDTO();
        paymentDTO.setRecordId(recordId);
        paymentDTO.setPaidAmount(paidAmount);
        
        // 【修正】我们依赖 BizBillingRecordService 来处理核心支付事务
        recordService.processPayment(paymentDTO);
    }
}