package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.entity.BizClass;
import com.dormitory.entity.BizStaffInfo;
import com.dormitory.entity.SysDepartment;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.BizClassMapper;
import com.dormitory.mapper.BizStaffInfoMapper;
import com.dormitory.mapper.SysDepartmentMapper;
import com.dormitory.service.ISysDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator; // 【修改】
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门/院系管理业务服务实现类
 */
@Service
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartment> implements ISysDepartmentService {
    
    @Autowired private BizClassMapper classMapper;
    @Autowired private BizStaffInfoMapper staffInfoMapper;
    
    /**
     * 删除逻辑
     */
    @Override
    @Transactional
    public void deleteDepartment(Long deptId) {

        if (classMapper.exists(new LambdaQueryWrapper<BizClass>().eq(BizClass::getDepartmentId, deptId))) {
            throw new BusinessException("删除失败：该部门/院系下仍有关联的班级！");
        }
        
        if (staffInfoMapper.exists(new LambdaQueryWrapper<BizStaffInfo>().eq(BizStaffInfo::getDepartmentId, deptId))) {
            throw new BusinessException("删除失败：该部门/院系下仍有关联的教职工！");
        }
        
        if (this.exists(new LambdaQueryWrapper<SysDepartment>().eq(SysDepartment::getParentId, deptId))) {
            throw new BusinessException("删除失败：该部门/院系下仍有子部门！");
        }
        
        this.removeById(deptId);
    }
    
    // =========================================================
    // 树形结构方法 (增加排序)
    // =========================================================
    
    /**
     * 查询部门列表，并构建树形结构 (已按 deptSort 排序)
     */
    @Override
    public List<SysDepartment> selectDeptTree() {
        // 1. 查询所有部门数据
        List<SysDepartment> deptList = this.list(
                new LambdaQueryWrapper<SysDepartment>()
                        .orderByAsc(SysDepartment::getParentId)
                        // 修改：增加排序字段
                        .orderByAsc(SysDepartment::getDeptSort)
        );
        
        // 2. 构建树形结构
        return buildDeptTree(deptList);
    }
    
    /**
     * 核心方法：递归构建部门树
     */
    private List<SysDepartment> buildDeptTree(List<SysDepartment> deptList) {
        // 1. 找出所有根节点 (ParentId = 0)
        List<SysDepartment> rootNodes = deptList.stream()
                .filter(dept -> dept.getParentId() == 0L)
                .collect(Collectors.toList());
        
        // 2. 递归设置子节点
        rootNodes.forEach(root -> root.setChildren(getChildren(root, deptList)));
        
        // 3. 修改：对根节点排序
        rootNodes.sort(Comparator.comparing(SysDepartment::getDeptSort, Comparator.nullsFirst(Comparator.naturalOrder())));
        
        return rootNodes;
    }
    
    /**
     * 递归获取子节点
     */
    private List<SysDepartment> getChildren(SysDepartment parent, List<SysDepartment> deptList) {
        List<SysDepartment> children = deptList.stream()
                .filter(dept -> parent.getDeptId().equals(dept.getParentId()))
                .peek(dept -> dept.setChildren(getChildren(dept, deptList)))
                // 修改：对子节点排序
                .sorted(Comparator.comparing(SysDepartment::getDeptSort, Comparator.nullsFirst(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        
        return children.isEmpty() ? null : children;
    }
}