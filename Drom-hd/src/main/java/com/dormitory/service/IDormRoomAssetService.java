package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.RoomAssetQueryDTO;
import com.dormitory.entity.DormRoomAsset;

/**
 * 房间固定资产业务服务接口
 */
public interface IDormRoomAssetService extends IService<DormRoomAsset> {
    
    /**
     * 分页查询房间资产列表 (含房间号)
     */
    Page<DormRoomAsset> selectAssetPage(RoomAssetQueryDTO queryDTO);
    
    /**
     * 新增资产 (含房间校验)
     */
    void addAsset(DormRoomAsset asset);
    
    /**
     * 批量删除资产 (需校验是否关联未完成的报修单)
     */
    void deleteAssetByIds(Long[] assetIds);
}