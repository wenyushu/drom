package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.entity.SysDepartment;

import java.util.List;

/**
 * 部门/院系管理业务服务接口
 */
public interface ISysDepartmentService extends IService<SysDepartment> {
    
    /**
     * 删除部门 (包含业务校验)
     * @param deptId 部门ID
     */
    void deleteDepartment(Long deptId);
    
    /**
     * 新增方法：查询部门树形结构
     * @return 部门树列表
     */
    List<SysDepartment> selectDeptTree();
}