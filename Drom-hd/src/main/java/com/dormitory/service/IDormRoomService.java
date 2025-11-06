package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.DormRoomQueryDTO;
import com.dormitory.dto.RoomStatusUpdateDTO; // <-- 【新增】导入
import com.dormitory.entity.DormRoom;

/**
 * 房间业务服务接口
 */
public interface IDormRoomService extends IService<DormRoom> {
    
    /** 分页查询房间列表 (包含楼栋名) */
    Page<DormRoom> selectRoomPage(DormRoomQueryDTO queryDTO);
    
    /** 新增房间 (Admin) */
    void addRoom(DormRoom room);
    
    /** 更新房间 (Admin 专属，可修改容量) */
    void updateRoomByAdmin(DormRoom room);
    
    /** 更新房间状态 (宿管/辅导员操作) */
    void updateRoomStatus(RoomStatusUpdateDTO dto);
    
    /** 批量删除房间 (Admin) */
    void deleteRoomByIds(Long[] roomIds);
}