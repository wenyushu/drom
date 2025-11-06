package com.dormitory.service;

/**
 * 学生生命周期定时任务服务接口
 */
public interface IStudentLifecycleService {
    
    /**
     * 【定时任务】
     * 自动检查并处理所有超期的学生（毕业/休学）
     * 1. 将达到毕业日期的学生设为 “毕业”。
     * 2. 将休学超过 3 年的学生设为 “退学”。
     * 3. 自动迁出床位并禁用账户。
     */
    void checkAndProcessOverdueStudents();
    
}