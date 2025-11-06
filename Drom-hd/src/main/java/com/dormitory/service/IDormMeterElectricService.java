package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.DormMeterElectricQueryDTO;
import com.dormitory.entity.DormMeterElectric;

public interface IDormMeterElectricService extends IService<DormMeterElectric> {
    
    /**
     * 分页查询电表列表 (含房间信息)
     */
    Page<DormMeterElectric> selectMeterPage(DormMeterElectricQueryDTO queryDTO);
    
    /**
     * 新增电表 (含一房一表约束校验)
     */
    void addMeter(DormMeterElectric meter);
    
    /**
     * 批量删除电表 (需级联删除读数和费用记录)
     */
    void deleteMeterByIds(Long[] meterIds);
}