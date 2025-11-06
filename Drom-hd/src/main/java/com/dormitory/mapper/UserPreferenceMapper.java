package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.UserPreference;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生住宿偏好 Mapper 接口
 */
@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {
}