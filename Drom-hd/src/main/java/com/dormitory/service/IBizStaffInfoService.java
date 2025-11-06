package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.StaffInfoQueryDTO;
import com.dormitory.dto.StaffInfoUpdateDTO; // 导入更新 DTO
import com.dormitory.entity.BizStaffInfo;
import com.dormitory.vo.StaffInfoVO;

/**
 * 教职工/后勤住宿信息业务服务接口 (修正版)
 */
public interface IBizStaffInfoService extends IService<BizStaffInfo> {
    
    /**
     * 分页查询教职工列表 (复杂联查，返回 VO)
     */
    Page<StaffInfoVO> selectStaffInfoPage(StaffInfoQueryDTO queryDTO);
    
    /**
     * 根据 UserID 获取教职工详细信息 (返回 VO)
     */
    StaffInfoVO getStaffInfoByUserId(Long userId);
    
    /**
     * 更新教职工信息 (Admin/人事 操作)
     * 【核心修正】：参数类型改为 StaffInfoUpdateDTO
     */
    void updateStaffInfo(StaffInfoUpdateDTO updateDTO);
}