package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.DormRoomQueryDTO;
import com.dormitory.dto.RoomStatusUpdateDTO;
import com.dormitory.entity.DormRoom;
import com.dormitory.service.IDormRoomService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 宿舍房间管理控制器
 */
@Tag(name = "宿舍房间管理", description = "Admin和宿管对宿舍房间进行管理")
@RestController
@RequestMapping("/api/dorm/room")
public class DormRoomController {
    
    @Autowired
    private IDormRoomService roomService;
    
    /**
     * 分页查询房间列表 (所有人可查)
     */
    @Operation(summary = "房间分页查询", description = "获取房间列表，包含楼栋信息")
    // @SaCheckPermission("dorm:room:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<Page<DormRoom>> list(@Valid DormRoomQueryDTO queryDTO) {
        Page<DormRoom> page = roomService.selectRoomPage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 新增房间 (Admin专属)
     */
    @Operation(summary = "新增房间 (Admin专属)", description = "创建房间资产，设置房间容量和用途")
    @SaCheckPermission("dorm:room:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody DormRoom room) {
        roomService.addRoom(room);
        return R.ok("房间新增成功");
    }
    
    /**
     * Admin 修改房间【所有信息】(包括容量、用途)
     */
    @Operation(summary = "修改房间(Admin)", description = "Admin修改房间信息、容量、用途等")
    @SaCheckPermission("dorm:room:edit") // 权限：dorm:room:edit
    @PutMapping
    public R<Void> updateByAdmin(@Valid @RequestBody DormRoom room) {
        roomService.updateRoomByAdmin(room); // 调用 V1 的完整更新方法
        return R.ok("房间信息修改成功");
    }
    
    /**
     * 宿管/辅导员修改房间【状态】(封禁/解封)
     */
    @Operation(summary = "修改房间状态(宿管/辅导员)", description = "宿管/辅导员封禁或解封房间（如维修）")
    @SaCheckPermission("dorm:room:status:edit") // 权限：dorm:room:status:edit
    @PutMapping("/status")
    public R<Void> updateStatus(@Valid @RequestBody RoomStatusUpdateDTO dto) {
        roomService.updateRoomStatus(dto);
        return R.ok("房间状态更新成功");
    }
    
    /**
     * 批量删除房间 (Admin专属)
     */
    @Operation(summary = "批量删除房间 (Admin专属)", description = "删除房间资产，有住户时会失败")
    @SaCheckPermission("dorm:room:remove")
    @DeleteMapping("/{roomIds}")
    public R<Void> remove(@PathVariable Long[] roomIds) {
        roomService.deleteRoomByIds(roomIds);
        return R.ok("房间删除成功");
    }
}