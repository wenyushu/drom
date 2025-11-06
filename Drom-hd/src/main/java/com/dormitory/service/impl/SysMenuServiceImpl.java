package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.MenuAddUpdateDTO;
import com.dormitory.dto.MenuQueryDTO;
import com.dormitory.entity.SysMenu;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.SysMenuMapper;
import com.dormitory.mapper.SysRoleMapper; // 用于权限关联校验
import com.dormitory.service.ISysMenuService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单/权限业务服务实现类
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {
    
    @Autowired
    private SysMenuMapper menuMapper;
    
    @Autowired
    private SysRoleMapper roleMapper; // 用于删除时校验是否被角色关联
    
    /**
     * 查询菜单列表，并构建树形结构
     */
    @Override
    public List<SysMenu> selectMenuList(MenuQueryDTO queryDTO) {

        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotEmpty(queryDTO.getMenuName()), SysMenu::getMenuName, queryDTO.getMenuName())
                .eq(StrUtil.isNotEmpty(queryDTO.getMenuType()), SysMenu::getMenuType, queryDTO.getMenuType())
                .like(StrUtil.isNotEmpty(queryDTO.getPerms()), SysMenu::getPerms, queryDTO.getPerms())
                .orderByAsc(SysMenu::getParentId)
                .orderByAsc(SysMenu::getMenuSort);
        
        List<SysMenu> menuList = this.list(wrapper);
        
        return buildMenuTree(menuList);
    }
    
    /**
     * 核心方法：递归构建菜单树
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menuList) {

        List<SysMenu> rootNodes = menuList.stream()
                .filter(menu -> menu.getParentId() == 0L)
                .collect(Collectors.toList());
        
        rootNodes.forEach(root -> root.setChildren(getChildren(root, menuList)));
        
        rootNodes.sort(Comparator.comparing(SysMenu::getMenuSort, Comparator.nullsFirst(Comparator.naturalOrder())));
        
        return rootNodes;
    }
    
    /**
     * 递归获取子节点
     */
    private List<SysMenu> getChildren(SysMenu parent, List<SysMenu> menuList) {

        List<SysMenu> children = menuList.stream()
                .filter(menu -> parent.getMenuId().equals(menu.getParentId()))
                .peek(menu -> menu.setChildren(getChildren(menu, menuList)))
                .sorted(Comparator.comparing(SysMenu::getMenuSort, Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        
        return children.isEmpty() ? null : children;
    }
    
    /**
     * 新增菜单
     */
    @Override
    public void addMenu(MenuAddUpdateDTO addDTO) {

        if (checkPermsUnique(addDTO.getPerms(), null)) {
            throw new BusinessException("新增菜单失败，权限标识已存在");
        }
        
        SysMenu menu = BeanUtil.copyProperties(addDTO, SysMenu.class);
        this.save(menu);
    }
    
    /**
     * 修改菜单
     */
    @Override
    public void updateMenu(MenuAddUpdateDTO updateDTO) {

        if (updateDTO.getMenuId() == null) {
            throw new BusinessException("修改菜单时，菜单ID不能为空");
        }
        if (checkPermsUnique(updateDTO.getPerms(), updateDTO.getMenuId())) {
            throw new BusinessException("修改菜单失败，权限标识已存在");
        }
        
        SysMenu menu = BeanUtil.copyProperties(updateDTO, SysMenu.class);
        this.updateById(menu);
    }
    
    /**
     * 删除菜单
     */
    @Override
    @Transactional
    public void deleteMenuById(Long menuId) {
        // 1. 校验是否存在子菜单
        if (count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, menuId)) > 0) {
            throw new BusinessException("删除失败，该菜单/目录存在子菜单或按钮");
        }
        
        // 2. 【【【【【 修改：校验是否被角色关联 】】】】】
        // 使用我们刚在 SysRoleMapper 中添加的新方法
        if (roleMapper.countMenuUsage(menuId) > 0) {
            throw new BusinessException("删除失败，该菜单/权限已被角色关联，请先解除角色关联");
        }
        
        // 3. 执行删除
        this.removeById(menuId);
    }
    
    /**
     * 校验权限标识是否唯一
     */
    private boolean checkPermsUnique(String perms, Long menuId) {
        // ... (此方法保持不变) ...
        if (StrUtil.isBlank(perms)) {
            return false;
        }
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getPerms, perms);
        if (menuId != null) {
            wrapper.ne(SysMenu::getMenuId, menuId);
        }
        return this.count(wrapper) > 0;
    }
}