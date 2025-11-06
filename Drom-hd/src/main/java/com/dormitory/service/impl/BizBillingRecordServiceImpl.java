package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.BillingRecordQueryDTO;
import com.dormitory.dto.BillingPaymentDTO;
import com.dormitory.entity.*; // 【修改】导入所有实体
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.*; // 【修改】导入所有 Mapper
import com.dormitory.service.IBizBillingRecordService;
import cn.dev33.satoken.stp.StpUtil; // 【新增】
import cn.hutool.core.collection.CollUtil; // 【新增】
import org.slf4j.Logger; // 【新增】
import org.slf4j.LoggerFactory; // 【新增】
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects; // 【新增】
import java.util.stream.Collectors;

/**
 * 账单记录业务服务实现类
 */
@Service
public class BizBillingRecordServiceImpl extends ServiceImpl<BizBillingRecordMapper, BizBillingRecord> implements IBizBillingRecordService {
    
    @Autowired private DormRoomMapper roomMapper;
    @Autowired private DormBuildingMapper buildingMapper;
    
    // 【【【【【 新增注入 (用于数据权限) 】】】】】
    @Autowired private StuStudentMapper studentMapper;
    @Autowired private DormBedMapper bedMapper;
    @Autowired private BizClassMapper classMapper;
    
    private static final Logger log = LoggerFactory.getLogger(BizBillingRecordServiceImpl.class);
    
    /**
     * 分页查询账单记录 (含房间号、楼栋名 和 【安全过滤】)
     */
    @Override
    public Page<BizBillingRecord> selectRecordPage(BillingRecordQueryDTO queryDTO) {
        
        // 1. 构建基础查询条件
        LambdaQueryWrapper<BizBillingRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getMeterType() != null, BizBillingRecord::getMeterType, queryDTO.getMeterType())
                .eq(queryDTO.getStatus() != null, BizBillingRecord::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getIsPaid() != null, BizBillingRecord::getIsPaid, queryDTO.getIsPaid())
                .eq(queryDTO.getCycleStartDate() != null, BizBillingRecord::getCycleStartDate, queryDTO.getCycleStartDate());
        
        // =========================================================
        // 【【【【【 新增：数据越权安全过滤 】】】】】
        // =========================================================
        Long loginId = StpUtil.getLoginIdAsLong();
        
        // 1. 检查是否为学生 (RoleKey = "student")
        if (StpUtil.hasRole("student")) {
            log.debug("[数据权限] 检测到学生 (ID: {}) 查询账单，强制过滤本人房间数据。", loginId);
            
            // 1.1 查找学生档案
            StuStudent student = studentMapper.selectOne(new LambdaQueryWrapper<StuStudent>().eq(StuStudent::getUserId, loginId));
            if (student == null || student.getCurrentBedId() == null) {
                log.warn("[数据权限] 学生 (ID: {}) 无档案或无床位，返回空数据。", loginId);
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            
            // 1.2 查找床位对应的房间
            DormBed bed = bedMapper.selectById(student.getCurrentBedId());
            if (bed == null || bed.getRoomId() == null) {
                log.warn("[数据权限] 学生 (ID: {}) 床位信息异常，返回空数据。", loginId);
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            
            // 1.3 强制查询条件：room_id 必须等于学生所在的房间ID
            wrapper.eq(BizBillingRecord::getRoomId, bed.getRoomId());
            
        } else if (StpUtil.hasRole("counselor")) {
            // 2. 如果是辅导员，查询自己所管班级的学生所在的所有房间ID
            log.debug("[数据权限] 检测到辅导员 (ID: {}) 查询账单，开始过滤所管班级房间数据。", loginId);
            
            // 2.1 查找辅导员管理的所有班级
            List<Long> classIds = classMapper.selectList(new LambdaQueryWrapper<BizClass>().eq(BizClass::getCounselorUserId, loginId))
                    .stream().map(BizClass::getClassId).collect(Collectors.toList());
            
            if (CollUtil.isEmpty(classIds)) {
                log.warn("[数据权限] 辅导员 (ID: {}) 未关联任何班级，返回空数据。", loginId);
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            
            // 2.2 查找这些班级下的所有学生
            List<Long> bedIds = studentMapper.selectList(new LambdaQueryWrapper<StuStudent>().in(StuStudent::getClassId, classIds))
                    .stream().map(StuStudent::getCurrentBedId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            
            if (CollUtil.isEmpty(bedIds)) {
                log.warn("[数据权限] 辅导员 (ID: {}) 所管学生均未住宿，返回空数据。", loginId);
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            
            // 2.3 查找这些床位对应的所有房间ID
            List<Long> roomIds = bedMapper.selectBatchIds(bedIds)
                    .stream().map(DormBed::getRoomId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            
            if (CollUtil.isEmpty(roomIds)) {
                log.warn("[数据权限] 辅导员 (ID: {}) 所管学生床位信息异常，返回空数据。", loginId);
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
            }
            
            // 2.4 强制查询条件：room_id 必须在这些房间ID中
            wrapper.in(BizBillingRecord::getRoomId, roomIds);
            
        } else {
            // 3. 如果是 Admin、宿管(dorm_manager)、财务(finance)
            // 不添加基于角色的过滤，但允许 DTO 传入 roomId 进行查询
            log.debug("[数据权限] 检测到管理员 (ID: {})，允许查询所有数据。", loginId);
            wrapper.eq(queryDTO.getRoomId() != null, BizBillingRecord::getRoomId, queryDTO.getRoomId());
        }
        // =========================================================
        
        
        wrapper.orderByDesc(BizBillingRecord::getCycleEndDate);
        
        Page<BizBillingRecord> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        
        // 2. 填充关联信息 (房间号、楼栋名)
        if (!page.getRecords().isEmpty()) {
            
            List<Long> roomIds = page.getRecords().stream().map(BizBillingRecord::getRoomId).distinct().collect(Collectors.toList());
            
            // 批量查询房间和楼栋信息
            Map<Long, DormRoom> roomMap = roomIds.isEmpty() ? Collections.emptyMap() :
                    roomMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(DormRoom::getRoomId, r -> r));
            
            // 【修正】修复NPE，确保 roomMap.values() 不为空
            List<Long> buildingIds = roomMap.values().stream()
                    .map(DormRoom::getBuildingId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            Map<Long, String> buildingNameMap = buildingIds.isEmpty() ? Collections.emptyMap() :
                    buildingMapper.selectBatchIds(buildingIds).stream().collect(Collectors.toMap(DormBuilding::getBuildingId, DormBuilding::getBuildingName));
            
            // 填充数据
            page.getRecords().forEach(record -> {
                DormRoom room = roomMap.get(record.getRoomId());
                if (room != null) {
                    record.setRoomNumber(room.getRoomNumber());
                    record.setBuildingName(buildingNameMap.get(room.getBuildingId()));
                }
            });
        }
        return page;
    }
    
    /**
     * 处理账单支付逻辑 (学生/财务 操作)
     * (此方法保持不变)
     */
    @Override
    @Transactional
    public void processPayment(BillingPaymentDTO paymentDTO) {
        BizBillingRecord record = this.getById(paymentDTO.getRecordId());
        
        if (record == null) {
            throw new BusinessException("账单记录不存在");
        }
        if (record.getIsPaid() == 1 || "2".equals(record.getStatus())) {
            throw new BusinessException("该账单已支付，请勿重复操作");
        }
        if (record.getTotalAmount() == null || record.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("账单金额无效，无法支付");
        }
        
        // 核心校验：核对支付金额
        if (record.getTotalAmount().compareTo(paymentDTO.getPaidAmount()) != 0) {
            throw new BusinessException("支付金额不匹配，请核对后重试");
        }
        
        // 更新状态
        record.setIsPaid(1);
        record.setStatus("2"); // 状态：已支付
        record.setPaidTime(LocalDateTime.now());
        
        this.updateById(record);
    }
}