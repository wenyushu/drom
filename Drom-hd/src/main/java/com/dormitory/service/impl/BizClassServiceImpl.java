package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.ClassQueryDTO;
import com.dormitory.entity.BizClass;
import com.dormitory.entity.StuStudent;
import com.dormitory.entity.SysDepartment;
import com.dormitory.entity.SysUser;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.BizClassMapper;
import com.dormitory.mapper.StuStudentMapper; // 导入学生 Mapper
import com.dormitory.mapper.SysDepartmentMapper;
import com.dormitory.mapper.SysUserMapper;
import com.dormitory.service.IBizClassService;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BizClassServiceImpl extends ServiceImpl<BizClassMapper, BizClass> implements IBizClassService {
    
    @Autowired private SysDepartmentMapper departmentMapper;
    @Autowired private SysUserMapper userMapper;
    @Autowired private StuStudentMapper studentMapper; // 用于删除校验
    
    @Override
    public Page<BizClass> selectClassPage(ClassQueryDTO queryDTO) {
        LambdaQueryWrapper<BizClass> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotEmpty(queryDTO.getClassName()), BizClass::getClassName, queryDTO.getClassName())
                .eq(queryDTO.getDepartmentId() != null, BizClass::getDepartmentId, queryDTO.getDepartmentId())
                .like(StrUtil.isNotEmpty(queryDTO.getMajorName()), BizClass::getMajorName, queryDTO.getMajorName())
                .eq(queryDTO.getCounselorUserId() != null, BizClass::getCounselorUserId, queryDTO.getCounselorUserId())
                .eq(StrUtil.isNotEmpty(queryDTO.getEnrollmentYear()), BizClass::getEnrollmentYear, queryDTO.getEnrollmentYear());
        
        Page<BizClass> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        
        // 填充关联信息
        if (!page.getRecords().isEmpty()) {
            List<BizClass> records = page.getRecords();
            List<Long> deptIds = records.stream().map(BizClass::getDepartmentId).distinct().collect(Collectors.toList());
            List<Long> counselorIds = records.stream().map(BizClass::getCounselorUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            
            Map<Long, String> deptMap = deptIds.isEmpty() ? Collections.emptyMap() :
                    departmentMapper.selectBatchIds(deptIds).stream().collect(Collectors.toMap(SysDepartment::getDeptId, SysDepartment::getDeptName));
            Map<Long, String> counselorMap = counselorIds.isEmpty() ? Collections.emptyMap() :
                    userMapper.selectBatchIds(counselorIds).stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getNickname));
            
            records.forEach(c -> {
                c.setDepartmentName(deptMap.get(c.getDepartmentId()));
                c.setCounselorName(counselorMap.get(c.getCounselorUserId()));
            });
        }
        return page;
    }
    
    @Override
    public void addClass(BizClass bizClass) {
        // TODO: 校验 院系ID 和 辅导员ID 是否存在
        this.save(bizClass);
    }
    
    @Override
    public void updateClass(BizClass bizClass) {
        if (bizClass.getClassId() == null) {
            throw new BusinessException("班级ID不能为空");
        }
        this.updateById(bizClass);
    }
    
    @Override
    public void deleteClassById(Long classId) {
        // 校验：如果班级下还有学生，不允许删除
        if (studentMapper.exists(new LambdaQueryWrapper<StuStudent>().eq(StuStudent::getClassId, classId))) {
            throw new BusinessException("删除失败：该班级下仍有关联学生！");
        }
        this.removeById(classId);
    }
}