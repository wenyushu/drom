package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.StuLeaveStatus;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生离校/留校状态 Mapper 接口
 */
@Mapper
public interface StuLeaveStatusMapper extends BaseMapper<StuLeaveStatus> {
}