package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.RoomChangeApprovalDTO;
import com.dormitory.dto.RoomChangeQueryDTO;
import com.dormitory.dto.RoomChangeRequestDTO;
import com.dormitory.entity.DormAllocationLog;
import com.dormitory.service.IDormAllocationService;
import com.dormitory.utils.R;
import com.dormitory.vo.RoomChangeRequestVO;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 宿舍分配与调宿控制器
 */
@Tag(name = "宿舍分配与调宿", description = "处理学生入住、迁出、分配和调宿申请")
@RestController
@RequestMapping("/api/allocation")
public class DormAllocationController {
    
    @Autowired private IDormAllocationService allocationService;

    
    @Operation(summary = "手动分配床位 (Admin)", description = "将指定学生分配到指定空闲床位")
    @SaCheckPermission("dorm:allocation:assign")
    @PostMapping("/assign/{studentId}/{bedId}")
    public R<DormAllocationLog> assign(@PathVariable Long studentId, @PathVariable Long bedId) {
        DormAllocationLog log = allocationService.assignStudentToBed(studentId, bedId);
        return R.ok("手动分配成功", log);
    }
    
    @Operation(summary = "迁出学生 (Admin)", description = "将学生从当前床位迁出（退宿/毕业/休学）")
    @SaCheckPermission("dorm:allocation:checkout")
    @PostMapping("/checkout/{studentId}")
    public R<Void> checkout(@PathVariable Long studentId,
                            @RequestParam String actionType,
                            @RequestParam String reason) {
        allocationService.checkoutStudentFromBed(studentId, actionType, reason);
        return R.ok("学生迁出成功");
    }
    
    @Operation(summary = "批量自动分配 (Admin)", description = "为指定学生列表自动分配宿舍")
    @SaCheckPermission("dorm:allocation:auto")
    @PostMapping("/auto-allocate")
    public R<Map<Long, String>> autoAllocate(@RequestBody List<Long> studentIds) {
        Map<Long, String> results = allocationService.allocateRoomsForStudents(studentIds);
        return R.ok("批量分配完成", results);
    }
    
    
    // =========================================================
    // SECTION 2: 学生/管理员调宿申请流程 (【修改】)
    // =========================================================
    
    @Operation(summary = "学生提交调宿申请", description = "学生本人提交调宿申请，等待审批")
    @SaCheckPermission("dorm:change:submit")
    @PostMapping("/request/submit")
    public R<Void> submitRoomChange(@Valid @RequestBody RoomChangeRequestDTO dto) {
        Long loginId = StpUtil.getLoginIdAsLong();
        allocationService.submitRoomChangeRequest(dto, loginId);
        return R.ok("调宿申请提交成功，请等待审批");
    }
    
    @Operation(summary = "管理员审批调宿申请", description = "辅导员/Admin 批准或驳回学生的调宿申请")
    @SaCheckPermission("dorm:change:approve")
    @PostMapping("/request/approve")
    public R<Void> approveRoomChange(@Valid @RequestBody RoomChangeApprovalDTO dto) {
        Long adminUserId = StpUtil.getLoginIdAsLong();
        allocationService.approveRoomChangeRequest(dto, adminUserId);
        return R.ok("调宿申请处理完成");
    }
    
    /**
     * 【【【【【 2. 已修改：使用 DTO 修复分页 】】】】】
     */
    @Operation(summary = "管理员查询调宿申请(分页)", description = "查询所有学生的调宿申请记录")
    @SaCheckPermission("dorm:change:query:all")
    @GetMapping("/request/list")
    public R<Page<RoomChangeRequestVO>> listRequests(@Valid RoomChangeQueryDTO queryDTO) { // <-- 修改参数
        Page<RoomChangeRequestVO> result = allocationService.selectRoomChangeRequestPage(queryDTO); // <-- 传递 DTO
        return R.ok(result);
    }
    
    @Operation(summary = "学生查询自己的调宿申请", description = "学生查询自己的历史调宿申请记录")
    @SaCheckPermission("dorm:change:query:my")
    @GetMapping("/request/my-list")
    public R<List<RoomChangeRequestVO>> getMyRequests() {
        Long loginId = StpUtil.getLoginIdAsLong();
        List<RoomChangeRequestVO> result = allocationService.getMyRoomChangeRequests(loginId);
        return R.ok(result);
    }
}