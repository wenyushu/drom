package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.LeaveStatusQueryDTO;
import com.dormitory.entity.StuLeaveStatus;
import com.dormitory.service.IStuLeaveStatusService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 学生离校/留校状态控制器
 */
@Tag(name = "学生状态管理", description = "管理学生的在校、离校、请假等状态")
@RestController
@RequestMapping("/api/stu/leave-status")
public class StuLeaveStatusController {
    
    @Autowired private IStuLeaveStatusService statusService;
    
    /**
     * 分页查询学生状态 (Admin/宿管/辅导员权限)
     */
    @Operation(summary = "学生状态分页查询", description = "查询学生的离校、留校等状态记录")
    // @SaCheckPermission("stu:leave-status:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<Page<StuLeaveStatus>> list(@Valid LeaveStatusQueryDTO queryDTO) {
        Page<StuLeaveStatus> page = statusService.selectStatusPage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 新增或更新学生状态 (Admin/宿管/辅导员权限)
     */
    @Operation(summary = "新增或更新学生状态", description = "更新学生的在校、离校、请假状态")
    @SaCheckPermission("stu:leave-status:edit")
    @PostMapping // 使用 PostMapping 同时处理新增和更新
    public R<Void> saveOrUpdate(@Valid @RequestBody StuLeaveStatus status) {
        statusService.saveOrUpdateStatus(status);
        return R.ok("学生状态更新成功");
    }
    
    // 通常不提供直接删除状态记录的接口
    // 建议直接将不再使用的用户数据的状态设置为禁用，以避免孤儿数据
}