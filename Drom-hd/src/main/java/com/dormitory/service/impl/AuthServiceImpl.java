package com.dormitory.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dormitory.entity.SysUser;
import com.dormitory.exception.BusinessException;
import com.dormitory.mapper.SysUserMapper;
import com.dormitory.service.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 认证服务的实现、密码加密等
 */
@Service
public class AuthServiceImpl implements IAuthService {
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired // 注入加密器
    private PasswordEncoder passwordEncoder;
    
    
    @Override
    public String login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException("用户名或密码不能为空"); // 抛出自定义异常
        }
        
        // 1. 查询用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = userMapper.selectOne(wrapper);

        // 2. 校验用户是否存在
        if (user == null) {
            throw new BusinessException("登录失败：用户不存在或密码错误");
        }
        
        
//        // 3. 校验密码，测试用(未加密)
//        if (!password.equals(user.getPassword())) {
//            throw new BusinessException("登录失败：用户不存在或密码错误"); // 保持统一错误提示
//        }
        
        // 3. 【密码加密】：使用 BCrypt 的 matches 方法校验密码
        // 参数 1：用户输入的明文密码
        // 参数 2：数据库中存储的加密哈希值
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("登录失败：用户不存在或密码错误");
        }
        
        
        // 4. 校验用户状态
        if (user.getStatus() == 1) {
            throw new BusinessException("登录失败：该账号已被禁用");
        }
        
        // 5. 使用 Sa-Token 登录，并绑定账号 ID
        // StpUtil.login() 会生成 Token 并保存 Session 到 Redis
        StpUtil.login(user.getUserId());
        
        // 6. 返回 Token
        return StpUtil.getTokenValue();
    }
    
    // TODO: 实现登出接口
}