package com.dormitory.auth; // 建议新建 auth 包

import cn.dev33.satoken.stp.StpInterface;
import com.dormitory.entity.SysRole;
import com.dormitory.entity.SysUser;
import com.dormitory.mapper.SysRoleMapper;
import com.dormitory.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限和角色数据加载实现类
 */
@Component
public class SaTokenService implements StpInterface {
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    // 我们需要一个 Mapper 来查询菜单/权限标识
    // 假设您已经创建了 SysMenuMapper，否则需要先创建它
    // @Autowired
    // private SysMenuMapper menuMapper;
    
    // --- 临时替代方案：由于我们还没有 SysMenuMapper，我们先在 SysUserMapper 中实现查询 ---
    // 为了不引入新的报错，我们先假设权限和角色是硬编码或通过简单查询获取的。
    
    /**
     * 根据账号 id 获取权限码列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        
        // --- 修正类型转换 ---
        Long userId = Long.valueOf(String.valueOf(loginId));
        
        // 使用新方法从数据库查询权限列表
        List<String> perms = userMapper.selectPermsByUserId(userId);
        
        // 假设 Admin 角色 (role_id=1) 应该有所有权限，以防数据库配置遗漏
        SysUser user = userMapper.selectById(userId);
        if (user != null && user.getRoleId() == 1L) {
            perms.add("*:*:*"); // Admin 通配符权限
        }
        
        // 过滤掉重复和空的权限标识
        return perms.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * 根据账号 id 获取角色标识列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        
        // 修正: 优先转换为 String，然后解析为 Long
        Long userId = Long.valueOf(String.valueOf(loginId));
        
        // 1. 根据 loginId (用户ID) 获取用户对象
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        
        // 2. 根据用户的 role_id 查询角色关键字 (role_key)
        SysRole role = roleMapper.selectById(user.getRoleId());
        
        if (role != null) {
            return List.of(role.getRoleKey());
        }
        
        return Collections.emptyList();
    }
}