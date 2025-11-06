package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper 接口
 * 负责对 sys_role 表的 CRUD 操作，并包含角色-菜单关联表的维护方法。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
    
    /**
     * 清除角色与菜单/权限的所有关联
     * @param roleId 角色ID
     */
    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    void deleteRoleMenuByRoleId(Long roleId);
    
    /**
     * 批量新增角色与菜单/权限的关联
     * 注意：该方法的具体实现逻辑在 SysRoleMapper.xml 文件中
     * * @param roleId 角色ID
     * @param menuIds 权限/菜单ID列表
     */
    void batchInsertRoleMenu(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);
    
    // 新增方法：检查菜单 ID 是否在 sys_role_menu 表中被使用
    /**
     * 检查菜单ID是否被任何角色关联
     * @param menuId 菜单ID
     * @return 关联的角色数量
     */
    @Select("SELECT COUNT(1) FROM sys_role_menu WHERE menu_id = #{menuId}")
    long countMenuUsage(@Param("menuId") Long menuId);
}