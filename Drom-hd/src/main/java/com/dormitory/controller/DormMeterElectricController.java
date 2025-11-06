package com.dormitory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.entity.DormMeterElectric;
import com.dormitory.service.IDormMeterElectricService;
import com.dormitory.utils.R;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dormitory.dto.DormMeterElectricQueryDTO;
import jakarta.validation.Valid;

/**
 * 房间电表资产控制器
 */
@Tag(name = "房间电表管理", description = "Admin 和水电管理员对电表资产进行管理")
@RestController
@RequestMapping("/api/dorm/meter/electric")
public class DormMeterElectricController {
    
    @Autowired
    private IDormMeterElectricService electricMeterService;
    
    /**
     * 分页查询电表列表
     */
    @Operation(summary = "电表分页查询", description = "获取电表资产列表，包含房间号")
    @GetMapping("/list")
    // 所有人（已登录用户）均可查询，但全局登录拦截器 SaTokenConfigure 仍会生效
    public R<Page<DormMeterElectric>> list(@Valid DormMeterElectricQueryDTO queryDTO) {
        Page<DormMeterElectric> result = electricMeterService.selectMeterPage(queryDTO);
        return R.ok(result);
    }
    
    /**
     * 新增电表 (核心测试点：一房一表)
     */
    @Operation(summary = "新增电表", description = "为房间配置电表，确保一房一表")
    @SaCheckPermission("dorm:meter:electric:add")
    @PostMapping
    public R<Void> add(@RequestBody DormMeterElectric meter) {
        electricMeterService.addMeter(meter);
        return R.ok("电表新增成功");
    }
    
    /**
     * 修改电表信息
     */
    @Operation(summary = "修改电表信息", description = "修改电表型号、状态等信息")
    @SaCheckPermission("dorm:meter:electric:edit")
    @PutMapping
    public R<Void> update(@RequestBody DormMeterElectric meter) {
        electricMeterService.updateById(meter);
        return R.ok("电表信息修改成功");
    }
    
    /**
     * 批量删除电表
     */
    @Operation(summary = "批量删除电表", description = "删除电表资产，需级联清除读数记录")
    @SaCheckPermission("dorm:meter:electric:remove")
    @DeleteMapping("/{meterIds}")
    public R<Void> delete(@PathVariable Long[] meterIds) {
        electricMeterService.deleteMeterByIds(meterIds);
        return R.ok("电表批量删除成功");
    }
}