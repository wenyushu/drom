package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.RoomAssetQueryDTO;
import com.dormitory.entity.DormRoomAsset;
import com.dormitory.service.IDormRoomAssetService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 房间固定资产管理控制器
 */
@Tag(name = "房间固定资产管理", description = "Admin对房间内的固定资产进行管理")
@RestController
@RequestMapping("/api/dorm/asset")
public class DormRoomAssetController {
    
    @Autowired private IDormRoomAssetService assetService;
    
    /**
     * 分页查询资产列表
     */
    @Operation(summary = "资产分页查询", description = "查询房间固定资产列表")
    // @SaCheckPermission("dorm:asset:query")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    @GetMapping("/list")
    public R<Page<DormRoomAsset>> list(@Valid RoomAssetQueryDTO queryDTO) {
        Page<DormRoomAsset> page = assetService.selectAssetPage(queryDTO);
        return R.ok(page);
    }
    
    /**
     * 新增资产
     */
    @Operation(summary = "新增资产", description = "为房间添加固定资产记录")
    @SaCheckPermission("dorm:asset:add")
    @PostMapping
    public R<Void> add(@Valid @RequestBody DormRoomAsset asset) {
        assetService.addAsset(asset);
        return R.ok("资产新增成功");
    }
    
    /**
     * 修改资产信息
     */
    @Operation(summary = "修改资产信息", description = "修改资产状态、序列号等")
    @SaCheckPermission("dorm:asset:edit")
    @PutMapping
    public R<Void> update(@Valid @RequestBody DormRoomAsset asset) {
        assetService.updateById(asset);
        return R.ok("资产信息修改成功");
    }
    
    /**
     * 批量删除资产
     */
    @Operation(summary = "批量删除资产", description = "删除资产记录，有未完成报修单时失败")
    @SaCheckPermission("dorm:asset:remove")
    @DeleteMapping("/{assetIds}")
    public R<Void> delete(@PathVariable Long[] assetIds) {
        assetService.deleteAssetByIds(assetIds);
        return R.ok("资产批量删除成功");
    }
}