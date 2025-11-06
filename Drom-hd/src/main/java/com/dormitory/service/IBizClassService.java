package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.entity.BizClass;
import com.dormitory.dto.ClassQueryDTO; // 需要创建

/**
 * 班级管理业务服务接口
 */
public interface IBizClassService extends IService<BizClass> {
    Page<BizClass> selectClassPage(ClassQueryDTO queryDTO);
    void addClass(BizClass bizClass);
    void updateClass(BizClass bizClass);
    void deleteClassById(Long classId);
}