package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.SysCampus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 校区 Mapper 接口
 */
@Mapper
public interface SysCampusMapper extends BaseMapper<SysCampus> {
    
    // MyBatis-Plus 提供了基本的 CRUD，无需额外方法
}