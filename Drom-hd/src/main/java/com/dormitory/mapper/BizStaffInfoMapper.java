package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.BizStaffInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教职工/后勤住宿信息 Mapper 接口
 */
@Mapper
public interface BizStaffInfoMapper extends BaseMapper<BizStaffInfo> {
}