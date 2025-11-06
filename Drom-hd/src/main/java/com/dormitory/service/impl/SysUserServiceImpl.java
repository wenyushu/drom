package com.dormitory.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.UserAddUpdateDTO;
import com.dormitory.dto.UserQueryDTO;
import com.dormitory.entity.BizStaffInfo; // 【【【【【 1. 新增导入 】】】】】
import com.dormitory.entity.StuStudent; // 【【【【【 1. 新增导入 】】】】】
import com.dormitory.entity.SysRole;
import com.dormitory.entity.SysUser;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.BizStaffInfoMapper; // 【【【【【 1. 新增导入 】】】】】
import com.dormitory.mapper.StuStudentMapper; // 【【【【【 1. 新增导入 】】】】】
import com.dormitory.mapper.SysRoleMapper;
import com.dormitory.mapper.SysUserMapper;
import com.dormitory.service.ISysUserService;
import com.dormitory.vo.UserVO;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户业务服务实现类
 * 修复档案分离，增加级联操作
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // 注入档案 Mappers
    @Autowired
    private StuStudentMapper studentMapper;
    
    @Autowired
    private BizStaffInfoMapper staffInfoMapper;
    
    
    /**
     * 分页查询用户列表 (返回 UserVO)
     * (此方法保持不变)
     */
    @Override
    public Page<UserVO> selectUserPage(UserQueryDTO queryDTO) {
        
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotEmpty(queryDTO.getUsername()), SysUser::getUsername, queryDTO.getUsername())
                .like(StrUtil.isNotEmpty(queryDTO.getNickname()), SysUser::getNickname, queryDTO.getNickname())
                .eq(queryDTO.getRoleId() != null, SysUser::getRoleId, queryDTO.getRoleId())
                .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
                .orderByAsc(SysUser::getUserId);
        
        Page<SysUser> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        Page<UserVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        
        if (CollUtil.isEmpty(page.getRecords())) {
            return voPage;
        }
        
        List<SysUser> userList = page.getRecords();
        List<Long> roleIds = userList.stream().map(SysUser::getRoleId).distinct().collect(Collectors.toList());
        Map<Long, String> roleMap = roleIds.isEmpty() ? Collections.emptyMap() :
                roleMapper.selectBatchIds(roleIds).stream()
                        .collect(Collectors.toMap(SysRole::getRoleId, SysRole::getRoleName));
        
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtil.copyProperties(user, vo);
            vo.setRoleName(roleMap.get(user.getRoleId()));
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(userVOList);
        return voPage;
    }
    
    
    /**
     * 新增用户
     * 增加级联新增档案
     */
    @Override
    @Transactional
    public void addUser(UserAddUpdateDTO addDTO) {
        
        // 1. 校验用户名唯一性
        if (checkUsernameUnique(addDTO.getUsername(), null)) {
            throw new BusinessException("新增用户失败，登录账号 " + addDTO.getUsername() + " 已存在");
        }
        
        // 2. 校验角色ID是否存在
        if (!roleMapper.exists(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleId, addDTO.getRoleId()))) {
            throw new BusinessException("新增用户失败，指定的角色不存在");
        }
        
        // 3. 密码校验与加密
        if (StrUtil.isBlank(addDTO.getPassword())) {
            throw new BusinessException("新增用户时，密码不能为空");
        }
        
        // 4. 转换 DTO 为 SysUser 实体
        SysUser user = new SysUser();
        // 复制基础字段 (username, password, roleId, nickname, userType, sex, phone, email, dateOfBirth, hometown, etc.)
        BeanUtil.copyProperties(addDTO, user);
        // 使用 DTO 中新增的 realName 字段
        user.setRealName(addDTO.getRealName());
        
        // 加密密码
        String encodedPassword = passwordEncoder.encode(addDTO.getPassword());
        user.setPassword(encodedPassword);
        
        // 5. 保存 SysUser (保存后 user.getUserId() 会被自动填充)
        this.save(user);
        
        // 6. 核心修改：业务联动，自动创建档案
        try {
            if ("1".equals(user.getUserType())) {
                // 6.1 创建学生档案
                // 校验必填项
                if (addDTO.getCurrentCampusId() == null || addDTO.getEnterDate() == null) {
                    throw new BusinessException("新增学生失败：必须提供'当前校区ID (currentCampusId)'和'入学日期 (enterDate)'");
                }
                
                StuStudent student = new StuStudent();
                BeanUtil.copyProperties(addDTO, student); // 复制 DTO 中所有匹配的字段
                student.setUserId(user.getUserId()); // 关联 SysUser ID
                
                // 设置关键的默认值
                student.setAcademicStatus("0"); // 默认 "0" (正常在校)
                student.setIsOnCampusResident(1); // 默认 "1" (校内)
                
                studentMapper.insert(student);
                
            } else if (!"0".equals(user.getUserType())) {
                // 假设 0=Admin (无档案), 1=Student, 其他 (2,3,4,5...) 都是教职工
                
                // 6.2 创建教职工档案
                // 校验必填项
                if (addDTO.getHireDate() == null) {
                    throw new BusinessException("新增教职工失败：必须提供'入职日期 (hireDate)'");
                }
                
                BizStaffInfo staffInfo = new BizStaffInfo();
                BeanUtil.copyProperties(addDTO, staffInfo); // 复制 DTO 中所有匹配的字段
                staffInfo.setUserId(user.getUserId()); // 关联 SysUser ID
                
                // 满足 @NotNull 约束 (hireDate 已从 DTO 获取)
                staffInfo.setIsOnCampusResident(1); // 默认愿意住校
                
                staffInfoMapper.insert(staffInfo);
            }
        } catch (Exception e) {
            // 如果档案创建失败，则回滚 SysUser 的创建
            throw new BusinessException("创建用户成功，但创建关联档案失败：" + e.getMessage());
        }
    }
    
    
    /**
     * 修改用户
     * 增加级联更新档案
     */
    @Override
    @Transactional
    public void updateUser(UserAddUpdateDTO updateDTO) {
        if (updateDTO.getUserId() == null) {
            throw new BusinessException("修改用户时，用户ID不能为空");
        }
        
        // 校验用户名唯一性 (排除当前用户)
        if (checkUsernameUnique(updateDTO.getUsername(), updateDTO.getUserId())) {
            throw new BusinessException("修改用户失败，登录账号已存在");
        }
        
        // 校验角色ID
        if (!roleMapper.exists(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleId, updateDTO.getRoleId()))) {
            throw new BusinessException("修改用户失败，指定的角色不存在");
        }
        
        // 1. 更新 SysUser 表
        SysUser userToUpdate = new SysUser();
        BeanUtil.copyProperties(updateDTO, userToUpdate);
        userToUpdate.setRealName(updateDTO.getRealName()); // 确保 RealName 被更新
        
        if (StrUtil.isNotEmpty(updateDTO.getPassword())) {
            userToUpdate.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        } else {
            userToUpdate.setPassword(null); // 确保不更新密码字段
        }
        
        this.updateById(userToUpdate);
        
        // 2. 级联更新关联档案
        // (只更新共有的基础信息，如姓名、性别、电话、生日等)
        try {
            if ("1".equals(updateDTO.getUserType())) {
                // 2.1 更新学生档案
                StuStudent student = new StuStudent();
                BeanUtil.copyProperties(updateDTO, student);
                student.setStudentId(null); // 防止 studentId (如果有) 干扰更新
                student.setUserId(updateDTO.getUserId()); // 确保 UserId 是更新条件
                
                // 使用 LambdaUpdateWrapper 根据 UserId 更新
                studentMapper.update(student, new LambdaQueryWrapper<StuStudent>()
                        .eq(StuStudent::getUserId, updateDTO.getUserId()));
                
            } else if (!"0".equals(updateDTO.getUserType())) {
                // 2.2 更新教职工档案
                BizStaffInfo staffInfo = new BizStaffInfo();
                BeanUtil.copyProperties(updateDTO, staffInfo);
                staffInfo.setUserId(updateDTO.getUserId()); // 确保 UserId 是更新条件
                
                staffInfoMapper.update(staffInfo, new LambdaQueryWrapper<BizStaffInfo>()
                        .eq(BizStaffInfo::getUserId, updateDTO.getUserId()));
            }
        } catch (Exception e) {
            throw new BusinessException("更新用户信息成功，但同步更新档案失败：" + e.getMessage());
        }
        
        // 3. TODO: 如果修改了 userType，(例如学生转职工)，逻辑会非常复杂
        // (V2.0 暂不实现，假设 UserType 不可变更)
    }
    
    
    /**
     * 批量删除用户
     * 增加级联删除档案
     */
    @Override
    @Transactional
    public void deleteUserByIds(Long[] userIds) {
        if (ArrayUtil.isEmpty(userIds)) {
            throw new BusinessException("删除用户时，用户ID列表不能为空");
        }
        
        List<Long> userIdList = Arrays.asList(userIds);
        
        // 1. 校验
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (userIdList.contains(currentUserId)) {
            throw new BusinessException("不能删除当前登录用户");
        }
        if (userIdList.contains(10001L)) {
            throw new BusinessException("不允许删除超级管理员账户");
        }
        
        // 2. 核心修改：级联逻辑删除关联档案
        // (注意：MyBatis-Plus 的 delete 默认是逻辑删除，如果档案表没有 @TableLogic，则会物理删除)
        // (假设 StuStudent 和 BizStaffInfo 也配置了逻辑删除)
        
        // 2.1 删除学生档案
        studentMapper.delete(new LambdaQueryWrapper<StuStudent>()
                .in(StuStudent::getUserId, userIdList));
        
        // 2.2 删除教职工档案
        staffInfoMapper.delete(new LambdaQueryWrapper<BizStaffInfo>()
                .in(BizStaffInfo::getUserId, userIdList));
        
        // 3. 执行逻辑删除 SysUser
        this.removeBatchByIds(userIdList);
    }
    
    
    /**
     * 校验用户名是否唯一
     * (此方法保持不变)
     */
    private boolean checkUsernameUnique(String username, Long userId) {
        if (StrUtil.isBlank(username)) return false;
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        if (userId != null) {
            wrapper.ne(SysUser::getUserId, userId);
        }
        return this.exists(wrapper);
    }
}