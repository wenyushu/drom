package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.MenuAddUpdateDTO;
import com.dormitory.dto.MenuQueryDTO;
import com.dormitory.entity.SysMenu;

import java.util.List;

/**
 * 菜单/权限业务服务接口
 */
public interface ISysMenuService extends IService<SysMenu> {
    
    /**
     * 查询菜单列表 (树形结构)
     */
    List<SysMenu> selectMenuList(MenuQueryDTO queryDTO);
    
    /**
     * 新增菜单
     */
    void addMenu(MenuAddUpdateDTO addDTO);
    
    /**
     * 修改菜单
     */
    void updateMenu(MenuAddUpdateDTO updateDTO);
    
    /**
     * 删除菜单 (需要校验是否有子菜单或被角色关联)
     */
    void deleteMenuById(Long menuId);
}