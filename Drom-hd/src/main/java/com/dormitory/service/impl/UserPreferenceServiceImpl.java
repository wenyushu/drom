package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.entity.UserPreference;
import com.dormitory.entity.SysUser; // 导入 SysUser 用于校验
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.UserPreferenceMapper;
import com.dormitory.mapper.SysUserMapper; // 导入 SysUser Mapper
import com.dormitory.service.IUserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 通用用户住宿偏好业务服务实现类
 */
@Service
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreference> implements IUserPreferenceService {
    
    @Autowired
    private SysUserMapper userMapper; // 注入 SysUser Mapper 用于校验
    
    @Override
    public UserPreference getMyPreference(Long loginId) {
        // 主键就是 loginId (SysUser ID)
        UserPreference preference = this.getById(loginId);
        if (preference == null) {
            // 如果不存在，返回一个包含 userId 的空默认对象
            preference = new UserPreference();
            preference.setUserId(loginId);
        }
        return preference;
    }
    
    @Override
    public void saveMyPreference(UserPreference preference, Long loginId) {
        // 1. 核心安全校验：确保用户只能修改自己的偏好
        if (preference.getUserId() == null || !preference.getUserId().equals(loginId)) {
            throw new BusinessException("权限不足：您只能修改自己的住宿偏好。");
        }
        // 2. 使用 saveOrUpdate (基于主键 user_id)
        boolean success = this.saveOrUpdate(preference);
        if (!success) {
            throw new BusinessException("偏好保存失败，请重试");
        }
    }
    
    @Override
    public UserPreference getPreferenceByUserId(Long userId) {
        UserPreference preference = this.getById(userId);
        if (preference == null) {
            preference = new UserPreference();
            preference.setUserId(userId);
        }
        return preference;
    }
    
    @Override
    public void savePreferenceByAdmin(UserPreference preference) {
        if (preference.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        // 1. 校验 SysUser 表中是否存在该 UserId
        if (!userMapper.exists(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserId, preference.getUserId()))) {
            throw new BusinessException("保存失败：指定的用户ID不存在");
        }
        
        // 2. 执行保存或更新
        boolean success = this.saveOrUpdate(preference);
        if (!success) {
            throw new BusinessException("管理员保存偏好失败");
        }
    }
}