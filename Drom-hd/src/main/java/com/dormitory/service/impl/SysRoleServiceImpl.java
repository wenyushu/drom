package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.RoleAddUpdateDTO;
import com.dormitory.dto.RoleQueryDTO;
import com.dormitory.entity.SysRole;
import com.dormitory.entity.SysUser;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.SysRoleMapper;
import com.dormitory.mapper.SysUserMapper;
import com.dormitory.service.ISysRoleService;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 角色业务服务实现类 (V2: 修复删除逻辑)
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    // --- 【修复】注入 SysUserMapper 用于校验 ---
    @Autowired
    private SysUserMapper userMapper;
    
    /**
     * 分页查询角色列表
     */
    @Override
    public Page<SysRole> selectRolePage(RoleQueryDTO queryDTO) {
        
        Page<SysRole> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        page.setSearchCount(true);
        
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.like(StrUtil.isNotEmpty(queryDTO.getRoleName()), SysRole::getRoleName, queryDTO.getRoleName())
                .eq(StrUtil.isNotEmpty(queryDTO.getRoleKey()), SysRole::getRoleKey, queryDTO.getRoleKey())
                .eq(queryDTO.getStatus() != null, SysRole::getStatus, queryDTO.getStatus())
                .orderByAsc(SysRole::getRoleSort);
        
        return this.page(page, wrapper);
    }
    
    /**
     * 新增角色并关联权限
     */
    @Override
    @Transactional
    public void addRole(RoleAddUpdateDTO addDTO) {
        // 1. 校验角色关键字唯一性
        if (checkRoleKeyUnique(addDTO.getRoleKey(), null)) {
            throw new BusinessException("新增角色失败，角色关键字已存在");
        }
        
        // 2. 转换并保存角色
        SysRole role = BeanUtil.copyProperties(addDTO, SysRole.class);
        this.save(role);
        
        // 3. 关联权限
        insertRoleMenu(role.getRoleId(), addDTO.getMenuIds());
    }
    
    /**
     * 修改角色信息及关联权限
     */
    @Override
    @Transactional
    public void updateRole(RoleAddUpdateDTO updateDTO) {
        if (updateDTO.getRoleId() == null) {
            throw new BusinessException("修改角色时，角色ID不能为空");
        }
        
        // 1. 校验角色关键字唯一性 (排除当前角色)
        if (checkRoleKeyUnique(updateDTO.getRoleKey(), updateDTO.getRoleId())) {
            throw new BusinessException("修改角色失败，角色关键字已存在");
        }
        
        // 2. 转换并更新角色
        SysRole role = BeanUtil.copyProperties(updateDTO, SysRole.class);
        this.updateById(role);
        
        // 3. 重新关联权限
        insertRoleMenu(role.getRoleId(), updateDTO.getMenuIds());
    }
    
    /**
     * 批量删除角色 (【修复】增加用户关联校验)
     */
    @Override
    @Transactional
    public void deleteRoleByIds(Long[] roleIds) {
        if (ArrayUtil.isEmpty(roleIds)) {
            throw new BusinessException("删除角色时，角色ID列表不能为空");
        }
        
        List<Long> roleIdList = Arrays.asList(roleIds);
        
        // 业务校验：不能删除超级管理员角色 (role_id=1)
        if (roleIdList.contains(1L)) {
            throw new BusinessException("超级管理员角色不允许删除");
        }
        
        // 1. 校验角色是否仍被用户使用
        boolean isRoleInUse = userMapper.exists(
                new LambdaQueryWrapper<SysUser>().in(SysUser::getRoleId, roleIdList)
        );
        if (isRoleInUse) {
            throw new BusinessException("删除失败：所选角色中至少有一个仍被用户关联，请先解绑用户");
        }
        // 校验结束
        
        // 2. 删除角色表记录 (MyBatis-Plus 自动逻辑删除)
        this.removeBatchByIds(roleIdList);
        
        // 3. 删除角色与菜单的关联记录
        for (Long roleId : roleIdList) {
            roleMapper.deleteRoleMenuByRoleId(roleId);
        }
    }
    
    /**
     * 插入角色与菜单权限的关联记录
     */
    private void insertRoleMenu(Long roleId, List<Long> menuIds) {
        // 1. 先清除旧关联
        roleMapper.deleteRoleMenuByRoleId(roleId);
        
        // 2. 插入新关联
        if (menuIds != null && !menuIds.isEmpty()) {
            roleMapper.batchInsertRoleMenu(roleId, menuIds);
        }
    }
    
    /**
     * 校验角色关键字是否唯一
     */
    private boolean checkRoleKeyUnique(String roleKey, Long roleId) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, roleKey);
        if (roleId != null) {
            wrapper.ne(SysRole::getRoleId, roleId);
        }
        return this.count(wrapper) > 0;
    }
}