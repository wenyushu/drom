package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.AlertResolveDTO;
import com.dormitory.dto.ElectricAlertQueryDTO; // 需要创建此 DTO
import com.dormitory.entity.BizElectricAlert;

/**
 * 违规用电告警业务服务接口
 */
public interface IBizElectricAlertService extends IService<BizElectricAlert> {
    
    /**
     * 分页查询告警记录 (含房间号)
     */
    Page<BizElectricAlert> selectAlertPage(ElectricAlertQueryDTO queryDTO);
    
    /**
     * 新增告警记录 (通常由系统触发或 Admin 录入)
     */
    void addAlert(BizElectricAlert alert);
    
    /**
     * 标记告警为已处理
     */
    void resolveAlert(AlertResolveDTO resolveDTO);
}