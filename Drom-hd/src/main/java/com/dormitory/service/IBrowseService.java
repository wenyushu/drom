package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.entity.DormBuilding;
import com.dormitory.entity.DormFloor;
import com.dormitory.entity.SysCampus;
import com.dormitory.vo.BedBrowseVO;
import com.dormitory.vo.RoomBrowseVO;
import com.dormitory.dto.BuildingBrowseQueryDTO;
import com.dormitory.dto.RoomBrowseQueryDTO;

import java.util.List;

/**
 * 宿舍资源浏览(只读)服务接口
 */
public interface IBrowseService {
    
    /**
     * 1. 获取所有校区
     */
    List<SysCampus> getCampusList();
    
    /**
     * 2. 根据校区 ID 获取楼栋列表 (分页)
     */
    Page<DormBuilding> getBuildingPage(BuildingBrowseQueryDTO queryDTO);
    
    /**
     * 3. 根据楼栋 ID 获取楼层列表 (不分页)
     */
    List<DormFloor> getFloorsByBuilding(Long buildingId);
    
    /**
     * 4. 根据楼层 ID 获取房间列表 (分页)
     */
    Page<RoomBrowseVO> getRoomPageByFloor(RoomBrowseQueryDTO queryDTO);
    
    /**
     * 5. 根据房间 ID 获取床位列表 (含住户信息)
     */
    List<BedBrowseVO> getBedsByRoom(Long roomId, Long loginId);
}