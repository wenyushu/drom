package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    /**
     * 根据用户名查询用户详细信息
     * @param username 用户名
     * @return SysUser
     */
    SysUser selectUserByUsername(String username);
    
    
    
    /**
     * 根据用户ID查询用户拥有的所有权限标识 (perms)
     * 【优化后的 SQL：只联查 role_menu 和 menu 表，并通过子查询获取用户的 role_id】
     */
    @Select("SELECT m.perms FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
            "WHERE rm.role_id = (SELECT role_id FROM sys_user WHERE user_id = #{userId}) " +
            "AND m.perms IS NOT NULL AND m.perms != ''")
    List<String> selectPermsByUserId(Long userId);
}