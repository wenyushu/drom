package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.DormRoom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 宿舍房间 Mapper 接口
 */
@Mapper
public interface DormRoomMapper extends BaseMapper<DormRoom> {
    
    /**
     * 原子性地增加房间入住人数 (+1)
     * 具体 SQL 实现在 DormRoomMapper.xml 文件中
     * @param roomId 房间ID
     * @return 影响的行数
     */
    int increaseOccupancy(@Param("roomId") Long roomId);
    
    /**
     * 原子性地减少房间入住人数 (-1)
     * 具体 SQL 实现在 DormRoomMapper.xml 文件中
     * @param roomId 房间ID
     * @return 影响的行数
     */
    int decreaseOccupancy(@Param("roomId") Long roomId);
    
}