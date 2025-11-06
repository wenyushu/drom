package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.RepairOrderQueryDTO;
import com.dormitory.dto.RepairOrderUpdateStatusDTO;
import com.dormitory.entity.*; // 【修改】导入所有实体
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.*; // 【修改】导入所有 Mapper
import com.dormitory.service.IBizRepairOrderService;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 报修工单业务服务实现类
 * 已修复VO填充
 */
@Service
public class BizRepairOrderServiceImpl extends ServiceImpl<BizRepairOrderMapper, BizRepairOrder> implements IBizRepairOrderService {
    
    // 注入所有需要的 Mapper
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private SysUserMapper userMapper;
    @Autowired private DormRoomAssetMapper assetMapper;
    @Autowired private StuStudentMapper studentMapper;
    @Autowired private BizClassMapper classMapper;
    
    @Autowired private DormFloorMapper floorMapper;
    @Autowired private DormBuildingMapper buildingMapper;
    @Autowired private SysCampusMapper campusMapper;
    
    private static final Logger log = LoggerFactory.getLogger(BizRepairOrderServiceImpl.class);
    
    /**
     * 分页查询工单列表
     * 已修复 VO 填充
     */
    @Override
    public Page<BizRepairOrder> selectOrderPage(RepairOrderQueryDTO queryDTO) {
        
        // 1. 构建基础查询条件
        LambdaQueryWrapper<BizRepairOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getRoomId() != null, BizRepairOrder::getRoomId, queryDTO.getRoomId())
                .eq(StrUtil.isNotEmpty(queryDTO.getOrderStatus()), BizRepairOrder::getOrderStatus, queryDTO.getOrderStatus());
        
        // =========================================================
        // 【数据越权安全过滤】(保持不变)
        // =========================================================
        Long loginId = StpUtil.getLoginIdAsLong();
        
        if (StpUtil.hasRole("student")) {
            log.debug("[数据权限] 检测到学生 (ID: {}) 查询报修单，强制过滤本人数据。", loginId);
            wrapper.eq(BizRepairOrder::getApplicantUserId, loginId);
            
        } else if (StpUtil.hasRole("counselor")) {
            log.debug("[数据权限] 检测到辅导员 (ID: {}) 查询报修单，开始过滤所管班级学生数据。", loginId);
            List<Long> classIds = classMapper.selectList(new LambdaQueryWrapper<BizClass>()
                            .eq(BizClass::getCounselorUserId, loginId))
                    .stream().map(BizClass::getClassId).collect(Collectors.toList());
            
            if (CollUtil.isEmpty(classIds)) {
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            
            List<Long> studentUserIds = studentMapper.selectList(new LambdaQueryWrapper<StuStudent>()
                            .in(StuStudent::getClassId, classIds))
                    .stream().map(StuStudent::getUserId).distinct().collect(Collectors.toList());
            
            if (CollUtil.isEmpty(studentUserIds)) {
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            wrapper.in(BizRepairOrder::getApplicantUserId, studentUserIds);
            
        } else {
            log.debug("[数据权限] 检测到管理员 (ID: {})，允许查询所有数据。", loginId);
            wrapper.eq(queryDTO.getApplicantUserId() != null, BizRepairOrder::getApplicantUserId, queryDTO.getApplicantUserId());
        }
        // =========================================================
        
        wrapper.orderByDesc(BizRepairOrder::getSubmitTime);
        
        // 2. 执行分页查询
        Page<BizRepairOrder> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        
        // 3. 填充关联信息 (房间号、申请人、资产名)
        if (page.getRecords().isEmpty()) {
            return page;
        }
        
        List<BizRepairOrder> records = page.getRecords();
        
        // 提取所有关联 ID
        List<Long> roomIds = records.stream().map(BizRepairOrder::getRoomId).distinct().collect(Collectors.toList());
        List<Long> applicantIds = records.stream().map(BizRepairOrder::getApplicantUserId).distinct().collect(Collectors.toList());
        List<Long> assetIds = records.stream().map(BizRepairOrder::getAssetId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        
        // 已修复：开始多级联查
        
        // 批量查询 Map
        final Map<Long, DormRoom> roomMap = roomIds.isEmpty() ? Collections.emptyMap() :
                roomMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(DormRoom::getRoomId, r -> r));
        
        List<Long> floorIds = roomMap.values().stream().map(DormRoom::getFloorId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        final Map<Long, DormFloor> floorMap = floorIds.isEmpty() ? Collections.emptyMap() :
                floorMapper.selectBatchIds(floorIds).stream().collect(Collectors.toMap(DormFloor::getFloorId, f -> f));
        
        List<Long> buildingIds = floorMap.values().stream().map(DormFloor::getBuildingId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        final Map<Long, DormBuilding> buildingMap = buildingIds.isEmpty() ? Collections.emptyMap() :
                buildingMapper.selectBatchIds(buildingIds).stream().collect(Collectors.toMap(DormBuilding::getBuildingId, b -> b));
        
        List<Long> campusIds = buildingMap.values().stream().map(DormBuilding::getCampusId).distinct().collect(Collectors.toList());
        final Map<Long, String> campusMap = campusIds.isEmpty() ? Collections.emptyMap() :
                campusMapper.selectBatchIds(campusIds).stream().collect(Collectors.toMap(SysCampus::getCampusId, SysCampus::getCampusName));
        
        // 使用 RealName (真实姓名) 作为申请人姓名
        final Map<Long, String> userMap = applicantIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(applicantIds).stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getRealName));
        
        final Map<Long, String> assetMap = assetIds.isEmpty() ? Collections.emptyMap() :
                assetMapper.selectBatchIds(assetIds).stream().collect(Collectors.toMap(DormRoomAsset::getAssetId, DormRoomAsset::getAssetName));
        
        // 填充数据
        records.forEach(order -> {
            DormRoom room = roomMap.get(order.getRoomId());
            if (room != null) {
                // 【修复】填充完整的楼栋-房间号
                DormFloor floor = floorMap.get(room.getFloorId());
                if (floor != null) {
                    DormBuilding building = buildingMap.get(floor.getBuildingId());
                    if (building != null) {
                        String campusName = campusMap.get(building.getCampusId());
                        order.setRoomFullName(String.format("%s-%s-%s层-%s",
                                (campusName != null ? campusName : "N/A"),
                                building.getBuildingName(),
                                floor.getFloorNumber(),
                                room.getRoomNumber()));
                    } else {
                        order.setRoomFullName(room.getRoomNumber()); // 降级
                    }
                } else {
                    order.setRoomFullName(room.getRoomNumber()); // 降级
                }
            }
            order.setApplicantName(userMap.get(order.getApplicantUserId()));
            order.setAssetName(assetMap.get(order.getAssetId()));
        });
        
        return page;
    }
    
    
    /**
     * 提交报修工单 (学生/宿管)
     */
    @Override
    @Transactional
    public void submitOrder(BizRepairOrder order) {
        // 1. 校验房间是否存在
        if (order.getRoomId() == null || !roomMapper.exists(new LambdaQueryWrapper<DormRoom>().eq(DormRoom::getRoomId, order.getRoomId()))) {
            throw new BusinessException("提交失败：报修房间不存在。");
        }
        
        // 2. 校验资产是否存在
        if (order.getAssetId() != null && !assetMapper.exists(new LambdaQueryWrapper<DormRoomAsset>().eq(DormRoomAsset::getAssetId, order.getAssetId()))) {
            throw new BusinessException("提交失败：关联的资产不存在。");
        }
        
        // 3. 自动填充信息
        order.setSubmitTime(LocalDateTime.now());
        order.setOrderStatus("0"); // 0 = 待分配
        order.setApplicantUserId(StpUtil.getLoginIdAsLong()); // 自动获取当前登录用户作为申请人
        
        // 4. 执行保存 (插入工单)
        this.save(order);
        
        // 5. 【业务联动 1】
        // 提交报修后，自动将关联的资产状态设为 '1' (损坏/待修)
        if (order.getAssetId() != null) {
            DormRoomAsset asset = assetMapper.selectById(order.getAssetId());
            // 只有当资产状态是 '0' (正常) 时才更新，防止覆盖 '报废' 等状态
            if (asset != null && "0".equals(asset.getStatus())) {
                log.info("工单 {} 已提交，联动更新资产 {} 状态为 [损坏/待修]", order.getOrderId(), asset.getAssetId());
                asset.setStatus("1"); // 1 = 损坏/待修
                assetMapper.updateById(asset);
            }
        }
    }
    
    /**
     * 【核心修改】更新工单状态 (维修人员/管理员)
     */
    @Override
    @Transactional
    public void updateOrderStatus(RepairOrderUpdateStatusDTO updateDTO) {
        
        // 1. 基础校验
        BizRepairOrder order = this.getById(updateDTO.getOrderId());
        if (order == null) {
            throw new BusinessException("工单不存在，无法更新状态。");
        }
        // 防止重复处理
        if (List.of("2", "3").contains(order.getOrderStatus())) {
            throw new BusinessException("该工单已完结（已完成或无法修复），请勿重复操作");
        }
        
        // 2. 更新工单基础信息
        order.setOrderStatus(updateDTO.getNewStatus());
        order.setHandlerUserId(StpUtil.getLoginIdAsLong()); // 将当前操作者（维修工）设为处理人
        order.setRepairResult(updateDTO.getRepairResult());
        order.setFinishTime(LocalDateTime.now()); // 标记处理时间
        
        // 3. 【【V5 核心业务触发器】】
        // 根据工单处理结果，反向更新资产和房间的状态
        
        DormRoom room = roomMapper.selectById(order.getRoomId());
        DormRoomAsset asset = (order.getAssetId() != null) ? assetMapper.selectById(order.getAssetId()) : null;
        
        // 3.1 场景一：标准维修（已完成）
        if ("2".equals(updateDTO.getNewStatus())) {
            log.info("工单 {} 已完成，正在将关联资产 {} 状态恢复为 [正常]。", order.getOrderId(), order.getAssetId());
            
            // 联动：将资产状态恢复为 '0' (正常)
            if (asset != null) {
                asset.setStatus("0");
                assetMapper.updateById(asset);
            }
        }
        
        // 3.2 场景二：重大事故（无法修复，如火灾、主水管炸裂）
        if ("3".equals(updateDTO.getNewStatus())) {
            log.warn("工单 {} 标记为 [无法修复]，触发【房间封禁】和【资产报废】...", order.getOrderId());
            
            // 联动 1：将房间状态更新为 '1' (待维修/封禁)
            if (room != null) {
                room.setRoomStatus("1"); // 1 = 待维修/封禁
                roomMapper.updateById(room);
            }
            
            // 联动 2：将资产状态更新为 '2' (报废)
            if (asset != null) {
                asset.setStatus("2");
                assetMapper.updateById(asset);
            }
        }
        
        // 4. 保存工单的最终状态
        this.updateById(order);
    }
}