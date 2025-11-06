package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.DormBuildingQueryDTO;
import com.dormitory.entity.DormBuilding;

/**
 * 楼栋业务服务接口
 */
public interface IDormBuildingService extends IService<DormBuilding> {
    
    /**
     * 分页查询楼栋列表 (包含关联的校区和宿管信息)
     */
    Page<DormBuilding> selectBuildingPage(DormBuildingQueryDTO queryDTO);
    
    /**
     * 新增楼栋
     */
    void addBuilding(DormBuilding building);
    
    /**
     * 修改楼栋
     */
    void updateBuilding(DormBuilding building);
    
    /**
     * 批量删除楼栋 (含业务约束校验)
     */
    void deleteBuildingByIds(Long[] buildingIds);
}