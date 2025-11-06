package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.LeaveStatusQueryDTO; // 导入 DTO
import com.dormitory.entity.StuLeaveStatus;

/**
 * 学生离校/留校状态业务服务接口
 */
public interface IStuLeaveStatusService extends IService<StuLeaveStatus> {
    
    /**
     * 分页查询学生状态列表 (含学生姓名和学号)
     */
    Page<StuLeaveStatus> selectStatusPage(LeaveStatusQueryDTO queryDTO);
    
    /**
     * 新增或更新学生状态 (含校验)
     */
    void saveOrUpdateStatus(StuLeaveStatus status);
}