package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.utils.R;
import com.dormitory.entity.DormMeterWater;
import com.dormitory.service.IDormMeterWaterService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dormitory.dto.DormMeterWaterQueryDTO;
import jakarta.validation.Valid;

/**
 * 房间水表资产控制器
 */
@Tag(name = "房间水表管理", description = "水表资产的 CRUD 操作")
@RestController
@RequestMapping("/api/dorm/meter/water")
public class DormMeterWaterController {
    
    @Autowired
    private IDormMeterWaterService waterMeterService;
    
    /**
     * 分页查询 - 所有人可查
     */
    @Operation(summary = "水表分页查询", description = "获取水表资产列表，包含房间号")
    @GetMapping("/list")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    public R<Page<DormMeterWater>> page(@Valid DormMeterWaterQueryDTO queryDTO) {
        Page<DormMeterWater> result = waterMeterService.selectMeterPage(queryDTO);
        return R.ok(result);
    }
    
    /**
     * 新增水表 - 仅限 Admin/管理员
     */
    @Operation(summary = "新增水表", description = "为房间配置水表，确保一房一表")
    @SaCheckPermission("dorm:meter:water:add")
    @PostMapping
    public R<Void> add(@RequestBody DormMeterWater meter) {
        waterMeterService.addMeter(meter);
        return R.ok("水表新增成功");
    }
    
    /**
     * 修改水表信息 - 仅限 Admin/管理员
     */
    @Operation(summary = "修改水表信息", description = "修改水表型号、状态等信息")
    @SaCheckPermission("dorm:meter:water:edit")
    @PutMapping
    public R<Void> update(@RequestBody DormMeterWater meter) {
        waterMeterService.updateById(meter);
        return R.ok("水表信息修改成功");
    }
    
    /**
     * 批量删除水表 - 仅限 Admin/管理员
     */
    @Operation(summary = "批量删除水表", description = "删除水表资产")
    @SaCheckPermission("dorm:meter:water:remove")
    @DeleteMapping("/{meterIds}")
    public R<Void> delete(@PathVariable Long[] meterIds) {
        waterMeterService.deleteMeterByIds(meterIds);
        return R.ok("水表批量删除成功");
    }
}