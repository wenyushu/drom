package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.StuStudent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StuStudentMapper extends BaseMapper<StuStudent> {
    // MyBatis-Plus 提供基础 CRUD
}