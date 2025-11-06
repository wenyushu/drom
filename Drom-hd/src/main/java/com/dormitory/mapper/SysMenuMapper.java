package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.SysMenu; // 假设你已创建 SysMenu 实体
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限/菜单 Mapper 接口
 * 负责 sys_menu 表的数据访问。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    // 权限查询逻辑已经放在 SysUserMapper 中，这里先留空，保持 Mapper 接口存在即可。
}