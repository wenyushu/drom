package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.entity.StuStudent;
import com.dormitory.entity.SysUser;
import com.dormitory.mapper.StuStudentMapper;
import com.dormitory.mapper.SysUserMapper;
import com.dormitory.service.IDormAllocationService;
import com.dormitory.service.IStudentLifecycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生生命周期定时任务服务实现类
 */
@Service
public class StudentLifecycleServiceImpl implements IStudentLifecycleService {
    
    private static final Logger log = LoggerFactory.getLogger(StudentLifecycleServiceImpl.class);
    
    @Autowired
    private StuStudentMapper studentMapper;
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private IDormAllocationService allocationService;
    
    // 您要求的休学超期年限
    private static final int LEAVE_OF_ABSENCE_OVERDUE_YEARS = 3;
    private static final String STATUS_IN_SCHOOL = "0";
    private static final String STATUS_ON_LEAVE = "1";
    private static final String STATUS_GRADUATED = "2";
    private static final String STATUS_WITHDRAWN = "3";
    private static final int USER_ACCOUNT_DISABLED = 1;
    
    /**
     * 【定时任务】每天凌晨4点执行
     * 自动检查并处理所有超期的学生（毕业/休学）
     */
    @Override
    @Scheduled(cron = "0 0 4 * * ?") // 每天凌晨4点执行
    @Transactional
    public void checkAndProcessOverdueStudents() {
        log.info("【定时任务】开始执行：检查学生生命周期状态...");
        
        LocalDate today = LocalDate.now();
        int processedCount = 0;
        
        // --- 1. 处理自动毕业/结业的学生 ---
        // 查找所有 “在校” 且 “毕业日期” 已到，或已过的学生
        List<StuStudent> studentsToGraduate = studentMapper.selectList(
                new LambdaQueryWrapper<StuStudent>()
                        .eq(StuStudent::getAcademicStatus, STATUS_IN_SCHOOL)
                        .isNotNull(StuStudent::getGraduationDate)
                        .le(StuStudent::getGraduationDate, today)
        );
        
        if (!studentsToGraduate.isEmpty()) {
            log.warn("【定时任务】发现 {} 名达到毕业日期的学生，开始处理...", studentsToGraduate.size());
            for (StuStudent student : studentsToGraduate) {
                try {
                    processStudentExit(student, STATUS_GRADUATED, "达到预计毕业日期，自动毕业");
                    processedCount++;
                } catch (Exception e) {
                    log.error("【定时任务】处理学生 (ID: {}) 自动毕业失败: {}", student.getStudentId(), e.getMessage());
                }
            }
        }
        
        // --- 2. 处理休学超期自动退学的学生 ---
        // 规则：休学状态（status=1）且最后更新时间在3年前
        LocalDateTime threeYearsAgo = LocalDateTime.now().minusYears(LEAVE_OF_ABSENCE_OVERDUE_YEARS);
        
        List<StuStudent> studentsToWithdraw = studentMapper.selectList(
                new LambdaQueryWrapper<StuStudent>()
                        .eq(StuStudent::getAcademicStatus, STATUS_ON_LEAVE)
                        // 我们使用 updateTime 作为判断休学开始时间的依据
                        .le(StuStudent::getUpdateTime, threeYearsAgo)
        );
        
        if (!studentsToWithdraw.isEmpty()) {
            log.warn("【定时任务】发现 {} 名休学超期（{}年）的学生，开始处理...", studentsToWithdraw.size(), LEAVE_OF_ABSENCE_OVERDUE_YEARS);
            for (StuStudent student : studentsToWithdraw) {
                try {
                    processStudentExit(student, STATUS_WITHDRAWN, "休学超期自动退学");
                    processedCount++;
                } catch (Exception e) {
                    log.error("【定时任务】处理学生 (ID: {}) 休学超期失败: {}", student.getStudentId(), e.getMessage());
                }
            }
        }
        
        log.info("【定时任务】执行完毕。共处理 {} 名学生。", processedCount);
    }
    
    /**
     * 内部辅助方法：统一处理学生离校（毕业/退学）的业务联动
     * @param student     学生档案
     * @param newStatus   新学籍状态 (2=毕业, 3=退学)
     * @param reason      原因
     */
    private void processStudentExit(StuStudent student, String newStatus, String reason) {
        log.info("【生命周期服务】正在处理 StudentID: {}，UserID: {}。 原因: {}",
                student.getStudentId(), student.getUserId(), reason);
        
        // 1. 更新学籍状态
        student.setAcademicStatus(newStatus);
        studentMapper.updateById(student);
        
        // 2. 自动迁出床位
        if (student.getCurrentBedId() != null) {
            log.info("... 正在迁出床位 ID: {}", student.getCurrentBedId());
            // 调用您已有的迁出服务
            allocationService.checkoutStudentFromBed(student.getStudentId(), newStatus, reason);
            // (checkoutStudentFromBed 已经处理了床位清空、房间人数-1、写日志)
            // (我们在这里需要确保 StuStudent 表的 currentBedId 也被清空)
            StuStudent studentToUpdate = new StuStudent();
            studentToUpdate.setStudentId(student.getStudentId());
            studentToUpdate.setCurrentBedId(null);
            studentMapper.updateById(studentToUpdate);
        }
        
        // 3. 禁用 SysUser 账户
        log.info("... 正在禁用 SysUser 账户 ID: {}", student.getUserId());
        SysUser userToDisable = new SysUser();
        userToDisable.setUserId(student.getUserId());
        userToDisable.setStatus(USER_ACCOUNT_DISABLED); // 1 = 禁用
        userMapper.updateById(userToDisable);
    }
}