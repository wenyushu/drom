package com.dormitory.service.impl;

// ... (省略大量 imports) ...
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.service.IDormAllocationService;
import com.dormitory.dto.RoomChangeApprovalDTO;
import com.dormitory.dto.RoomChangeQueryDTO; // 【【【【【 1. 导入新 DTO 】】】】】
import com.dormitory.dto.RoomChangeRequestDTO;
import com.dormitory.vo.RoomChangeRequestVO;
import com.dormitory.entity.*;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;

/**
 * 宿舍分配与调宿核心业务服务实现类 (V5.3: 修正分页查询 Bug)
 */
@Service
public class DormAllocationServiceImpl implements IDormAllocationService {
    
    private static final Logger log = LoggerFactory.getLogger(DormAllocationServiceImpl.class);
    
    private static final String HAN_ETHNICITY = "汉族";
    private static final int MAX_MINORITY_PER_ROOM = 2;
    
    @Autowired private StuStudentMapper studentMapper;
    @Autowired private UserPreferenceMapper preferenceMapper;
    @Autowired private DormBedMapper bedMapper;
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private DormFloorMapper floorMapper;
    @Autowired private DormBuildingMapper buildingMapper;
    @Autowired private SysCampusMapper campusMapper;
    @Autowired private DormFloorGenderRuleMapper floorRuleMapper;
    @Autowired private DormAllocationLogMapper allocationLogMapper;
    @Autowired private SysUserMapper userMapper;
    @Autowired private DormAllocationMapper allocationMapper;
    @Autowired private BizRoomChangeRequestMapper requestMapper;
    
    
    // =================================================================
    // SECTION 1: 自动分配 (保持不变)
    // =================================================================
    
    @Override
    @Transactional
    public Map<Long, String> allocateRoomsForStudents(List<Long> studentIds) {
        // ... (省略 allocateRoomsForStudents 完整逻辑, 保持不变) ...
        log.info("【自动分配 V3】开始执行，待分配学生ID数量: {}", studentIds.size());
        Map<Long, String> results = new HashMap<>();
        if (CollUtil.isEmpty(studentIds)) {
            return results;
        }
        
        List<StuStudent> students = studentMapper.selectBatchIds(studentIds);
        students = students.stream()
                .filter(s -> s != null && s.getCurrentBedId() == null && s.getIsOnCampusResident() != null && s.getIsOnCampusResident() == 1 && "0".equals(s.getAcademicStatus()))
                .collect(Collectors.toList());
        
        if (students.isEmpty()) {
            log.warn("【自动分配 V3】没有需要分配的学生 (已过滤)。");
            return results;
        }
        
        List<Long> userIds = students.stream().map(StuStudent::getUserId).collect(Collectors.toList());
        
        final Map<Long, SysUser> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(SysUser::getUserId, u -> u));
        
        final Map<Long, UserPreference> prefMap = userIds.isEmpty() ? Collections.emptyMap() :
                preferenceMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(UserPreference::getUserId, p -> p));
        
        
        Map<String, List<StuStudent>> studentGroups = students.stream()
                .collect(Collectors.groupingBy(student -> {
                    SysUser user = userMap.get(student.getUserId());
                    String gender = (user != null) ? user.getSex() : "unknown";
                    Long classId = student.getClassId() != null ? student.getClassId() : 0L;
                    return classId + "_" + gender;
                }));
        
        List<StuStudent> globalLeftovers = new ArrayList<>();
        
        for (Map.Entry<String, List<StuStudent>> entry : studentGroups.entrySet()) {
            String[] groupKey = entry.getKey().split("_");
            Long classId = Long.parseLong(groupKey[0]);
            String gender = groupKey[1];
            List<StuStudent> groupStudents = entry.getValue();
            
            if ("unknown".equals(gender)) {
                groupStudents.forEach(s -> results.put(s.getStudentId(), "分配失败：无法获取性别信息"));
                continue;
            }
            
            Long campusId = groupStudents.get(0).getCurrentCampusId();
            
            List<DormBed> allAvailableBeds = allocationMapper.findAvailableBeds(campusId, gender, null);
            log.info("【自动分配 V3】为 班级={} 性别={} 找到 {} 个可用床位", classId, gender, allAvailableBeds.size());
            
            List<StuStudent> groupLeftovers = allocateGroupV3(groupStudents, allAvailableBeds, prefMap, results, userMap);
            globalLeftovers.addAll(groupLeftovers);
        }
        
        if (!globalLeftovers.isEmpty()) {
            log.warn("【自动分配 V3】开始处理 {} 名剩余人员 (跨班级分配)...", globalLeftovers.size());
            
            Map<String, List<StuStudent>> leftoverGroups = globalLeftovers.stream()
                    .collect(Collectors.groupingBy(student -> {
                        SysUser user = userMap.get(student.getUserId());
                        String gender = (user != null) ? user.getSex() : "unknown";
                        Long campusId = student.getCurrentCampusId();
                        return campusId + "_" + gender;
                    }));
            
            for (Map.Entry<String, List<StuStudent>> entry : leftoverGroups.entrySet()) {
                String[] groupKey = entry.getKey().split("_");
                Long campusId = Long.parseLong(groupKey[0]);
                String gender = groupKey[1];
                if ("unknown".equals(gender)) continue;
                List<DormBed> availableBeds = allocationMapper.findAvailableBeds(campusId, gender, null);
                allocateGroupV3(entry.getValue(), availableBeds, prefMap, results, userMap);
            }
        }
        
        return results;
    }
    
    private List<StuStudent> allocateGroupV3(List<StuStudent> students, List<DormBed> beds, final Map<Long, UserPreference> prefMap, Map<Long, String> results, final Map<Long, SysUser> userMap) {

        List<StuStudent> leftovers = new ArrayList<>();
        
        final Map<Long, List<DormBed>> availableBedsByRoom = beds.stream()
                .filter(b -> b.getIsOccupied() == null || b.getIsOccupied() == 0)
                .collect(Collectors.groupingBy(DormBed::getRoomId));
        
        for (StuStudent student : students) {
            if (results.containsKey(student.getStudentId())) continue;
            
            SysUser studentUser = userMap.get(student.getUserId());
            if (studentUser == null) {
                results.put(student.getStudentId(), "分配失败：无法获取用户信息");
                continue;
            }
            
            final UserPreference studentPref = prefMap.getOrDefault(student.getUserId(), new UserPreference());
            final boolean studentIsMinority = studentUser.getEthnicity() != null && !HAN_ETHNICITY.equals(studentUser.getEthnicity());
            
            Long bestBedId = null;
            int maxScore = Integer.MIN_VALUE;
            
            for (Map.Entry<Long, List<DormBed>> entry : availableBedsByRoom.entrySet()) {
                Long roomId = entry.getKey();
                List<DormBed> emptyBedsInRoom = entry.getValue();
                
                if (emptyBedsInRoom.isEmpty()) continue;
                
                List<DormBed> currentOccupants = bedMapper.selectList(new LambdaQueryWrapper<DormBed>()
                        .eq(DormBed::getRoomId, roomId)
                        .eq(DormBed::getIsOccupied, 1)
                        .isNotNull(DormBed::getOccupantUserId)
                );
                
                List<Long> occupantUserIds = currentOccupants.stream().map(DormBed::getOccupantUserId).collect(Collectors.toList());
                Map<Long, SysUser> occupantUserMap = occupantUserIds.isEmpty() ? Collections.emptyMap() :
                        userMapper.selectBatchIds(occupantUserIds).stream().collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
                
                long minorityCountInRoom = occupantUserMap.values().stream()
                        .filter(u -> u.getEthnicity() != null && !HAN_ETHNICITY.equals(u.getEthnicity()))
                        .count();
                
                if (studentIsMinority && minorityCountInRoom >= MAX_MINORITY_PER_ROOM) {
                    continue;
                }
                
                int totalScore = 0;
                if (CollUtil.isEmpty(currentOccupants)) {
                    totalScore = 100;
                } else {
                    for (DormBed occupantBed : currentOccupants) {
                        Long occupantUserId = occupantBed.getOccupantUserId();
                        UserPreference occupantPref = preferenceMapper.selectById(occupantUserId);
                        occupantPref = occupantPref != null ? occupantPref : new UserPreference();
                        totalScore += scorePreferenceMatch(studentPref, occupantPref);
                    }
                    totalScore /= currentOccupants.size();
                }
                
                if (totalScore > maxScore) {
                    maxScore = totalScore;
                    bestBedId = emptyBedsInRoom.get(0).getBedId();
                }
            }
            
            if (bestBedId != null) {
                try {
                    final Long allocatedBedId = bestBedId;
                    
                    DormAllocationLog logEntry = assignStudentToBed(student.getStudentId(), allocatedBedId);
                    
                    DormBed finalBed = bedMapper.selectById(allocatedBedId);
                    final Long finalRoomId = finalBed.getRoomId();
                    availableBedsByRoom.get(finalRoomId).removeIf(b -> b.getBedId().equals(allocatedBedId));
                    DormRoom room = roomMapper.selectById(finalRoomId);
                    DormFloor floor = floorMapper.selectById(room.getFloorId());
                    DormBuilding building = buildingMapper.selectById(floor.getBuildingId());
                    SysCampus campus = campusMapper.selectById(building.getCampusId());
                    
                    results.put(student.getStudentId(), String.format("%s-%s-%s层-%s-%s (匹配度: %d)",
                            campus.getCampusName(), building.getBuildingName(), floor.getFloorNumber(), room.getRoomNumber(), finalBed.getBedNumber(), maxScore));
                    
                } catch (BusinessException e) {
                    results.put(student.getStudentId(), "分配失败(V3): " + e.getMessage());
                }
            } else {
                results.put(student.getStudentId(), "分配失败：无符合偏好/民族约束的空闲床位");
                leftovers.add(student);
            }
        }
        
        return leftovers;
    }
    
    private int scorePreferenceMatch(UserPreference pref1, UserPreference pref2) {
        // ... (省略 scorePreferenceMatch 完整逻辑, 保持不变) ...
        int score = 100;
        if (pref1 == null || pref2 == null) return 0;
        
        boolean sleepConflict = (Objects.equals("0", pref1.getSleepType()) && Objects.equals("2", pref2.getSleepType())) ||
                (Objects.equals("2", pref1.getSleepType()) && Objects.equals("0", pref2.getSleepType()));
        boolean wakeConflict = (Objects.equals("0", pref1.getWakeType()) && Objects.equals("2", pref2.getWakeType())) ||
                (Objects.equals("2", pref1.getWakeType()) && Objects.equals("0", pref2.getWakeType()));
        if (sleepConflict || wakeConflict) {
            score -= 30;
        } else if (!Objects.equals(pref1.getSleepType(), pref2.getSleepType()) || !Objects.equals(pref1.getWakeType(), pref2.getWakeType())) {
            score -= 10;
        }
        
        boolean smokerConflict = (pref1.getIsSmoker() != null && pref1.getIsSmoker() == 1 && pref2.getIsSmoker() != null && pref2.getIsSmoker() == 0) ||
                (pref1.getIsSmoker() != null && pref1.getIsSmoker() == 0 && pref2.getIsSmoker() != null && pref2.getIsSmoker() == 1);
        if (smokerConflict) {
            score -= 20;
        }
        
        int cleanDiff = 0;
        try {
            if (pref1.getCleanlinessLevel() != null && pref2.getCleanlinessLevel() != null) {
                cleanDiff = Math.abs(Integer.parseInt(pref1.getCleanlinessLevel()) - Integer.parseInt(pref2.getCleanlinessLevel()));
            }
        } catch (Exception ignored) { }
        
        if (cleanDiff >= 2) {
            score -= 20;
        } else if (cleanDiff == 1) {
            score -= 5;
        }
        
        boolean p1_isSensitive = pref1.getIsLightSleeper() != null && pref1.getIsLightSleeper() == 1;
        boolean p2_isSensitive = pref2.getIsLightSleeper() != null && pref2.getIsLightSleeper() == 1;
        boolean p1_isNoisy = (Objects.equals("2", pref1.getMobileGameFreq()) || Objects.equals("2", pref1.getInRoomNoiseLevel()));
        boolean p2_isNoisy = (Objects.equals("2", pref2.getMobileGameFreq()) || Objects.equals("2", pref2.getInRoomNoiseLevel()));
        
        if ((p1_isSensitive && p2_isNoisy) || (p2_isSensitive && p1_isNoisy)) {
            score -= 15;
        }
        
        boolean p1_smellSensitive = "2".equals(pref1.getSmellSensitivity());
        boolean p2_smellSensitive = "2".equals(pref2.getSmellSensitivity());
        boolean p1_isSmoker = pref1.getIsSmoker() != null && pref1.getIsSmoker() == 1;
        boolean p2_isSmoker = pref2.getIsSmoker() != null && pref2.getIsSmoker() == 1;
        
        if ((p1_smellSensitive && p2_isSmoker) || (p2_smellSensitive && p1_isSmoker)) {
            score -= 10;
        }
        
        return Math.max(0, score);
    }
    
    // =================================================================
    // SECTION 2: 手动分配与迁出 (保持不变)
    // =================================================================
    
    @Override
    @Transactional
    public DormAllocationLog assignStudentToBed(Long studentId, Long targetBedId) {
        // ... (省略 assignStudentToBed 完整逻辑, 保持不变) ...
        StuStudent student = studentMapper.selectById(studentId);
        if (student == null) throw new BusinessException("分配失败：学生信息不存在");
        SysUser studentUser = userMapper.selectById(student.getUserId());
        if (studentUser == null) throw new BusinessException("分配失败：关联用户信息不存在");
        
        if (student.getCurrentBedId() != null) {
            if (student.getCurrentBedId().equals(targetBedId)) throw new BusinessException("分配失败：学生已在该床位");
            log.info("学生 {} 原有床位 {}，执行自动迁出以分配新床位 {}", studentId, student.getCurrentBedId(), targetBedId);
            checkoutStudentFromBed(studentId, "1", "管理员手动调宿");
        }
        
        DormBed bed = bedMapper.selectById(targetBedId);
        if (bed == null) throw new BusinessException("分配失败：目标床位不存在");
        if (bed.getIsOccupied() != null && bed.getIsOccupied() == 1) throw new BusinessException("分配失败：目标床位已被占用");
        
        validateBedRules(bed, studentUser, student.getCurrentCampusId());
        
        bed.setOccupantUserId(student.getUserId());
        bed.setOccupantType("1");
        bed.setIsOccupied(1);
        bedMapper.updateById(bed);
        
        student.setCurrentBedId(targetBedId);
        studentMapper.updateById(student);
        
        int updatedRows = roomMapper.increaseOccupancy(bed.getRoomId());
        if (updatedRows == 0) {
            throw new BusinessException("分配失败：更新房间入住人数失败，可能房间已满");
        }
        
        DormAllocationLog allocationLog = new DormAllocationLog();
        allocationLog.setStudentId(studentId);
        allocationLog.setBedId(targetBedId);
        allocationLog.setActionType("0");
        allocationLog.setReasonType("管理员手动分配");
        allocationLog.setFlowStatus("2");
        allocationLog.setStartTime(LocalDateTime.now());
        allocationLog.setIsActive(1);
        
        allocationLogMapper.deactivatePreviousLogs(studentId, targetBedId, null);
        allocationLogMapper.insert(allocationLog);
        
        return allocationLog;
    }
    
    
    @Override
    @Transactional
    public void checkoutStudentFromBed(Long studentId, String actionType, String reason) {
        // ... (省略 checkoutStudentFromBed 完整逻辑, 保持不变) ...
        log.info("【迁出服务 V4】开始执行。StudentId: {}", studentId);
        
        StuStudent student = studentMapper.selectById(studentId);
        if (student == null || student.getCurrentBedId() == null) {
            log.warn("【迁出服务 V4】学生ID {} 无需迁出，当前无床位记录", studentId);
            return;
        }
        Long currentBedId = student.getCurrentBedId();
        DormBed bed = bedMapper.selectById(currentBedId);
        
        if (bed != null) {
            log.info("【迁出服务 V4】正在清空床位... BedId: {}", currentBedId);
            bed.setOccupantUserId(null);
            bed.setOccupantType(null);
            bed.setIsOccupied(0);
            bedMapper.updateById(bed);
            
            roomMapper.decreaseOccupancy(bed.getRoomId());
        } else {
            log.warn("【迁出服务 V4】学生ID {} 关联的床位ID {} 不存在...", studentId, currentBedId);
        }
        
        DormAllocationLog checkoutLog = new DormAllocationLog();
        checkoutLog.setStudentId(studentId);
        checkoutLog.setBedId(currentBedId);
        checkoutLog.setActionType(actionType);
        checkoutLog.setReasonType(reason);
        checkoutLog.setFlowStatus("2");
        checkoutLog.setStartTime(LocalDateTime.now());
        checkoutLog.setIsActive(0);
        
        allocationLogMapper.deactivatePreviousLogs(studentId, currentBedId, null);
        allocationLogMapper.insert(checkoutLog);
        
        log.info("【迁出服务 V4】执行完毕。StudentId: {}", studentId);
    }
    
    // =========================================================
    // 调宿申请流程：已修改
    // =========================================================
    
    /**
     * 学生提交调宿申请 (保持不变)
     */
    @Override
    @Transactional
    public void submitRoomChangeRequest(RoomChangeRequestDTO dto, Long loginId) {
        // ... (省略 submitRoomChangeRequest 完整逻辑, 保持不变) ...
        StuStudent student = studentMapper.selectOne(new LambdaQueryWrapper<StuStudent>().eq(StuStudent::getUserId, loginId));
        if (student == null) {
            throw new BusinessException("未找到关联的学生档案");
        }
        if (student.getCurrentBedId() == null) {
            throw new BusinessException("您当前没有床位，无法申请调宿");
        }
        SysUser studentUser = userMapper.selectById(loginId);
        if (studentUser == null) {
            throw new BusinessException("关联用户不存在");
        }
        
        boolean hasPendingRequest = requestMapper.exists(new LambdaQueryWrapper<BizRoomChangeRequest>()
                .eq(BizRoomChangeRequest::getStudentId, student.getStudentId())
                .in(BizRoomChangeRequest::getStatus, "0", "1")
        );
        if (hasPendingRequest) {
            throw new BusinessException("您已有待处理的调宿申请，请勿重复提交");
        }
        
        if (dto.getTargetBedId() != null) {
            DormBed targetBed = bedMapper.selectById(dto.getTargetBedId());
            validateBedRules(targetBed, studentUser, student.getCurrentCampusId());
        }
        
        BizRoomChangeRequest request = new BizRoomChangeRequest();
        BeanUtil.copyProperties(dto, request);
        request.setStudentId(student.getStudentId());
        request.setCurrentBedId(student.getCurrentBedId());
        request.setStatus("0");
        request.setSubmitTime(LocalDateTime.now());
        request.setCreateTime(LocalDateTime.now());
        
        requestMapper.insert(request);
    }
    
    /**
     * 管理员审批调宿申请 (保持不变)
     */
    @Override
    @Transactional
    public void approveRoomChangeRequest(RoomChangeApprovalDTO dto, Long adminUserId) {
   
        BizRoomChangeRequest request = requestMapper.selectById(dto.getRequestId());
        if (request == null) {
            throw new BusinessException("申请单不存在");
        }
        if (!"0".equals(request.getStatus())) {
            throw new BusinessException("该申请单已处理，请勿重复操作");
        }
        
        if ("2".equals(dto.getStatus())) {
            request.setStatus("2");
            request.setApprovalBy(adminUserId);
            request.setApprovalTime(LocalDateTime.now());
            request.setApprovalOpinion(dto.getApprovalOpinion());
            requestMapper.updateById(request);
            return;
        }
        
        if ("1".equals(dto.getStatus())) {
            Long targetBedId = request.getTargetBedId();
            
            if (targetBedId == null) {
                targetBedId = dto.getTargetBedId();
            }
            if (targetBedId == null) {
                throw new BusinessException("批准失败：必须为学生指定一个目标床位");
            }
            
            StuStudent student = studentMapper.selectById(request.getStudentId());
            SysUser studentUser = userMapper.selectById(student.getUserId());
            DormBed targetBed = bedMapper.selectById(targetBedId);
            
            validateBedRules(targetBed, studentUser, student.getCurrentCampusId());
            
            log.info("管理员 {} 批准调宿申请 {}，开始执行床位变更...", adminUserId, request.getRequestId());
            DormAllocationLog logEntry = assignStudentToBed(request.getStudentId(), targetBedId);
            
            request.setStatus("3");
            request.setTargetBedId(targetBedId);
            request.setApprovalBy(adminUserId);
            request.setApprovalTime(LocalDateTime.now());
            request.setApprovalOpinion("批准：" + (dto.getApprovalOpinion() != null ? dto.getApprovalOpinion() : ""));
            request.setFinishTime(LocalDateTime.now());
            requestMapper.updateById(request);
        }
    }
    
    /**
     * 使用 DTO 修复分页
     * 管理员分页查询所有调宿申请
     */
    @Override
    public Page<RoomChangeRequestVO> selectRoomChangeRequestPage(RoomChangeQueryDTO queryDTO) { // <-- 修改参数
        
        // 1. 创建用于查询基础数据的 Page<Entity>
        Page<BizRoomChangeRequest> basicPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 创建用于返回的 Page<VO>
        Page<RoomChangeRequestVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        
        // 2. 使用 basicPage (Page<Entity>) 来执行查询
        requestMapper.selectPage(basicPage,
                new LambdaQueryWrapper<BizRoomChangeRequest>()
                        .orderByDesc(BizRoomChangeRequest::getSubmitTime)
                // TODO: 在这里使用 DTO 中的筛选条件 (queryDTO.getStatus() 等)
        );
        
        // 3. 拷贝分页数据到 voPage
        voPage.setTotal(basicPage.getTotal());
        voPage.setPages(basicPage.getPages());
        
        if (basicPage.getRecords().isEmpty()) {
            voPage.setRecords(Collections.emptyList());
            return voPage; // <-- 返回 voPage
        }
        
        // 4. 填充数据
        List<RoomChangeRequestVO> voList = fillRoomChangeRequestVOInfo(basicPage.getRecords());
        voPage.setRecords(voList);
        return voPage; // <-- 返回 voPage
    }
    
    /**
     * 学生查询自己的调宿申请
     * (此方法保持不变)
     */
    @Override
    public List<RoomChangeRequestVO> getMyRoomChangeRequests(Long loginId) {
        // ... (省略 getMyRoomChangeRequests 完整逻辑, 保持不变) ...
        StuStudent student = studentMapper.selectOne(new LambdaQueryWrapper<StuStudent>().eq(StuStudent::getUserId, loginId));
        if (student == null) {
            return Collections.emptyList();
        }
        
        List<BizRoomChangeRequest> requests = requestMapper.selectList(new LambdaQueryWrapper<BizRoomChangeRequest>()
                .eq(BizRoomChangeRequest::getStudentId, student.getStudentId())
                .orderByDesc(BizRoomChangeRequest::getSubmitTime)
        );
        
        return fillRoomChangeRequestVOInfo(requests);
    }
    
    // =================================================================
    // SECTION 4: 辅助方法 (保持不变)
    // =================================================================
    
    /**
     * 辅助方法：校验目标床位是否符合分配规则
     */
    private void validateBedRules(DormBed targetBed, SysUser studentUser, Long studentCampusId) {

        if (targetBed == null || (targetBed.getIsOccupied() != null && targetBed.getIsOccupied() == 1)) {
            throw new BusinessException("目标床位不存在或是已被占用");
        }
        
        DormRoom targetRoom = roomMapper.selectById(targetBed.getRoomId());
        if (targetRoom == null) throw new BusinessException("目标床位关联的房间信息不存在");
        if (!"00".equals(targetRoom.getRoomPurposeType())) throw new BusinessException("目标房间不是学生宿舍，无法申请");
        if (!"0".equals(targetRoom.getRoomStatus())) throw new BusinessException("目标房间当前状态异常（如维修中），无法申请");
        
        DormFloor targetFloor = floorMapper.selectById(targetRoom.getFloorId());
        if (targetFloor == null) throw new BusinessException("目标床位关联的楼层信息不存在");
        
        DormBuilding targetBuilding = buildingMapper.selectById(targetFloor.getBuildingId());
        if (targetBuilding == null) throw new BusinessException("目标床位关联的楼栋信息不存在");
        
        if (!Objects.equals(studentCampusId, targetBuilding.getCampusId())) {
            throw new BusinessException("目标床位与您不在同一校区");
        }
        
        String studentGender = studentUser.getSex();
        String buildingGenderType = targetBuilding.getGenderType();
        boolean genderMatch = false;
        
        if (Objects.equals(buildingGenderType, studentGender)) {
            genderMatch = true;
        } else if (Objects.equals(buildingGenderType, "2")) {
            DormFloorGenderRule floorRule = floorRuleMapper.selectOne(
                    new LambdaQueryWrapper<DormFloorGenderRule>().eq(DormFloorGenderRule::getFloorId, targetFloor.getFloorId())
            );
            if (floorRule != null && Objects.equals(floorRule.getGenderType(), studentGender)) {
                genderMatch = true;
            }
        }
        
        if (!genderMatch) {
            throw new BusinessException("您的性别与目标床位（所在楼栋或楼层）的性别限制不符");
        }
    }
    
    /**
     * 辅助方法：填充调宿申请 VO 的关联信息
     */
    private List<RoomChangeRequestVO> fillRoomChangeRequestVOInfo(List<BizRoomChangeRequest> requests) {
        
        if (CollUtil.isEmpty(requests)) {
            return Collections.emptyList();
        }
        
        List<Long> studentIds = requests.stream().map(BizRoomChangeRequest::getStudentId).distinct().collect(Collectors.toList());
        List<Long> bedIds = requests.stream().map(BizRoomChangeRequest::getCurrentBedId).distinct().collect(Collectors.toList());
        bedIds.addAll(requests.stream().map(BizRoomChangeRequest::getTargetBedId).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
        List<Long> approvalUserIds = requests.stream().map(BizRoomChangeRequest::getApprovalBy).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        
        Map<Long, StuStudent> studentMap = studentIds.isEmpty() ? Collections.emptyMap() :
                studentMapper.selectBatchIds(studentIds).stream().collect(Collectors.toMap(StuStudent::getStudentId, Function.identity()));
        Map<Long, SysUser> userMap = approvalUserIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(approvalUserIds).stream().collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
        
        List<Long> studentUserIds = studentMap.values().stream().map(StuStudent::getUserId).collect(Collectors.toList());
        Map<Long, SysUser> studentUserMap = studentUserIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(studentUserIds).stream().collect(Collectors.toMap(SysUser::getUserId, Function.identity()));
        
        Map<Long, String> bedInfoMap = new HashMap<>();
        if (!bedIds.isEmpty()) {
            Map<Long, DormBed> bedMap = bedMapper.selectBatchIds(bedIds).stream().collect(Collectors.toMap(DormBed::getBedId, b -> b));
            List<Long> roomIds = bedMap.values().stream().map(DormBed::getRoomId).distinct().collect(Collectors.toList());
            Map<Long, DormRoom> roomMap = roomIds.isEmpty() ? Collections.emptyMap() : roomMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(DormRoom::getRoomId, r -> r));
            List<Long> floorIds = roomMap.values().stream().map(DormRoom::getFloorId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            Map<Long, DormFloor> floorMap = floorIds.isEmpty() ? Collections.emptyMap() : floorMapper.selectBatchIds(floorIds).stream().collect(Collectors.toMap(DormFloor::getFloorId, f -> f));
            List<Long> buildingIds = floorMap.values().stream().map(DormFloor::getBuildingId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            Map<Long, DormBuilding> buildingMap = buildingIds.isEmpty() ? Collections.emptyMap() : buildingMapper.selectBatchIds(buildingIds).stream().collect(Collectors.toMap(DormBuilding::getBuildingId, b -> b));
            Map<Long, String> campusMap = buildingMap.values().isEmpty() ? Collections.emptyMap() : campusMapper.selectBatchIds(buildingMap.values().stream().map(DormBuilding::getCampusId).distinct().collect(Collectors.toList())).stream().collect(Collectors.toMap(SysCampus::getCampusId, SysCampus::getCampusName));
            
            bedMap.forEach((bedId, bed) -> {
                DormRoom room = roomMap.get(bed.getRoomId());
                if(room == null) return;
                DormFloor floor = floorMap.get(room.getFloorId());
                if(floor == null) return;
                DormBuilding building = buildingMap.get(floor.getBuildingId());
                if(building == null) return;
                String campusName = campusMap.get(building.getCampusId());
                bedInfoMap.put(bedId, String.format("%s-%s-%s层-%s-%s", (campusName != null ? campusName : "N/A"), building.getBuildingName(), floor.getFloorNumber(), room.getRoomNumber(), bed.getBedNumber()));
            });
        }
        
        return requests.stream().map(request -> {
            RoomChangeRequestVO vo = BeanUtil.copyProperties(request, RoomChangeRequestVO.class);
            StuStudent student = studentMap.get(request.getStudentId());
            if (student != null) {
                SysUser user = studentUserMap.get(student.getUserId());
                if (user != null) {
                    vo.setStudentName(user.getRealName());
                    vo.setStudentUsername(user.getUsername());
                }
            }
            SysUser approver = userMap.get(request.getApprovalBy());
            if (approver != null) {
                vo.setApprovalByName(approver.getRealName());
            }
            vo.setCurrentBedInfo(bedInfoMap.get(request.getCurrentBedId()));
            vo.setTargetBedInfo(bedInfoMap.get(request.getTargetBedId()));
            return vo;
        }).collect(Collectors.toList());
    }
}