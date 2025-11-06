package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.DormRoomAsset;
import org.apache.ibatis.annotations.Mapper;

/**
 * 房间固定资产 Mapper 接口
 */
@Mapper
public interface DormRoomAssetMapper extends BaseMapper<DormRoomAsset> {
}