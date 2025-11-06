package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.entity.*;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.*;
import com.dormitory.service.IBrowseService;
import com.dormitory.vo.BedBrowseVO;
import com.dormitory.vo.RoomBrowseVO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.dormitory.dto.BuildingBrowseQueryDTO;
import com.dormitory.dto.RoomBrowseQueryDTO;

/**
 * 宿舍资源浏览(只读)服务实现
 */
@Service
public class BrowseServiceImpl implements IBrowseService {
    
    @Autowired private SysCampusMapper campusMapper;
    @Autowired private DormBuildingMapper buildingMapper;
    @Autowired private DormFloorMapper floorMapper;
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private DormBedMapper bedMapper;
    @Autowired private SysUserMapper userMapper;
    
    @Override
    public List<SysCampus> getCampusList() {
        // 1. 获取所有启用的校区
        return campusMapper.selectList(new LambdaQueryWrapper<SysCampus>()
                .eq(SysCampus::getStatus, 0));
    }
    
    @Override
    public Page<DormBuilding> getBuildingPage(BuildingBrowseQueryDTO queryDTO) { // --- 2. 修改参数 ---
        
        // --- 3. 核心修正：在这里创建 Page 对象 ---
        Page<DormBuilding> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 2. 返回指定校区下所有启用的楼栋
        return buildingMapper.selectPage(page, new LambdaQueryWrapper<DormBuilding>() // --- 4. 传递 page ---
                .eq(DormBuilding::getCampusId, queryDTO.getCampusId()) // --- 5. 使用 DTO 字段 ---
                .eq(DormBuilding::getStatus, 0)
                .orderByAsc(DormBuilding::getBuildingName));
    }
    
    @Override
    public List<DormFloor> getFloorsByBuilding(Long buildingId) {
        // ... (省略未修改的代码)
        List<DormFloor> floors = floorMapper.selectList(new LambdaQueryWrapper<DormFloor>()
                .eq(DormFloor::getBuildingId, buildingId)
                .orderByAsc(DormFloor::getFloorNumber));
        
        if (CollUtil.isNotEmpty(floors)) {
            DormBuilding building = buildingMapper.selectById(buildingId);
            if (building != null) {
                floors.forEach(floor -> floor.setBuildingName(building.getBuildingName()));
            }
        }
        return floors;
    }
    
    /**
     * 【V5.2 核心修正】4. 根据楼层 ID 获取房间列表 (分页)
     */
    @Override
    public Page<RoomBrowseVO> getRoomPageByFloor(RoomBrowseQueryDTO queryDTO) { // --- 6. 修改参数 ---
        
        // --- 7. 核心修正：创建 数据库查询用的 Page ---
        Page<DormRoom> dbPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 1. 查询基础房间数据
        LambdaQueryWrapper<DormRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DormRoom::getFloorId, queryDTO.getFloorId()) // --- 8. 使用 DTO 字段 ---
                .orderByAsc(DormRoom::getRoomNumber);
        roomMapper.selectPage(dbPage, wrapper);
        
        // --- 9. 核心修正：创建 VO 返回用的 Page ---
        Page<RoomBrowseVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setTotal(dbPage.getTotal());
        voPage.setPages(dbPage.getPages());
        
        if (CollUtil.isEmpty(dbPage.getRecords())) {
            voPage.setRecords(Collections.emptyList());
            return voPage; // --- 10. 返回 voPage ---
        }
        
        // 3. 【【联查填充逻辑】】
        // 3.1 获取楼层信息
        DormFloor floor = floorMapper.selectById(queryDTO.getFloorId()); // --- 11. 使用 DTO 字段 ---
        if (floor == null) {
            throw new BusinessException("楼层信息不存在");
        }
        
        // 3.2 获取楼栋信息
        DormBuilding building = buildingMapper.selectById(floor.getBuildingId());
        if (building == null) {
            throw new BusinessException("楼栋信息不存在");
        }
        
        // 4. 转换并填充 VO
        List<RoomBrowseVO> voList = dbPage.getRecords().stream().map(room -> {
            RoomBrowseVO vo = BeanUtil.copyProperties(room, RoomBrowseVO.class);
            
            // 填充楼层和楼栋信息
            vo.setFloorNumber(floor.getFloorNumber());
            vo.setFloorName(floor.getFloorName());
            vo.setBuildingId(building.getBuildingId());
            vo.setBuildingName(building.getBuildingName());
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage; // --- 12. 返回 voPage ---
    }
    
    /**
     * 5. 根据房间ID获取床位列表 (含住户信息权限控制)
     */
    @Override
    public List<BedBrowseVO> getBedsByRoom(Long roomId, Long loginId) {
        SysUser currentUser = userMapper.selectById(loginId);
        if (currentUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        List<Long> adminRoles = List.of(1L, 2L, 3L);
        boolean hasAdminPermission = adminRoles.contains(currentUser.getRoleId());
        
        List<DormBed> beds = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                .eq(DormBed::getRoomId, roomId)
                .orderByAsc(DormBed::getBedNumber));
        
        if (beds.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<Long> occupantUserIds = beds.stream()
                .map(DormBed::getOccupantUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, SysUser> occupantUserMap = occupantUserIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(occupantUserIds).stream()
                        .collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
        
        return beds.stream().map(bed -> {
            BedBrowseVO vo = new BedBrowseVO();
            BeanUtil.copyProperties(bed, vo, "occupantName", "occupantSex"); // 复制基础字段
            
            if (bed.getIsOccupied() == 1 && bed.getOccupantUserId() != null) {
                if (hasAdminPermission) {
                    SysUser occupant = occupantUserMap.get(bed.getOccupantUserId());
                    if (occupant != null) {
                        vo.setOccupantName(occupant.getRealName());
                        vo.setOccupantSex(occupant.getSex());
                    } else {
                        vo.setOccupantName("数据异常");
                    }
                } else {
                    vo.setOccupantName("已占用");
                }
            } else {
                vo.setOccupantName("空闲");
            }
            return vo;
        }).collect(Collectors.toList());
    }
}