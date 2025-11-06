package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.DormBuildingQueryDTO;
import com.dormitory.entity.DormBuilding;
import com.dormitory.service.IDormBuildingService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 楼栋资产管理控制器 (Admin 权限)
 */
@Tag(name = "楼栋资产管理", description = "Admin对楼栋进行创建、修改和删除")
@RestController
@RequestMapping("/api/dorm/building")
public class DormBuildingController {
    
    @Autowired
    private IDormBuildingService buildingService;
    
    /**
     * 分页查询楼栋列表
     */
    @Operation(summary = "楼栋分页查询", description = "获取楼栋列表，包含宿管和校区信息")
    // @SaCheckPermission("dorm:building:query")
    // 所有人均可查询
    @GetMapping("/list")
    public R<Page<DormBuilding>> list(@Valid DormBuildingQueryDTO queryDTO) {
        Page<DormBuilding> page = buildingService.selectBuildingPage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 新增楼栋 (Admin专属)
     */
    @Operation(summary = "新增楼栋", description = "创建新的楼栋资产")
    @SaCheckPermission("dorm:building:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody DormBuilding building) {
        buildingService.addBuilding(building);
        return R.ok("楼栋新增成功");
    }
    
    /**
     * 修改楼栋 (Admin专属)
     */
    @Operation(summary = "修改楼栋", description = "修改楼栋的基础信息、宿管和状态")
    @SaCheckPermission("dorm:building:edit")
    @PutMapping
    public R<Void> edit(@Valid @RequestBody DormBuilding building) {
        buildingService.updateBuilding(building);
        return R.ok("楼栋信息修改成功");
    }
    
    /**
     * 批量删除楼栋 (Admin专属)
     */
    @Operation(summary = "批量删除楼栋", description = "删除楼栋资产，有房间关联时会失败")
    @SaCheckPermission("dorm:building:remove")
    @DeleteMapping("/{buildingIds}")
    public R<Void> remove(@PathVariable Long[] buildingIds) {
        buildingService.deleteBuildingByIds(buildingIds);
        return R.ok("楼栋删除成功");
    }
}