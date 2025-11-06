package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.entity.BizBillingRate;

/**
 * 计费费率业务服务接口
 */
public interface IBizBillingRateService extends IService<BizBillingRate> {
    // 基础 CRUD 由 IService 提供
    
    /**
     * 新增：带校验的新增
     * 新增费率 (含唯一性校验)
     */
    void addRate(BizBillingRate rate);
    
    /**
     * 新增：带校验的删除
     * 删除费率 (含账单关联校验)
     */
    void deleteRate(Long rateId);
}