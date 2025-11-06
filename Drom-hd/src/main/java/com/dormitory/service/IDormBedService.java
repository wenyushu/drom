package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.entity.DormBed;
import com.dormitory.dto.DormBedQueryDTO;

/**
 * 床位业务服务接口
 */
public interface IDormBedService extends IService<DormBed> {
    
    /**
     * 根据房间容量批量创建床位
     * @param roomId 房间ID
     * @param capacity 房间容量
     */
    void batchAddBeds(Long roomId, Integer capacity);
    
    /**
     * 批量删除床位 (含业务约束校验：is_occupied)
     * @param bedIds 床位ID数组
     */
    void deleteBedByIds(Long[] bedIds);
    
    /**
     * 分页查询床位列表
     */
    Page<DormBed> selectBedPage(DormBedQueryDTO queryDTO); // --- 2. 修改参数 ---
}