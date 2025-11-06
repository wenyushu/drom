package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.entity.DormBuilding;
import com.dormitory.entity.SysCampus;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.DormBuildingMapper;
import com.dormitory.mapper.SysCampusMapper;
import com.dormitory.service.ISysCampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 校区管理业务服务实现类
 */
@Service
public class SysCampusServiceImpl extends ServiceImpl<SysCampusMapper, SysCampus> implements ISysCampusService {
    
    // 基础 CRUD 已由 ServiceImpl 提供
    
    
    // 可在此添加唯一性校验等业务逻辑
    
    // 新增：注入楼栋 Mapper
    @Autowired
    private DormBuildingMapper buildingMapper;
    
    /**
     * 新增：删除校区 (含业务校验)
     * @param campusId 校区ID
     */
    public void deleteCampus(Long campusId) {
        // 1. 校验校区下是否有关联楼栋
        boolean hasBuildings = buildingMapper.exists(
                new LambdaQueryWrapper<DormBuilding>().eq(DormBuilding::getCampusId, campusId)
        );
        
        if (hasBuildings) {
            throw new BusinessException("删除失败：该校区下仍有关联的楼栋！");
        }
        
        // 2. 执行删除
        this.removeById(campusId);
    }
}