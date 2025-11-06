package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.entity.DormBuilding;
import com.dormitory.entity.DormFloor;
import com.dormitory.entity.SysCampus;
import com.dormitory.service.IBrowseService;
import com.dormitory.utils.R;
import com.dormitory.vo.BedBrowseVO;
import com.dormitory.vo.RoomBrowseVO;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.dormitory.dto.BuildingBrowseQueryDTO;
import com.dormitory.dto.RoomBrowseQueryDTO;
import jakarta.validation.Valid;

/**
 * 宿舍资源浏览控制器 (只读)
 */
@Tag(name = "宿舍资源浏览 (公共)", description = "所有人按层级浏览宿舍资源")
@RestController
@RequestMapping("/api/browse")
public class BrowseController {
    
    @Autowired private IBrowseService browseService;
    
    @Operation(summary = "1. 获取校区列表")
    // @SaCheckPermission("dorm:browse:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/campus")
    public R<List<SysCampus>> getCampusList() {
        return R.ok(browseService.getCampusList());
    }
    
    @Operation(summary = "2. 获取楼栋列表 (分页)")
    // @SaCheckPermission("dorm:browse:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/buildings")
    public R<Page<DormBuilding>> getBuildingPage(@Valid BuildingBrowseQueryDTO queryDTO) {
        return R.ok(browseService.getBuildingPage(queryDTO));
    }
    
    @Operation(summary = "3. 获取楼层列表")
    // @SaCheckPermission("dorm:browse:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/floors/{buildingId}")
    public R<List<DormFloor>> getFloors(@PathVariable Long buildingId) {
        return R.ok(browseService.getFloorsByBuilding(buildingId));
    }
    
    @Operation(summary = "4. 获取房间列表 (分页)")
    // @SaCheckPermission("dorm:browse:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/rooms")
    public R<Page<RoomBrowseVO>> getRooms(@Valid RoomBrowseQueryDTO queryDTO) {
        return R.ok(browseService.getRoomPageByFloor(queryDTO));
    }
    
    @Operation(summary = "5. 获取床位列表 (含住户信息)", description = "核心：根据登录者权限返回不同级别的住户信息")
    // @SaCheckPermission("dorm:browse:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/beds/{roomId}")
    public R<List<BedBrowseVO>> getBeds(@PathVariable Long roomId) {
        Long loginId = StpUtil.getLoginIdAsLong();
        return R.ok(browseService.getBedsByRoom(roomId, loginId));
    }
}