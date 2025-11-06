package com.dormitory.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.entity.BizBillingRate;
import com.dormitory.mapper.BizBillingRateMapper;
import com.dormitory.service.IBizBillingRateService;
import org.springframework.stereotype.Service;

/**
 * 计费费率业务服务实现类
 * 基础 CRUD 由 ServiceImpl 自动提供
 */
@Service // ❗ 必须要有此注解，Spring 才能扫描并创建 Bean ❗
public class BizBillingRateServiceImpl
        extends ServiceImpl<BizBillingRateMapper, BizBillingRate>
        implements IBizBillingRateService {
    
    // 基础 CRUD 由 ServiceImpl 继承实现
    // 可在此添加费率唯一性、生效日期的业务校验逻辑
}