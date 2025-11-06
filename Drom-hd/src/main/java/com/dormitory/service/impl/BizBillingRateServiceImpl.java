package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.entity.BizBillingRate;
import com.dormitory.entity.BizBillingRecord;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.BizBillingRateMapper;
import com.dormitory.mapper.BizBillingRecordMapper;
import com.dormitory.service.IBizBillingRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 计费费率业务服务实现类 (V2: 补充校验)
 */
@Service
public class BizBillingRateServiceImpl
        extends ServiceImpl<BizBillingRateMapper, BizBillingRate>
        implements IBizBillingRateService {
    
    // 【【【【【 新增：注入账单 Mapper 】】】】】
    @Autowired
    private BizBillingRecordMapper billingRecordMapper;
    
    /**
     * 新增：实现带校验的新增
     * 新增费率 (含唯一性校验)
     */
    @Override
    public void addRate(BizBillingRate rate) {
        // 1. 校验：同一类型、同一生效日期的费率不能重复
        // (如果启用了阶梯计费，这里的校验应该更复杂，目前简化为类型+日期唯一)
        boolean exists = this.exists(new LambdaQueryWrapper<BizBillingRate>()
                .eq(BizBillingRate::getMeterType, rate.getMeterType())
                .eq(BizBillingRate::getValidFrom, rate.getValidFrom())
        );
        if (exists) {
            throw new BusinessException("新增失败：已存在相同类型和生效日期的费率");
        }
        
        this.save(rate);
    }
    
    /**
     * 新增：实现带校验的删除
     * 删除费率 (含账单关联校验)
     */
    @Override
    public void deleteRate(Long rateId) {
        BizBillingRate rate = this.getById(rateId);
        if (rate == null) {
            throw new BusinessException("删除失败：费率不存在");
        }
        
        // 1. 校验：如果费率已被账单使用（通常不允许删除历史费率）
        // (简化校验：只检查是否存在使用该费率类型的账单)
        // (严格校验：应检查账单的 cycle_start_date 是否晚于 rate.validFrom)
        boolean inUse = billingRecordMapper.exists(
                new LambdaQueryWrapper<BizBillingRecord>()
                        .eq(BizBillingRecord::getMeterType, rate.getMeterType())
                        .ge(BizBillingRecord::getCycleStartDate, rate.getValidFrom())
        );
        
        if (inUse) {
            throw new BusinessException("删除失败：该费率或其类型已被历史账单使用，无法删除");
        }
        
        this.removeById(rateId);
    }
}