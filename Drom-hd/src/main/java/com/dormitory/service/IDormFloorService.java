package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.DormFloorQueryDTO;
import com.dormitory.entity.DormFloor;

import java.util.List; // 导入 List

/**
 * 楼层业务服务接口
 */
public interface IDormFloorService extends IService<DormFloor> {
    
//    /**
//     * 分页查询楼层 (含楼栋名)
//     * @param page 分页对象
//     * @param buildingId 楼栋ID (用于过滤)
//     * @return
//     */
//    Page<DormFloor> selectFloorPage(Page<DormFloor> page, Long buildingId);
    
    /**
     * 分页查询楼层 (含楼栋名)
     * @param queryDTO 分页及查询参数
     * @return
     */
    Page<DormFloor> selectFloorPage(DormFloorQueryDTO queryDTO);
    
    /**
     * 根据楼栋 ID 获取楼层列表 (不分页)
     * @param buildingId 楼栋ID
     * @return
     */
    List<DormFloor> getFloorsByBuildingId(Long buildingId);
    
    /**
     * 新增楼层 (含校验)
     */
    void addFloor(DormFloor floor);
    
    /**
     * 删除楼层 (含校验)
     */
    void deleteFloor(Long floorId);
}