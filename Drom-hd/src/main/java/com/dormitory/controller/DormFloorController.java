package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.entity.DormFloor;
import com.dormitory.service.IDormFloorService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.dormitory.dto.DormFloorQueryDTO;

/**
 * 楼层管理控制器 (Admin 专属)
 */
@Tag(name = "楼层管理 (Admin)", description = "Admin 对楼栋下的楼层进行创建、修改和删除")
@RestController
@RequestMapping("/api/dorm/floor")
public class DormFloorController {
    
    @Autowired private IDormFloorService floorService;
    
    @Operation(summary = "楼层分页查询", description = "获取楼层列表，可按楼栋 ID 筛选")
    // @SaCheckPermission("dorm:floor:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<Page<DormFloor>> list(@Valid DormFloorQueryDTO queryDTO) {
        Page<DormFloor> result = floorService.selectFloorPage(queryDTO);
        return R.ok(result);
    }
    
    @Operation(summary = "根据楼栋 ID 查询楼层", description = "获取指定楼栋下的所有楼层（不分页）")
    // @SaCheckPermission("dorm:floor:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list-by-building/{buildingId}")
    public R<List<DormFloor>> listByBuildingId(@PathVariable Long buildingId) {
        List<DormFloor> result = floorService.getFloorsByBuildingId(buildingId);
        return R.ok(result);
    }
    
    @Operation(summary = "新增楼层", description = "在指定楼栋下创建楼层")
    @SaCheckPermission("dorm:floor:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody DormFloor floor) {
        floorService.addFloor(floor);
        return R.ok("楼层新增成功");
    }
    
    @Operation(summary = "修改楼层", description = "更新楼层信息")
    @SaCheckPermission("dorm:floor:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody DormFloor floor) {
        floorService.updateById(floor);
        return R.ok("楼层修改成功");
    }
    
    @Operation(summary = "删除楼层", description = "删除楼层信息（含业务校验）")
    @SaCheckPermission("dorm:floor:remove")
    @DeleteMapping("/{floorId}")
    public R<Void> delete(@PathVariable Long floorId) {
        floorService.deleteFloor(floorId);
        return R.ok("楼层删除成功");
    }
}