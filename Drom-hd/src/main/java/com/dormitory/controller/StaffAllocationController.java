package com.dormitory.controller;

import com.dormitory.service.IStaffAllocationService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教职工/后勤宿舍分配控制器
 */
@Tag(name = "教职工宿舍分配", description = "Admin对教职工/后勤人员进行分配和迁出")
@RestController
@RequestMapping("/api/staff/allocation")
public class StaffAllocationController {
    
    @Autowired private IStaffAllocationService staffAllocationService;
    
    /**
     * 手动分配教职工到床位 (Admin 权限)
     */
    @Operation(summary = "手动分配教职工床位", description = "将指定教职工分配到指定空闲床位")
    @SaCheckPermission("staff:allocation:assign")
    @PostMapping("/assign/{staffUserId}/{bedId}")
    public R<Void> assign(@PathVariable Long staffUserId, @PathVariable Long bedId) {
        staffAllocationService.assignStaffToBed(staffUserId, bedId);
        return R.ok("教职工手动分配成功");
    }
    
    /**
     * 将教职工迁出床位 (Admin 权限)
     */
    @Operation(summary = "迁出教职工", description = "将教职工从当前床位迁出（离职/搬离）")
    @SaCheckPermission("staff:allocation:checkout")
    @PostMapping("/checkout/{staffUserId}")
    public R<Void> checkout(@PathVariable Long staffUserId, @RequestParam String reason) {
        staffAllocationService.checkoutStaffFromBed(staffUserId, reason);
        return R.ok("教职工迁出成功");
    }
    
    /**
     * 批量自动分配教职工 (Admin 权限)
     */
    @Operation(summary = "批量自动分配教职工", description = "为指定教职工列表自动分配宿舍")
    @SaCheckPermission("staff:allocation:auto")
    @PostMapping("/auto-allocate")
    public R<Map<Long, String>> autoAllocate(@RequestBody List<Long> staffUserIds) {
        Map<Long, String> results = staffAllocationService.allocateRoomsForStaff(staffUserIds);
        return R.ok("教职工批量分配完成", results);
    }
}