package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.StudentQueryDTO;
import com.dormitory.dto.StudentUpdateDTO;
import com.dormitory.entity.StuStudent;
import com.dormitory.vo.StudentVO; // 导入 VO

/**
 * 学生详细信息业务服务接口
 */
public interface IStuStudentService extends IService<StuStudent> {
    
    /**
     * 分页查询学生列表 (复杂联查，返回 VO)
     */
    Page<StudentVO> selectStudentPage(StudentQueryDTO queryDTO);
    
    /**
     * 根据学生ID获取详细信息 (返回 VO)
     */
    StudentVO getStudentVoById(Long studentId);
    
    /**
     * 更新学生信息 (由管理员操作)
     */
    void updateStudentInfo(StudentUpdateDTO updateDTO);
    
    // 通常不提供直接删除学生记录的接口，而是通过学籍状态标记
}