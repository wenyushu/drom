package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dormitory.dto.UserAddUpdateDTO;
import com.dormitory.dto.UserQueryDTO;
import com.dormitory.entity.SysUser;
import com.dormitory.vo.UserVO;

/**
 * 用户业务服务接口
 */
public interface ISysUserService extends IService<SysUser> {
    
    /**
     * 分页查询用户列表
     */
    Page<UserVO> selectUserPage(UserQueryDTO queryDTO);
    
    /**
     * 新增用户
     */
    void addUser(UserAddUpdateDTO addDTO);
    
    /**
     * 修改用户
     */
    void updateUser(UserAddUpdateDTO updateDTO);
    
    /**
     * 批量删除用户 (逻辑删除)
     */
    void deleteUserByIds(Long[] userIds);
}