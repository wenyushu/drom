package com.dormitory.service.impl;

// 导入 Service 接口
import com.dormitory.service.IStaffAllocationService;

// 导入 MyBatis Plus 核心功能
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 导入 LambdaQueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

// 导入所有相关的实体类
import com.dormitory.entity.*;

// 导入业务异常类
import com.dormitory.exception.BusinessException;

// 导入所有相关的 Mapper 接口
import com.dormitory.mapper.*;

// 导入日志
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 导入 Spring 核心注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 导入 Java Util 包
import java.time.LocalDateTime;
import java.util.*; // <-- 导入 java.util.*
import java.util.function.Function; // 导入 Function
import java.util.stream.Collectors;

// 导入 Hutool 工具类
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

// 导入 Sa-Token 工具 (用于获取当前登录用户)
import cn.dev33.satoken.stp.StpUtil;

/**
 * 教职工/后勤宿舍分配业务服务实现类 (V5.3: 修复V2.0匹配逻辑和离职Bug)
 */
@Service // 标记为 Spring Bean
public class StaffAllocationServiceImpl extends ServiceImpl<BizStaffAllocationLogMapper, BizStaffAllocationLog> implements IStaffAllocationService {
    
    private static final Logger log = LoggerFactory.getLogger(StaffAllocationServiceImpl.class);
    
    // --- 注入所有需要的 Mapper ---
    @Autowired private BizStaffInfoMapper staffInfoMapper;
    @Autowired private SysUserMapper userMapper;
    @Autowired private DormAllocationMapper allocationMapper; // 复杂查询
    @Autowired private DormBedMapper bedMapper;
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private DormBuildingMapper buildingMapper;
    @Autowired private DormFloorMapper floorMapper;
    @Autowired private SysCampusMapper campusMapper;
    @Autowired private DormFloorGenderRuleMapper floorRuleMapper;
    
    // (baseMapper 即 bizStaffAllocationLogMapper, 由 ServiceImpl 自动注入)
    
    
    /**
     * 批量为教职工分配宿舍 (自动)
     * 【修复】V2.0 匹配逻辑
     */
    @Override
    @Transactional // 整个过程是一个大事务
    public Map<Long, String> allocateRoomsForStaff(List<Long> staffUserIds) {
        Map<Long, String> results = new HashMap<>();
        if (CollUtil.isEmpty(staffUserIds)) {
            return results;
        }
        
        // 1. 准备数据：筛选需要分配的教职工
        List<BizStaffInfo> staffList = staffInfoMapper.selectBatchIds(staffUserIds);
        staffList = staffList.stream()
                // 筛选：必须未分配床位、且愿意住校
                .filter(s -> s != null && s.getCurrentBedId() == null && s.getIsOnCampusResident() != null && s.getIsOnCampusResident() == 1)
                .collect(Collectors.toList());
        
        if (staffList.isEmpty()) {
            log.warn("没有需要分配的教职工。");
            return results;
        }
        
        // 2. 按校区和性别分组，并执行分配
        for (BizStaffInfo staff : staffList) {
            try {
                // 3. 获取教职工信息（校区、性别）
                SysUser user = userMapper.selectById(staff.getUserId());
                if (user == null) {
                    results.put(staff.getUserId(), "分配失败：关联用户不存在");
                    continue;
                }
                
                // 修复：处理 TODO 1 - 校区信息
                // 业务缺陷：BizStaffInfo 或 SysDepartment 均未关联 campusId
                log.warn("【业务待办】教职工 (ID: {}) 校区信息未实现, 暂硬编码为 '1L' (主校区)", staff.getUserId());
                Long campusId = 1L;
                
                String staffGender = user.getSex();
                
                // 4. 查找可用的教职工床位 (purpose_type = '1')
                List<DormBed> availableBeds = allocationMapper.findAvailableStaffBeds(campusId, 100); // 查100个
                
                if (availableBeds.isEmpty()) {
                    results.put(staff.getUserId(), "分配失败：无符合条件的空闲教职工床位");
                    continue;
                }
                
                // 修复：实现 TODO 2 - V2.0 匹配逻辑
                // V2.0 匹配逻辑 (根据职称 title_name 匹配房间容量 room_capacity)
                DormBed targetBed = findBestBedForStaff(staff, availableBeds);
                if (targetBed == null) {
                    // 如果 V2 匹配失败 (例如教授想住单人间，但只有4人间)，则降级为 V1 逻辑
                    log.warn("【分配降级】教职工 (ID: {}) 未找到最佳匹配床位 (可能因职称要求)，尝试分配任意床位...", staff.getUserId());
                    targetBed = availableBeds.get(0); // V1.0 简化：选择第一个可用的
                }
                
                // 6. 执行分配 (调用手动分配，更新DB状态)
                assignStaffToBed(staff.getUserId(), targetBed.getBedId());
                
                // 7. V5 修正：联查楼层和楼栋
                DormRoom room = roomMapper.selectById(targetBed.getRoomId());
                DormFloor floor = floorMapper.selectById(room.getFloorId());
                DormBuilding building = buildingMapper.selectById(floor.getBuildingId());
                SysCampus campus = campusMapper.selectById(building.getCampusId());
                
                results.put(staff.getUserId(), String.format("成功分配到 %s-%s-%s层-%s-%s",
                        campus.getCampusName(), building.getBuildingName(), floor.getFloorNumber(), room.getRoomNumber(), targetBed.getBedNumber()));
                
            } catch (BusinessException e) {
                results.put(staff.getUserId(), "分配失败：" + e.getMessage());
            } catch (Exception e) {
                log.error("分配教职工 {} 失败", staff.getUserId(), e);
                results.put(staff.getUserId(), "分配失败：系统内部错误");
                // 抛出运行时异常以回滚事务
                throw new RuntimeException("教职工分配失败，事务回滚", e);
            }
        }
        return results;
    }
    
    /**
     * 【新增】辅助方法：V2.0 匹配逻辑
     * 根据职称 title_name 匹配房间容量 room_capacity
     */
    private DormBed findBestBedForStaff(BizStaffInfo staff, List<DormBed> availableBeds) {
        String titleName = staff.getTitleName();
        Integer targetCapacity = null;
        
        // 1. 定义职称和房间容量的业务规则
        // 修复：导入 StrUtil 后，此处的 StrUtil.isNotEmpty 可以被解析
        if (StrUtil.isNotEmpty(titleName)) {
            if (titleName.contains("教授") || titleName.contains("处长") || titleName.contains("院长")) {
                targetCapacity = 1; // 教授/处长/院长 优先分配单人间
            } else if (titleName.contains("副教授") || titleName.contains("讲师")) {
                targetCapacity = 2; // 副教授/讲师 优先分配双人间
            }
        }
        
        // 2. 如果没有匹配到特定职称，或者职称为空，则默认分配
        if (targetCapacity == null) {
            // 默认分配 4 人间或任意（优先已入住人数少的）
            return availableBeds.stream()
                    // 修复：DormBed.java 补充字段后，此处的 getRoomCapacity 可以被解析
                    .filter(bed -> bed.getRoomCapacity() != null && bed.getRoomCapacity() >= 4)
                    .findFirst()
                    .orElse(availableBeds.get(0)); // 降级
        }
        
        // 3. 筛选出所有符合容量的床位
        final int finalTargetCapacity = targetCapacity;
        List<DormBed> matchedBeds = availableBeds.stream()
                // 修复：DormBed.java 补充字段后，此处的 getRoomCapacity 可以被解析
                .filter(bed -> bed.getRoomCapacity() != null && bed.getRoomCapacity() == finalTargetCapacity)
                .collect(Collectors.toList());
        
        // 4. 优先选择符合容量的床位中的第一个
        if (CollUtil.isNotEmpty(matchedBeds)) {
            return matchedBeds.get(0);
        }
        
        // 5. 如果没有完全匹配的（例如 教授 想住 1 人间，但只有 2 人间），则返回 null，由主流程降级处理
        return null;
    }
    
    /**
     * 手动将教职工分配到指定床位 (Admin 操作)
     * (保持不变)
     */
    @Override
    @Transactional
    public void assignStaffToBed(Long staffUserId, Long targetBedId) {
        // 1. 校验教职工信息
        BizStaffInfo staffInfo = staffInfoMapper.selectById(staffUserId);
        if (staffInfo == null) {
            throw new BusinessException("分配失败：教职工信息不存在");
        }
        SysUser staffUser = userMapper.selectById(staffUserId);
        if (staffUser == null) {
            throw new BusinessException("分配失败：关联的系统用户不存在");
        }
        
        String actionType = "0"; // 默认 0=入住/分配
        String reason = "管理员手动分配";
        
        // 2. V5.1 如果教职工已有床位，则先执行迁出逻辑
        if (staffInfo.getCurrentBedId() != null) {
            if (staffInfo.getCurrentBedId().equals(targetBedId)) {
                throw new BusinessException("分配失败：已在该床位");
            }
            log.info("教职工 {} (UserID: {}) 原有床位 {}，执行自动迁出以分配新床位 {}", staffInfo.getJobTitle(), staffUserId, staffInfo.getCurrentBedId());
            // 自动迁出旧床位
            checkoutStaffFromBed(staffUserId, "管理员调宿");
            
            actionType = "1"; // 标记为 1=调宿
            reason = "管理员手动调宿";
        }
        
        // 3. 校验目标床位
        DormBed bed = bedMapper.selectById(targetBedId);
        if (bed == null) throw new BusinessException("分配失败：目标床位不存在");
        if (bed.getIsOccupied() != null && bed.getIsOccupied() == 1) throw new BusinessException("分配失败：目标床位已被占用");
        
        // 4. 核心修正 1：校验房间用途和规则
        DormRoom room = roomMapper.selectById(bed.getRoomId());
        if (room == null) throw new BusinessException("分配失败：床位关联的房间不存在");
        
        DormFloor floor = floorMapper.selectById(room.getFloorId());
        if (floor == null) throw new BusinessException("分配失败：房间关联的楼层不存在");
        
        DormBuilding building = buildingMapper.selectById(floor.getBuildingId());
        if (building == null) throw new BusinessException("分配失败：楼层关联的楼栋不存在");
        
        // 4.1 校验用途
        if (!"1".equals(building.getPurposeType())) {
            throw new BusinessException("分配失败：目标楼栋非教职工宿舍");
        }
        if ("00".equals(room.getRoomPurposeType())) {
            throw new BusinessException("分配失败：目标房间是学生宿舍，不能分配给教职工");
        }
        
        // 4.2 校验性别 (如果楼栋是混合型)
        if ("2".equals(building.getGenderType())) {
            DormFloorGenderRule rule = floorRuleMapper.selectOne(new LambdaQueryWrapper<DormFloorGenderRule>()
                    .eq(DormFloorGenderRule::getFloorId, floor.getFloorId()));
            
            // 修正点 3：使用 Objects.equals
            if (rule != null && !Objects.equals(rule.getGenderType(), staffUser.getSex())) {
                throw new BusinessException("分配失败：教职工性别与目标楼层性别限制不符");
            }
        }
        
        // 5. 更新床位
        bed.setOccupantUserId(staffUserId);
        bed.setOccupantType("2"); // 2 = 教职工
        bed.setIsOccupied(1);
        bedMapper.updateById(bed);
        
        // 6. 更新教职工信息
        staffInfo.setCurrentBedId(targetBedId);
        staffInfoMapper.updateById(staffInfo);
        
        // 7. 更新房间人数
        int updatedRows = roomMapper.increaseOccupancy(bed.getRoomId());
        if (updatedRows == 0) {
            throw new BusinessException("分配失败：更新房间入住人数失败，可能房间已满");
        }
        
        // 8. 修正 2：记录日志
        BizStaffAllocationLog allocationLog = new BizStaffAllocationLog();
        allocationLog.setUserId(staffUserId);
        allocationLog.setBedId(targetBedId);
        allocationLog.setActionType(actionType); // '0' or '1'
        allocationLog.setReason(reason);
        allocationLog.setStartTime(LocalDateTime.now());
        allocationLog.setOperatorId(StpUtil.getLoginIdAsLong()); // 记录操作的 Admin ID
        allocationLog.setCreateTime(LocalDateTime.now()); // 手动设置 createTime
        
        this.save(allocationLog); // 使用 ServiceImpl 的 save 方法 (因为继承了)
        
        log.info("教职工 {} (UserID: {}) 成功分配到床位 {}", staffInfo.getJobTitle(), staffUserId, targetBedId);
    }
    
    /**
     * 将教职工从当前床位迁出 (离职/搬离)
     * V5.3 修复：增加账户状态联动
     */
    @Override
    @Transactional
    public void checkoutStaffFromBed(Long staffUserId, String reason) {
        BizStaffInfo staffInfo = staffInfoMapper.selectById(staffUserId);
        if (staffInfo == null) {
            log.warn("教职工 {} 档案不存在，无法迁出", staffUserId);
            return;
        }
        
        Long currentBedId = staffInfo.getCurrentBedId();
        
        // 1. 清空床位
        if (currentBedId != null) {
            DormBed bed = bedMapper.selectById(currentBedId);
            if (bed != null) {
                bed.setOccupantUserId(null);
                bed.setOccupantType(null);
                bed.setIsOccupied(0);
                bedMapper.updateById(bed);
                // 2. 更新房间人数
                roomMapper.decreaseOccupancy(bed.getRoomId());
            }
        }
        
        // 3. 清空教职工床位
        staffInfo.setCurrentBedId(null);
        staffInfoMapper.updateById(staffInfo); // 更新 staffInfo（仅清空床位ID）
        
        // 4. 修复：Bug 1 - 联动更新 SysUser 状态
        // 如果原因是离职，则禁用账户并更新任职状态
        if ("离职".equals(reason)) {
            log.info("教职工 {} (UserID: {}) 离职，正在禁用其 SysUser 账户...", staffInfo.getJobTitle(), staffUserId);
            SysUser userToDisable = new SysUser();
            userToDisable.setUserId(staffUserId);
            userToDisable.setStatus(1); // 1 = 禁用
            userToDisable.setEmploymentStatus("1"); // 1 = 离职
            userMapper.updateById(userToDisable);
        }
        
        // 5. 记录日志 (保持不变)
        BizStaffAllocationLog checkoutLog = new BizStaffAllocationLog();
        checkoutLog.setUserId(staffUserId);
        checkoutLog.setBedId(currentBedId); // 记录迁出的床位
        checkoutLog.setActionType("2"); // 2 = 迁出/离职
        checkoutLog.setReason(reason);
        checkoutLog.setStartTime(LocalDateTime.now());
        checkoutLog.setOperatorId(StpUtil.getLoginIdAsLong()); // 记录操作的 Admin ID
        checkoutLog.setCreateTime(LocalDateTime.now()); // 手动设置 createTime
        
        this.save(checkoutLog); // 使用 ServiceImpl 的 save 方法
        
        log.info("教职工 {} (UserID: {}) 成功迁出，原因: {}", staffInfo.getJobTitle(), staffUserId, reason);
    }
}