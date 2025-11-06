package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.BillingRecordQueryDTO;
import com.dormitory.dto.BillingPaymentDTO; // 导入 DTO
import com.dormitory.entity.BizBillingRecord;

/**
 * 账单记录业务服务接口
 */
public interface IBizBillingRecordService extends IService<BizBillingRecord> {
    
    /**
     * 分页查询账单记录 (含房间号、楼栋名)
     */
    Page<BizBillingRecord> selectRecordPage(BillingRecordQueryDTO queryDTO);
    
    /**
     * 处理账单支付逻辑
     */
    void processPayment(BillingPaymentDTO paymentDTO);
}