package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.RoleAddUpdateDTO;
import com.dormitory.dto.RoleQueryDTO;
import com.dormitory.entity.SysRole;

/**
 * 角色业务服务接口
 * 我们将实现角色的 CRUD 操作，并处理角色与其权限（sys_role_menu 表）的关联逻辑。
 */
public interface ISysRoleService extends IService<SysRole> {
    
    /**
     * 分页查询角色列表
     */
    Page<SysRole> selectRolePage(RoleQueryDTO queryDTO);
    
    /**
     * 新增角色并关联权限
     */
    void addRole(RoleAddUpdateDTO addDTO);
    
    /**
     * 修改角色信息及关联权限
     */
    void updateRole(RoleAddUpdateDTO updateDTO);
    
    /**
     * 批量删除角色 (逻辑删除)
     */
    void deleteRoleByIds(Long[] roleIds);
}