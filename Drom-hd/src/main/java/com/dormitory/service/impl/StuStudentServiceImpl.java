package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.StudentQueryDTO;
import com.dormitory.dto.StudentUpdateDTO;
import com.dormitory.entity.*;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.*;
import com.dormitory.service.IDormAllocationService;
import com.dormitory.service.IStuStudentService;
import com.dormitory.vo.StudentVO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学生详细信息业务服务实现类
 * v5.1 已修正 bedInfo 格式
 */
@Service
public class StuStudentServiceImpl extends ServiceImpl<StuStudentMapper, StuStudent> implements IStuStudentService {
    
    // 1. 注入所有需要的 Mappers 和 Services
    @Autowired private SysUserMapper userMapper;
    @Autowired private SysDepartmentMapper departmentMapper;
    @Autowired private SysCampusMapper campusMapper;
    @Autowired private DormBedMapper bedMapper;
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private DormBuildingMapper buildingMapper;
    @Autowired private BizClassMapper classMapper;
    @Autowired private IDormAllocationService allocationService;
    @Autowired private DormFloorMapper floorMapper;
    
    private static final Logger log = LoggerFactory.getLogger(StuStudentServiceImpl.class); // 新增日志
    
    
    /**
     * 分页查询学生列表 (复杂联查，返回 VO，增加数据权限过滤)
     */
    @Override
    public Page<StudentVO> selectStudentPage(StudentQueryDTO queryDTO) {
        
        // 1. 【联查】处理院系(Department)和专业(Major)查询 (先查 biz_class)
        List<Long> classIdsToFilter = null;
        if (queryDTO.getDepartmentId() != null || StrUtil.isNotEmpty(queryDTO.getMajorName())) {
            LambdaQueryWrapper<BizClass> classWrapper = new LambdaQueryWrapper<>();
            classWrapper.eq(queryDTO.getDepartmentId() != null, BizClass::getDepartmentId, queryDTO.getDepartmentId())
                    .like(StrUtil.isNotEmpty(queryDTO.getMajorName()), BizClass::getMajorName, queryDTO.getMajorName())
                    .select(BizClass::getClassId);
            List<Object> classIdsObj = classMapper.selectObjs(classWrapper);
            classIdsToFilter = classIdsObj.stream().map(o -> (Long) o).collect(Collectors.toList());
            if (CollUtil.isEmpty(classIdsToFilter)) {
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
        }
        
        // 2. 构建 StuStudent 表的基础查询条件
        LambdaQueryWrapper<StuStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getCurrentCampusId() != null, StuStudent::getCurrentCampusId, queryDTO.getCurrentCampusId())
                .eq(queryDTO.getCurrentGradeLevel() != null, StuStudent::getCurrentGradeLevel, queryDTO.getCurrentGradeLevel())
                .eq(StrUtil.isNotEmpty(queryDTO.getAcademicStatus()), StuStudent::getAcademicStatus, queryDTO.getAcademicStatus());
        
        if (classIdsToFilter != null) {
            wrapper.in(StuStudent::getClassId, classIdsToFilter);
        }
        
        // 3. 【联查】处理姓名(realName)和学号(username)模糊查询 (联查 sys_user)
        List<Long> userIdsFromQuery = null;
        if (StrUtil.isNotEmpty(queryDTO.getNickname()) || StrUtil.isNotEmpty(queryDTO.getStuNumber())) {
            LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(StrUtil.isNotEmpty(queryDTO.getNickname()), SysUser::getRealName, queryDTO.getNickname())
                    .like(StrUtil.isNotEmpty(queryDTO.getStuNumber()), SysUser::getUsername, queryDTO.getStuNumber())
                    .select(SysUser::getUserId);
            List<Object> userIdsObj = userMapper.selectObjs(userWrapper);
            userIdsFromQuery = userIdsObj.stream().map(o -> (Long) o).collect(Collectors.toList());
            if (CollUtil.isEmpty(userIdsFromQuery)) {
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            // 先不应用 .in(wrapper)，等待权限过滤合并
        }
        
        // =========================================================
        // 4. 新增：数据越权安全过滤
        // =========================================================
        Long loginId = StpUtil.getLoginIdAsLong();
        List<Long> allowedUserIds = null; // 用于权限过滤
        
        if (StpUtil.hasRole("student")) {
            // 4.1 学生：强制只能看自己
            log.debug("[数据权限] 检测到学生 (ID: {}) 查询学生信息，强制过滤本人数据。", loginId);
            allowedUserIds = Collections.singletonList(loginId);
            
        } else if (StpUtil.hasRole("counselor")) {
            // 4.2 辅导员：强制只能看自己班的学生
            log.debug("[数据权限] 检测到辅导员 (ID: {}) 查询学生信息，开始过滤所管班级学生数据。", loginId);
            
            List<Long> classIds = classMapper.selectList(new LambdaQueryWrapper<BizClass>()
                            .eq(BizClass::getCounselorUserId, loginId))
                    .stream().map(BizClass::getClassId).collect(Collectors.toList());
            
            if (CollUtil.isEmpty(classIds)) {
                log.warn("[数据权限] 辅导员 (ID: {}) 未关联任何班级，返回空数据。", loginId);
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            
            // 找出这些班级下的所有学生
            allowedUserIds = this.list(new LambdaQueryWrapper<StuStudent>()
                            .in(StuStudent::getClassId, classIds))
                    .stream().map(StuStudent::getUserId).distinct().collect(Collectors.toList());
            
            if (CollUtil.isEmpty(allowedUserIds)) {
                log.warn("[数据权限] 辅导员 (ID: {}) 所管班级无学生，返回空数据。", loginId);
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            
        } else {
            // 4.3 Admin / 宿管 / 其他：允许查询
            log.debug("[数据权限] 检测到管理员 (ID: {})，允许查询所有数据。", loginId);
        }
        
        // 5. 【合并过滤条件】
        if (allowedUserIds != null) {
            // 如果有权限限制（学生/辅导员）
            if (userIdsFromQuery != null) {
                // 如果还有查询条件（学号/姓名），取二者交集
                allowedUserIds.retainAll(userIdsFromQuery);
            }
            if (CollUtil.isEmpty(allowedUserIds)) {
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            wrapper.in(StuStudent::getUserId, allowedUserIds);
            
        } else if (userIdsFromQuery != null) {
            // 如果没有权限限制（Admin），但有查询条件
            wrapper.in(StuStudent::getUserId, userIdsFromQuery);
        }
        // =========================================================
        
        // 6. 执行分页查询 (StuStudent 基础数据)
        Page<StuStudent> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        Page<StudentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        
        if (CollUtil.isEmpty(page.getRecords())) {
            return voPage;
        }
        
        // 7. 批量填充关联信息到 VO
        List<StudentVO> voList = fillStudentVOInfo(page.getRecords());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    
    /**
     * 根据学生ID获取详细信息 (返回 VO)
     */
    @Override
    public StudentVO getStudentVoById(Long studentId) {
        StuStudent student = this.getById(studentId);
        if (student == null) {
            return null;
        }
        return fillStudentVOInfo(Collections.singletonList(student)).get(0);
    }
    
    
//    /**
//     * 更新学生信息 (V4 修正版)
//     */
//    @Override
//    @Transactional
//    public void updateStudentInfo(StudentUpdateDTO updateDTO) {
//        // 1. 校验
//        if (updateDTO.getStudentId() == null) {
//            throw new BusinessException("学生ID不能为空");
//        }
//        StuStudent dbStudent = this.getById(updateDTO.getStudentId());
//        if (dbStudent == null) {
//            throw new BusinessException("学生档案不存在");
//        }
//
//        String newStatus = updateDTO.getAcademicStatus();
//        boolean checkoutTriggered = false;
//
//        // 2. 【核心业务触发器】检查学籍状态变更
//        if (newStatus != null && !newStatus.equals(dbStudent.getAcademicStatus())) {
//
//            if (List.of("1", "2", "3").contains(newStatus)) { // 1:休学, 2:毕业, 3:退学
//
//                if (dbStudent.getCurrentBedId() != null) {
//
//                    log.info("检测到学生ID {} 状态变为 {}，自动执行床位迁出...", dbStudent.getStudentId(), newStatus);
//
//                    String reason = "学籍状态变更 (" + newStatus + ")";
//
//                    // 调用迁出服务 (此方法只操作 bed, room, log)
//                    allocationService.checkoutStudentFromBed(dbStudent.getStudentId(), newStatus, reason);
//
//                    if ("2".equals(newStatus) || "3".equals(newStatus)) {
//                        SysUser userToUpdate = new SysUser();
//                        userToUpdate.setUserId(dbStudent.getUserId());
//                        userToUpdate.setStatus(1); // 1 = 禁用
//                        userMapper.updateById(userToUpdate);
//                    }
//
//                    checkoutTriggered = true; // 标记迁出已执行
//                }
//            }
//        }
//
//        // 3. 更新 StuStudent 表信息
//        BeanUtil.copyProperties(updateDTO, dbStudent, "userId", "studentId");
//
//        // 4. 修复调用迁出
//        if (checkoutTriggered) {
//            dbStudent.setCurrentBedId(null);
//        }
//
//        // 5. 执行更新 (dbStudent 包含了所有字段)
//        this.updateById(dbStudent);
//
//        // 6. 更新 SysUser 表中的基础信息
//        SysUser userToUpdate = new SysUser();
//        userToUpdate.setUserId(dbStudent.getUserId());
//        BeanUtil.copyProperties(updateDTO, userToUpdate);
//
//        if (userToUpdate.getNickname() != null || userToUpdate.getRealName() != null || userToUpdate.getSex() != null ||
//                userToUpdate.getPhoneNumber() != null || userToUpdate.getEmail() != null || userToUpdate.getDateOfBirth() != null ||
//                userToUpdate.getHometown() != null || userToUpdate.getPoliticalStatus() != null ||
//                userToUpdate.getHomeAddress() != null || userToUpdate.getAvatar() != null || userToUpdate.getEthnicity() != null)
//        {
//            userMapper.updateById(userToUpdate);
//        }
//    }
    
    /**
     * 更新学生信息 (V5: 增加学籍变更时对 SysUser 的联动)
     */
    @Override
    @Transactional
    public void updateStudentInfo(StudentUpdateDTO updateDTO) {
        // 1. 校验
        if (updateDTO.getStudentId() == null) {
            throw new BusinessException("学生ID不能为空");
        }
        StuStudent dbStudent = this.getById(updateDTO.getStudentId());
        if (dbStudent == null) {
            throw new BusinessException("学生档案不存在");
        }
        
        String newStatus = updateDTO.getAcademicStatus();
        boolean checkoutTriggered = false;
        
        // 2. 【核心业务触发器】检查学籍状态变更
        if (newStatus != null && !newStatus.equals(dbStudent.getAcademicStatus())) {
            
            if (List.of("1", "2", "3").contains(newStatus)) { // 1:休学, 2:毕业, 3:退学
                
                if (dbStudent.getCurrentBedId() != null) {
                    
                    log.info("检测到学生ID {} 状态变为 {}，自动执行床位迁出...", dbStudent.getStudentId(), newStatus);
                    
                    String reason = "学籍状态变更 (" + newStatus + ")";
                    
                    // 【V4 修正】调用迁出服务 (此方法只操作 bed, room, log)
                    allocationService.checkoutStudentFromBed(dbStudent.getStudentId(), newStatus, reason);
                    
                    checkoutTriggered = true; // 标记迁出已执行
                }
                
                // --- 【【【【【 新增逻辑：禁用 SysUser 账户 】】】】】 ---
                // 如果是毕业(2)或退学(3)，则禁用该学生的 SysUser 账户
                if ("2".equals(newStatus) || "3".equals(newStatus)) {
                    log.info("学生ID {} 状态变为 毕业/退学，自动禁用其 SysUser (ID: {}) 账户...",
                            dbStudent.getStudentId(), dbStudent.getUserId());
                    SysUser userToDisable = new SysUser();
                    userToDisable.setUserId(dbStudent.getUserId());
                    userToDisable.setStatus(1); // 1 = 禁用
                    userMapper.updateById(userToDisable);
                }
                // 如果是休学(1)，账户保持不变（允许登录查看信息），直到自动任务将其转为退学
                // --- 【【【【【 新增逻辑结束 】】】】】 ---
            }
        }
        
        // 3. 更新 StuStudent 表信息
        BeanUtil.copyProperties(updateDTO, dbStudent, "userId", "studentId");
        
        // 4. 【V4 关键修正】
        if (checkoutTriggered) {
            dbStudent.setCurrentBedId(null);
        }
        
        // 5. 执行更新 (dbStudent 包含了所有字段)
        this.updateById(dbStudent);
        
        // 6. 更新 SysUser 表中的基础信息
        SysUser userToUpdate = new SysUser();
        userToUpdate.setUserId(dbStudent.getUserId());
        BeanUtil.copyProperties(updateDTO, userToUpdate);
        
        if (userToUpdate.getNickname() != null || userToUpdate.getRealName() != null || userToUpdate.getSex() != null ||
                userToUpdate.getPhoneNumber() != null || userToUpdate.getEmail() != null || userToUpdate.getDateOfBirth() != null ||
                userToUpdate.getHometown() != null || userToUpdate.getPoliticalStatus() != null ||
                userToUpdate.getHomeAddress() != null || userToUpdate.getAvatar() != null || userToUpdate.getEthnicity() != null)
        {
            // 确保不意外解禁已被禁用的账户
            userToUpdate.setStatus(null); // 不通过此渠道更新状态
            userMapper.updateById(userToUpdate);
        }
    }
    
    
    /**
     * 【V5.1 修正】辅助方法：批量填充 StudentVO 的关联信息 (联查校区)
     */
    private List<StudentVO> fillStudentVOInfo(List<StuStudent> studentList) {
        if (CollUtil.isEmpty(studentList)) {
            return Collections.emptyList();
        }
        
        // --- 1. 批量获取基础关联 ID ---
        List<Long> userIds = studentList.stream().map(StuStudent::getUserId).distinct().collect(Collectors.toList());
        List<Long> campusIds = studentList.stream().map(StuStudent::getCurrentCampusId).distinct().collect(Collectors.toList());
        List<Long> supervisorIds = studentList.stream().map(StuStudent::getSupervisorUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> bedIds = studentList.stream().map(StuStudent::getCurrentBedId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        
        // --- 2. 获取班级信息 ---
        List<Long> classIds = studentList.stream().map(StuStudent::getClassId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        final Map<Long, BizClass> classMap = classIds.isEmpty() ? Collections.emptyMap() :
                classMapper.selectBatchIds(classIds).stream().collect(Collectors.toMap(BizClass::getClassId, c -> c));
        
        // --- 3. 获取院系和辅导员 ---
        List<Long> deptIds = classMap.values().stream().map(BizClass::getDepartmentId).distinct().collect(Collectors.toList());
        List<Long> counselorIds = classMap.values().stream().map(BizClass::getCounselorUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        
        // --- 4. 批量查询所有关联 Map ---
        userIds.addAll(supervisorIds);
        userIds.addAll(counselorIds);
        userIds = userIds.stream().distinct().collect(Collectors.toList());
        
        final Map<Long, SysUser> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(SysUser::getUserId, u -> u));
        final Map<Long, String> deptMap = deptIds.isEmpty() ? Collections.emptyMap() :
                departmentMapper.selectBatchIds(deptIds).stream().collect(Collectors.toMap(SysDepartment::getDeptId, SysDepartment::getDeptName));
        
        // 【修正】 campusMap 已经通过 campusIds 查询过了
        final Map<Long, String> campusMap = campusIds.isEmpty() ? Collections.emptyMap() :
                campusMapper.selectBatchIds(campusIds).stream().collect(Collectors.toMap(SysCampus::getCampusId, SysCampus::getCampusName));
        
        // --- 【V5.1 修正】床位 -> 房间 -> 楼层 -> 楼栋 联查逻辑 ---
        final Map<Long, DormBed> bedMap = bedIds.isEmpty() ? Collections.emptyMap() :
                bedMapper.selectBatchIds(bedIds).stream().collect(Collectors.toMap(DormBed::getBedId, b -> b));
        List<Long> roomIds = bedMap.values().stream().map(DormBed::getRoomId).distinct().collect(Collectors.toList());
        final Map<Long, DormRoom> roomMap = roomIds.isEmpty() ? Collections.emptyMap() :
                roomMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(DormRoom::getRoomId, r -> r));
        List<Long> floorIds = roomMap.values().stream().map(DormRoom::getFloorId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        final Map<Long, DormFloor> floorMap = floorIds.isEmpty() ? Collections.emptyMap() :
                floorMapper.selectBatchIds(floorIds).stream().collect(Collectors.toMap(DormFloor::getFloorId, f -> f));
        List<Long> buildingIds = floorMap.values().stream().map(DormFloor::getBuildingId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        // 【修正】获取楼栋全对象
        final Map<Long, DormBuilding> buildingMap = buildingIds.isEmpty() ? Collections.emptyMap() :
                buildingMapper.selectBatchIds(buildingIds).stream().collect(Collectors.toMap(DormBuilding::getBuildingId, b -> b));
        
        // 5. 转换并填充 VO
        return studentList.stream().map(student -> {
            StudentVO vo = BeanUtil.copyProperties(student, StudentVO.class);
            
            // 填充 SysUser 基础信息 (学生本人)
            SysUser user = userMap.get(student.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setRealName(user.getRealName());
                vo.setSex(user.getSex());
                vo.setPhoneNumber(user.getPhoneNumber());
                vo.setEmail(user.getEmail());
                vo.setDateOfBirth(user.getDateOfBirth());
                vo.setHometown(user.getHometown());
                vo.setPoliticalStatus(user.getPoliticalStatus());
                vo.setEthnicity(user.getEthnicity());
                vo.setHomeAddress(user.getHomeAddress());
                vo.setAvatar(user.getAvatar());
                vo.setStuNumber(user.getUsername());
            }
            
            // 填充校区
            vo.setCampusName(campusMap.get(student.getCurrentCampusId()));
            
            // 填充导师
            SysUser supervisor = userMap.get(student.getSupervisorUserId());
            if (supervisor != null) vo.setSupervisorName(supervisor.getRealName()); // 使用 RealName
            
            // 填充班级、院系、辅导员
            BizClass bizClass = classMap.get(student.getClassId());
            if (bizClass != null) {
                vo.setClassName(bizClass.getClassName());
                vo.setMajorName(bizClass.getMajorName());
                vo.setDepartmentName(deptMap.get(bizClass.getDepartmentId()));
                SysUser counselor = userMap.get(bizClass.getCounselorUserId());
                if (counselor != null) vo.setCounselorName(counselor.getRealName()); // 使用 RealName
            }
            
            // 【V5.1 核心修正】填充床位信息 (校区-楼栋-楼层-房间-床位)
            DormBed bed = bedMap.get(student.getCurrentBedId());
            if (bed != null) {
                DormRoom room = roomMap.get(bed.getRoomId());
                if (room != null) {
                    DormFloor floor = floorMap.get(room.getFloorId());
                    if (floor != null) {
                        DormBuilding building = buildingMap.get(floor.getBuildingId());
                        if (building != null) {
                            // 【修正】我们从 building 对象获取 campusId，然后从已有的 campusMap 中查找名称
                            String campusName = campusMap.get(building.getCampusId());
                            vo.setBedInfo(String.format("%s-%s-%s层-%s-%s",
                                    (campusName != null ? campusName : "N/A"),
                                    building.getBuildingName(),
                                    floor.getFloorNumber(), // 使用楼层号
                                    room.getRoomNumber(),
                                    bed.getBedNumber()));
                        }
                    }
                }
            } else {
                vo.setBedInfo(null);
            }
            return vo;
        }).collect(Collectors.toList());
    }
}