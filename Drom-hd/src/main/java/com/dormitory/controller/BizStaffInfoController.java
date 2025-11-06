package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.StaffInfoQueryDTO;
import com.dormitory.dto.StaffInfoUpdateDTO;
import com.dormitory.entity.BizStaffInfo;
import com.dormitory.service.IBizStaffInfoService;
import com.dormitory.utils.R;
import com.dormitory.vo.StaffInfoVO;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 教职工/后勤信息管理控制器 (最终完整版)
 */
@Tag(name = "教职工信息管理", description = "Admin/人事管理教职工（辅导员、后勤、导师等）信息")
@RestController
@RequestMapping("/api/staff/info")
public class BizStaffInfoController {
    
    @Autowired
    private IBizStaffInfoService staffInfoService;
    
    /**
     * 分页查询教职工列表
     */
    @Operation(summary = "教职工分页查询", description = "获取教职工详细信息列表")
    // @SaCheckPermission("staff:info:query") // 权限：查询
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<Page<StaffInfoVO>> list(@Valid StaffInfoQueryDTO queryDTO) {
        Page<StaffInfoVO> page = staffInfoService.selectStaffInfoPage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 获取单个教职工详细信息 (根据 UserID)
     */
    @Operation(summary = "获取教职工详情", description = "根据用户 ID 获取详细信息")
    // @SaCheckPermission("staff:info:query") // 权限：查询
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/{userId}")
    public R<StaffInfoVO> getInfo(@PathVariable Long userId) {
        StaffInfoVO vo = staffInfoService.getStaffInfoByUserId(userId);
        return R.ok(vo);
    }
    
    /**
     * 修改教职工信息 (管理员操作)
     * (使用 DTO 进行更新，避免 @NotNull 校验失败)
     */
    @Operation(summary = "修改教职工信息", description = "更新教职工的部门、住宿意愿、合同年限等信息")
    @SaCheckPermission("staff:info:edit") // 权限：修改
    @PutMapping
    public R<Void> update(@Valid @RequestBody StaffInfoUpdateDTO updateDTO) {
        staffInfoService.updateStaffInfo(updateDTO);
        return R.ok("教职工信息更新成功");
    }
    
    /**
     * 新增教职工信息 (用于同步 SysUser 新增的记录)
     */
    @Operation(summary = "新增教职工信息", description = "为新用户添加教职工档案")
    @SaCheckPermission("staff:info:add") // 权限：新增
    @PostMapping
    public R<Void> add(@Valid @RequestBody BizStaffInfo staffInfo) {
        // TODO: Service层需校验关联的用户ID是否已存在且是教职工/非学生类型
        staffInfoService.save(staffInfo);
        return R.ok("教职工档案新增成功");
    }
}