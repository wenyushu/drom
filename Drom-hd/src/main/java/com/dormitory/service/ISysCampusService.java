package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.entity.SysCampus;

/**
 * 校区管理业务服务接口
 */
public interface ISysCampusService extends IService<SysCampus> {
    // IService 已包含基本 CRUD
    
    /**
     * 删除校区 (包含业务校验)
     * @param campusId 校区ID
     */
    void deleteCampus(Long campusId);
}