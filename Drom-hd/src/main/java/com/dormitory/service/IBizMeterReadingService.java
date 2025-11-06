package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.MeterReadingQueryDTO;
import com.dormitory.entity.BizMeterReading;

/**
 * 水/电表读数记录业务服务接口
 */
public interface IBizMeterReadingService extends IService<BizMeterReading> {
    
    /**
     * 分页查询读数记录 (含表计编号和房间号)
     */
    Page<BizMeterReading> selectReadingPage(MeterReadingQueryDTO queryDTO);
    
    /**
     * 新增读数记录 (含校验)
     */
    void addReading(BizMeterReading reading);
    
    // 删除逻辑通常不直接提供给普通用户，由系统或Admin批量处理
}