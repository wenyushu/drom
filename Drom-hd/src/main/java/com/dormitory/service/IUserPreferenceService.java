package com.dormitory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.entity.UserPreference; // 导入实体

/**
 * 通用用户住宿偏好业务服务接口
 */
public interface IUserPreferenceService extends IService<UserPreference> {
    
    /**
     * 获取【当前登录用户】的住宿偏好
     * @param loginId (从 StpUtil 获取的 SysUser ID)
     * @return 偏好设置
     */
    UserPreference getMyPreference(Long loginId);
    
    /**
     * 学生/教职工保存【自己】的住宿偏好 (含本人鉴权)
     * @param preference 偏好设置 (包含 userId)
     * @param loginId (用于鉴权，必须与 preference.userId 匹配)
     */
    void saveMyPreference(UserPreference preference, Long loginId);
    
    /**
     * 管理员获取【指定用户】的住宿偏好
     * @param userId 目标用户ID
     * @return 偏好设置
     */
    UserPreference getPreferenceByUserId(Long userId);
    
    /**
     * 管理员保存【指定用户】的住宿偏好
     * @param preference 偏好设置
     */
    void savePreferenceByAdmin(UserPreference preference);
}