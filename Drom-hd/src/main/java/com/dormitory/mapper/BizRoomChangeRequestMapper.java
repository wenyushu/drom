package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.BizRoomChangeRequest;
import org.apache.ibatis.annotations.Mapper;

/**
 * 调宿申请 Mapper 接口
 * (BizRoomChangeRequestMapper.xml 文件中可以定义复杂联查)
 */
@Mapper
public interface BizRoomChangeRequestMapper extends BaseMapper<BizRoomChangeRequest> {
}