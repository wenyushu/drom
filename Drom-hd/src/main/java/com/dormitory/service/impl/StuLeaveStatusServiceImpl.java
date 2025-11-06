package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.LeaveStatusQueryDTO;
import com.dormitory.entity.StuLeaveStatus;
import com.dormitory.entity.StuStudent; // 导入学生实体
import com.dormitory.entity.SysUser; // 导入用户实体
import com.dormitory.exception.BusinessException; // 导入异常类
import com.dormitory.mapper.StuLeaveStatusMapper;
import com.dormitory.mapper.StuStudentMapper; // 导入学生 Mapper
import com.dormitory.mapper.SysUserMapper; // 导入用户 Mapper
import com.dormitory.service.IStuLeaveStatusService;
import cn.hutool.core.collection.CollUtil; // 导入 CollUtil
import cn.hutool.core.util.StrUtil; // 导入 StrUtil
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 导入事务注解

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects; // 导入 Objects
import java.util.stream.Collectors;

/**
 * 学生离校/留校状态业务服务实现类
 */
@Service
public class StuLeaveStatusServiceImpl extends ServiceImpl<StuLeaveStatusMapper, StuLeaveStatus> implements IStuLeaveStatusService {
    
    @Autowired private StuStudentMapper studentMapper;
    @Autowired private SysUserMapper userMapper; // 注入用户 Mapper 用于填充姓名
    
    @Override
    public Page<StuLeaveStatus> selectStatusPage(LeaveStatusQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<StuLeaveStatus> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getStudentId() != null, StuLeaveStatus::getStudentId, queryDTO.getStudentId())
                .eq(queryDTO.getStatusType() != null, StuLeaveStatus::getStatusType, queryDTO.getStatusType())
                .eq(queryDTO.getIsInDorm() != null, StuLeaveStatus::getIsInDorm, queryDTO.getIsInDorm())
                // 日期范围
                .ge(queryDTO.getStartDate() != null, StuLeaveStatus::getStartDate, queryDTO.getStartDate().atStartOfDay())
                .le(queryDTO.getEndDate() != null, StuLeaveStatus::getStartDate, queryDTO.getEndDate().plusDays(1).atStartOfDay())
                .orderByDesc(StuLeaveStatus::getStartDate);
        
        // 处理学号查询
        if (StrUtil.isNotEmpty(queryDTO.getStuNumber())) {
            StuStudent student = studentMapper.selectOne(new LambdaQueryWrapper<StuStudent>().eq(StuStudent::getStuNumber, queryDTO.getStuNumber()));
            if (student == null) {
                return new Page<>(queryDTO.getCurrent(), queryDTO.getSize()); // 学号不存在则返回空
            }
            wrapper.eq(StuLeaveStatus::getStudentId, student.getStudentId());
        }
        
        Page<StuLeaveStatus> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        
        // 2. 填充学生姓名和学号
        if (!page.getRecords().isEmpty()) {
            List<Long> studentIds = page.getRecords().stream().map(StuLeaveStatus::getStudentId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            // 批量查询学生基础信息 (需要 userId 来查姓名)
            Map<Long, StuStudent> studentMap = studentIds.isEmpty() ? Collections.emptyMap() :
                    studentMapper.selectBatchIds(studentIds).stream().collect(Collectors.toMap(StuStudent::getStudentId, s -> s));
            
            // 获取关联的 userId 列表
            List<Long> userIds = studentMap.values().stream().map(StuStudent::getUserId).distinct().collect(Collectors.toList());
            // 批量查询用户信息
            Map<Long, String> userNicknameMap = userIds.isEmpty() ? Collections.emptyMap() :
                    userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickname));
            
            page.getRecords().forEach(status -> {
                StuStudent student = studentMap.get(status.getStudentId());
                if (student != null) {
                    status.setStuNumber(student.getStuNumber());
                    // 填充学生姓名 (从 User Map 获取)
                    status.setStudentName(userNicknameMap.get(student.getUserId()));
                }
            });
        }
        return page;
    }
    
    @Override
    @Transactional // 确保原子性
    public void saveOrUpdateStatus(StuLeaveStatus status) {
        // 1. 校验 studentId 是否存在
        StuStudent student = studentMapper.selectById(status.getStudentId());
        if (student == null) {
            throw new BusinessException("更新失败：关联的学生不存在。");
        }
        
        // 2. 校验 statusType 是否有效 (0-4) - 可选
        List<String> validStatusTypes = List.of("0", "1", "2", "3", "4");
        if (!validStatusTypes.contains(status.getStatusType())) {
            throw new BusinessException("无效的状态类型。");
        }
        
        // 3. 业务联动：如果状态是离校 (1, 2, 4)，则 isInDorm 应设为 0
        if ("1".equals(status.getStatusType()) || "2".equals(status.getStatusType()) || "4".equals(status.getStatusType())) {
            status.setIsInDorm(0);
        } else { // 在校或留校
            status.setIsInDorm(1);
        }
        
        // 4. 使用 saveOrUpdate (基于 student_id 查询或唯一约束)
        // 尝试根据 student_id 查询现有记录
        StuLeaveStatus existing = this.getOne(new LambdaQueryWrapper<StuLeaveStatus>().eq(StuLeaveStatus::getStudentId, status.getStudentId()));
        if (existing != null) {
            // 如果存在，设置 ID 以执行更新
            status.setId(existing.getId());
        }
        // saveOrUpdate 会自动判断是插入还是更新
        boolean success = this.saveOrUpdate(status);
        if (!success) {
            throw new BusinessException("学生状态更新失败");
        }
        
        // 5. 业务联动：如果学生离校/毕业，是否清空床位？ (根据业务需求决定)
        // if (status.getIsInDorm() == 0 && student.getCurrentBedId() != null) {
        //     // 调用 BedService 清空床位占用信息
        // }
    }
}