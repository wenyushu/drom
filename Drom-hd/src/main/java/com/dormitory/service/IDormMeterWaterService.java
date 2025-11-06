package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.DormMeterWaterQueryDTO;
import com.dormitory.entity.DormMeterWater;

public interface IDormMeterWaterService extends IService<DormMeterWater> {
    
    /**
     * 分页查询水表列表 (含房间信息)
     */
    Page<DormMeterWater> selectMeterPage(DormMeterWaterQueryDTO queryDTO);
    
    /**
     * 新增水表 (含一房一表约束校验)
     */
    void addMeter(DormMeterWater meter);
    
    /**
     * 批量删除水表 (需级联删除读数和费用记录)
     */
    void deleteMeterByIds(Long[] meterIds);
}