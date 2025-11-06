package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.entity.DormBed;
import com.dormitory.service.IDormBedService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dormitory.dto.DormBedQueryDTO;
import jakarta.validation.Valid;

/**
 * 床位资产管理控制器
 */
@Tag(name = "床位资产管理", description = "Admin 对床位进行创建、修改和删除")
@RestController
@RequestMapping("/api/dorm/bed")
public class DormBedController {
    
    @Autowired
    private IDormBedService bedService;
    
    /**
     * 根据房间 ID 和容量批量创建床位 (A, B, C...)
     */
    @Operation(summary = "批量新增床位", description = "根据房间 ID 和容量批量创建床位资产")
    @SaCheckPermission("dorm:bed:add")
    @PostMapping("/batch/{roomId}/{capacity}")
    public R<Void> batchAdd(@PathVariable Long roomId, @PathVariable Integer capacity) {
        bedService.batchAddBeds(roomId, capacity);
        return R.ok("床位批量新增成功，共 " + capacity + " 个");
    }
    
    /**
     * 分页查询床位列表
     */
    @Operation(summary = "床位分页查询", description = "获取床位列表，包含房间和住户信息")
    // @SaCheckPermission("dorm:bed:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<Page<DormBed>> list(@Valid DormBedQueryDTO queryDTO) {
        Page<DormBed> result = bedService.selectBedPage(queryDTO);
        return R.ok(result);
    }
    
    /**
     * 批量删除床位 (含业务约束校验)
     */
    @Operation(summary = "批量删除床位", description = "删除床位资产，有住户时会失败")
    @SaCheckPermission("dorm:bed:remove")
    @DeleteMapping("/{bedIds}")
    public R<Void> remove(@PathVariable Long[] bedIds) {
        bedService.deleteBedByIds(bedIds);
        return R.ok("床位批量删除成功");
    }
}