package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.RepairOrderQueryDTO;
import com.dormitory.dto.RepairOrderUpdateStatusDTO;
import com.dormitory.entity.BizRepairOrder;

/**
 * 报修工单业务服务接口
 */
public interface IBizRepairOrderService extends IService<BizRepairOrder> {
    
    /**
     * 分页查询工单列表 (含联查信息)
     */
    Page<BizRepairOrder> selectOrderPage(RepairOrderQueryDTO queryDTO);
    
    /**
     * 提交报修工单 (学生/宿管)
     */
    void submitOrder(BizRepairOrder order);
    
    /**
     * 更新工单状态/分配维修人员 (维修人员/管理员)
     */
    void updateOrderStatus(RepairOrderUpdateStatusDTO updateDTO);
}