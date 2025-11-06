package com.dormitory.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.StaffInfoQueryDTO;
import com.dormitory.dto.StaffInfoUpdateDTO;
import com.dormitory.entity.*;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.*;
import com.dormitory.service.IBizStaffInfoService;
import com.dormitory.vo.StaffInfoVO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 教职工/后勤住宿信息业务服务实现类
 */
@Service
public class BizStaffInfoServiceImpl extends ServiceImpl<BizStaffInfoMapper, BizStaffInfo> implements IBizStaffInfoService {
    
    // 1. 注入所有需要的 Mapper
    @Autowired private SysUserMapper userMapper;
    @Autowired private SysDepartmentMapper departmentMapper;
    @Autowired private DormBedMapper bedMapper;
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private DormBuildingMapper buildingMapper;
    @Autowired private DormFloorMapper floorMapper;
    @Autowired private SysCampusMapper campusMapper;
    
    private static final Logger log = LoggerFactory.getLogger(BizStaffInfoServiceImpl.class); // 新增：日志
    
    /**
     * 分页查询教职工列表 (返回 StaffInfoVO)
     * 增加数据权限过滤
     */
    @Override
    public Page<StaffInfoVO> selectStaffInfoPage(StaffInfoQueryDTO queryDTO) {
        
        
        // 新增：数据越权安全过滤
        Long loginId = StpUtil.getLoginIdAsLong();
        
        // 0. 检查是否为学生 (RoleKey = "student")
        if (StpUtil.hasRole("student")) {
            log.warn("[数据权限] 检测到学生 (ID: {}) 尝试查询教职工列表。已禁止访问。", loginId);
            // 学生不允许查询教职工列表，返回空
            return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        }
        
        log.debug("[数据权限] 检测到管理员/教职工 (ID: {}) 查询教职工列表。", loginId);
        
        
        // 1. 构建 BizStaffInfo 表的基础查询条件
        LambdaQueryWrapper<BizStaffInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getDepartmentId() != null, BizStaffInfo::getDepartmentId, queryDTO.getDepartmentId())
                .like(StrUtil.isNotEmpty(queryDTO.getJobTitle()), BizStaffInfo::getJobTitle, queryDTO.getJobTitle())
                .eq(queryDTO.getIsOnCampusResident() != null, BizStaffInfo::getIsOnCampusResident, queryDTO.getIsOnCampusResident());
        
        // 2. 【核心修正】处理姓名(nickname)和工号(username)查询 (联查 sys_user)
        if (StrUtil.isNotEmpty(queryDTO.getNickname()) || StrUtil.isNotEmpty(queryDTO.getUsername())) {
            LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(StrUtil.isNotEmpty(queryDTO.getNickname()), SysUser::getRealName, queryDTO.getNickname()) // 查真实姓名
                    .like(StrUtil.isNotEmpty(queryDTO.getUsername()), SysUser::getUsername, queryDTO.getUsername()) // 查工号
                    .select(SysUser::getUserId);
            
            List<Object> userIdsObj = userMapper.selectObjs(userWrapper);
            List<Long> userIds = userIdsObj.stream().map(o -> (Long) o).collect(Collectors.toList());
            
            if (CollUtil.isEmpty(userIds)) {
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            wrapper.in(BizStaffInfo::getUserId, userIds);
        }
        
        // 3. 执行主分页查询
        Page<BizStaffInfo> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        
        // 4. 创建 VO 分页对象
        Page<StaffInfoVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        
        if (CollUtil.isEmpty(page.getRecords())) {
            return voPage; // 如果没有记录，直接返回
        }
        
        // 5. 批量填充关联信息到 VO
        List<StaffInfoVO> voList = fillStaffVOInfo(page.getRecords());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    /**
     * 根据 UserID 获取教职工详细信息 (返回 StaffInfoVO)
     */
    @Override
    public StaffInfoVO getStaffInfoByUserId(Long userId) {
        BizStaffInfo staffInfo = this.getById(userId); // 主键就是 user_id
        if (staffInfo == null) {
            return null;
        }
        // 调用辅助方法填充关联数据
        return fillStaffVOInfo(Collections.singletonList(staffInfo)).get(0);
    }
    
    /**
     * 更新教职工信息 (核心：合同与离职日期计算)
     */
    @Override
    @Transactional // 开启事务
    public void updateStaffInfo(StaffInfoUpdateDTO updateDTO) { // 使用 DTO
        if (updateDTO.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        // 1. 获取数据库中的旧记录
        BizStaffInfo existingInfo = this.getById(updateDTO.getUserId());
        if (existingInfo == null) {
            throw new BusinessException("教职工档案不存在");
        }
        if (existingInfo.getHireDate() == null) {
            throw new BusinessException("入职日期缺失，无法计算预计离职日期");
        }
        
        // 2. 核心逻辑：自动计算 expectedLeaveDate
        LocalDate hireDate = existingInfo.getHireDate();
        
        Integer duration = updateDTO.getContractDurationYears() != null
                ? updateDTO.getContractDurationYears()
                : existingInfo.getContractDurationYears();
        
        if (duration != null && duration > 0) {
            LocalDate expectedLeaveDate = hireDate.plusYears(duration);
            updateDTO.setExpectedLeaveDate(expectedLeaveDate);
        } else {
            updateDTO.setExpectedLeaveDate(null);
        }
        
        // 3. 执行更新
        BizStaffInfo staffInfo = new BizStaffInfo();
        BeanUtil.copyProperties(updateDTO, staffInfo);
        
        boolean success = this.updateById(staffInfo);
        
        if (!success) {
            throw new BusinessException("教职工信息更新失败");
        }
    }
    
    /**
     * 新增：实现带校验的新增
     * 新增教职工信息 (用于同步 SysUser 新增的记录)
     */
    @Override
    @Transactional
    public void addStaffInfo(BizStaffInfo staffInfo) {
        if (staffInfo.getUserId() == null) {
            throw new BusinessException("新增档案失败：必须关联用户ID (userId)");
        }
        
        // 1. 校验关联的用户ID是否存在
        SysUser user = userMapper.selectById(staffInfo.getUserId());
        if (user == null) {
            throw new BusinessException("新增档案失败：关联的用户ID " + staffInfo.getUserId() + " 不存在");
        }
        
        // 2. 校验 userType 是否为教职工
        if ("1".equals(user.getUserType())) { // 1=Student
            throw new BusinessException("新增档案失败：该用户是学生，无法添加教职工档案");
        }
        
        // 3. 校验该档案是否已存在
        if (this.exists(new LambdaQueryWrapper<BizStaffInfo>().eq(BizStaffInfo::getUserId, staffInfo.getUserId()))) {
            throw new BusinessException("新增档案失败：该用户的教职工档案已存在");
        }
        
        // 4. 校验部门ID（如果提供了）
        if (staffInfo.getDepartmentId() != null) {
            if (!departmentMapper.exists(new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getDeptId, staffInfo.getDepartmentId()))) {
                throw new BusinessException("新增档案失败：关联的部门ID不存在");
            }
        }
        
        // 5. 执行保存
        this.save(staffInfo);
    }
    
    
    /**
     * 【V5.1 修正】辅助方法：批量填充 StaffInfoVO 的关联信息 (含校区)
     */
    private List<StaffInfoVO> fillStaffVOInfo(List<BizStaffInfo> staffList) {
        if (CollUtil.isEmpty(staffList)) {
            return Collections.emptyList();
        }
        
        // 1. 提取所有需要查询的关联 ID
        List<Long> userIds = staffList.stream().map(BizStaffInfo::getUserId).distinct().collect(Collectors.toList());
        List<Long> deptIds = staffList.stream().map(BizStaffInfo::getDepartmentId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> bedIds = staffList.stream().map(BizStaffInfo::getCurrentBedId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        
        // 2. 批量查询关联信息 (Maps)
        final Map<Long, SysUser> userMap = userIds.isEmpty() ? Collections.emptyMap() :
                userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(SysUser::getUserId, u -> u));
        
        final Map<Long, String> deptMap = deptIds.isEmpty() ? Collections.emptyMap() :
                departmentMapper.selectBatchIds(deptIds).stream().collect(Collectors.toMap(SysDepartment::getDeptId, SysDepartment::getDeptName));
        
        // 3. 【V5.1 修正】床位 -> 房间 -> 楼层 -> 楼栋 -> 校区 联查逻辑
        final Map<Long, DormBed> bedMap = bedIds.isEmpty() ? Collections.emptyMap() :
                bedMapper.selectBatchIds(bedIds).stream().collect(Collectors.toMap(DormBed::getBedId, b -> b));
        
        List<Long> roomIds = bedMap.values().stream().map(DormBed::getRoomId).distinct().collect(Collectors.toList());
        final Map<Long, DormRoom> roomMap = roomIds.isEmpty() ? Collections.emptyMap() :
                roomMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(DormRoom::getRoomId, r -> r));
        
        List<Long> floorIds = roomMap.values().stream().map(DormRoom::getFloorId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        final Map<Long, DormFloor> floorMap = floorIds.isEmpty() ? Collections.emptyMap() :
                floorMapper.selectBatchIds(floorIds).stream().collect(Collectors.toMap(DormFloor::getFloorId, f -> f));
        
        List<Long> buildingIds = floorMap.values().stream().map(DormFloor::getBuildingId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        // 【修正】获取完整的楼栋对象，以便获取 campusId
        final Map<Long, DormBuilding> buildingMap = buildingIds.isEmpty() ? Collections.emptyMap() :
                buildingMapper.selectBatchIds(buildingIds).stream().collect(Collectors.toMap(DormBuilding::getBuildingId, b -> b));
        
        // 【新增】获取校区 Map
        List<Long> campusIds = buildingMap.values().stream().map(DormBuilding::getCampusId).distinct().collect(Collectors.toList());
        final Map<Long, String> campusMap = campusIds.isEmpty() ? Collections.emptyMap() :
                campusMapper.selectBatchIds(campusIds).stream().collect(Collectors.toMap(SysCampus::getCampusId, SysCampus::getCampusName));
        
        
        // 4. 转换并填充 VO
        return staffList.stream().map(staffInfo -> {
            StaffInfoVO vo = BeanUtil.copyProperties(staffInfo, StaffInfoVO.class);
            
            // 填充 SysUser 信息
            SysUser user = userMap.get(staffInfo.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername()); // 工号
                vo.setRealName(user.getRealName()); // 姓名
                vo.setNickname(user.getNickname()); // 昵称
                vo.setSex(user.getSex());
                vo.setPhoneNumber(user.getPhoneNumber());
                vo.setEmail(user.getEmail());
                vo.setDateOfBirth(user.getDateOfBirth());
                vo.setHometown(user.getHometown());
                vo.setPoliticalStatus(user.getPoliticalStatus());
                vo.setEthnicity(user.getEthnicity());
                vo.setHomeAddress(user.getHomeAddress());
                vo.setAvatar(user.getAvatar());
            }
            
            // 填充 部门名称
            vo.setDepartmentName(deptMap.get(staffInfo.getDepartmentId()));
            
            // 【V5.1 核心修正】填充床位信息 (校区-楼栋-楼层-房间-床位)
            DormBed bed = bedMap.get(staffInfo.getCurrentBedId());
            if (bed != null) {
                DormRoom room = roomMap.get(bed.getRoomId());
                if (room != null) {
                    DormFloor floor = floorMap.get(room.getFloorId());
                    if (floor != null) {
                        DormBuilding building = buildingMap.get(floor.getBuildingId());
                        if (building != null) {
                            String campusName = campusMap.get(building.getCampusId());
                            // 格式：校区-楼栋-楼层号-房间号-床号
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
                vo.setBedInfo(null); // 确保未分配时为 null
            }
            return vo;
        }).collect(Collectors.toList());
    }
}