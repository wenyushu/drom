package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.dto.ElectricAlertQueryDTO;
import com.dormitory.dto.AlertResolveDTO; // 导入 DTO
import com.dormitory.entity.BizElectricAlert;
import com.dormitory.entity.DormRoom;
import com.dormitory.exception.BusinessException; // 导入异常类
import com.dormitory.mapper.BizElectricAlertMapper;
import com.dormitory.mapper.DormRoomMapper;
import com.dormitory.service.IBizElectricAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 导入事务注解

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects; // 导入 Objects
import java.util.stream.Collectors;

/**
 * 违规用电告警业务服务实现类
 */
@Service
public class BizElectricAlertServiceImpl extends ServiceImpl<BizElectricAlertMapper, BizElectricAlert> implements IBizElectricAlertService {
    
    @Autowired private DormRoomMapper roomMapper;
    
    @Override
    public Page<BizElectricAlert> selectAlertPage(ElectricAlertQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<BizElectricAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getRoomId() != null, BizElectricAlert::getRoomId, queryDTO.getRoomId())
                .eq(queryDTO.getAlertType() != null, BizElectricAlert::getAlertType, queryDTO.getAlertType())
                .eq(queryDTO.getIsResolved() != null, BizElectricAlert::getIsResolved, queryDTO.getIsResolved())
                // 日期范围
                .ge(queryDTO.getStartDate() != null, BizElectricAlert::getAlertTime, queryDTO.getStartDate().atStartOfDay())
                .le(queryDTO.getEndDate() != null, BizElectricAlert::getAlertTime, queryDTO.getEndDate().plusDays(1).atStartOfDay())
                .orderByDesc(BizElectricAlert::getAlertTime);
        
        Page<BizElectricAlert> page = this.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), wrapper);
        
        // 2. 填充房间号
        if (!page.getRecords().isEmpty()) {
            List<Long> roomIds = page.getRecords().stream().map(BizElectricAlert::getRoomId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
            final Map<Long, String> roomNumberMap = roomIds.isEmpty() ? Collections.emptyMap() :
                    roomMapper.selectBatchIds(roomIds).stream().collect(Collectors.toMap(DormRoom::getRoomId, DormRoom::getRoomNumber));
            page.getRecords().forEach(alert -> alert.setRoomNumber(roomNumberMap.get(alert.getRoomId())));
        }
        
        return page;
    }
    
    @Override
    public void addAlert(BizElectricAlert alert) {
        // 校验 roomId 是否存在
        if (!roomMapper.exists(new LambdaQueryWrapper<DormRoom>().eq(DormRoom::getRoomId, alert.getRoomId()))) {
            throw new BusinessException("新增告警失败：关联的房间不存在。");
        }
        // 可以在这里添加告警类型的校验逻辑
        this.save(alert);
    }
    
    @Override
    @Transactional // 确保更新原子性
    public void resolveAlert(AlertResolveDTO resolveDTO) { // 使用 DTO
        BizElectricAlert alert = this.getById(resolveDTO.getAlertId());
        if (alert == null) {
            throw new BusinessException("告警记录不存在");
        }
        if (alert.getIsResolved() != null && alert.getIsResolved() == 1) { // 检查是否已处理
            throw new BusinessException("该告警已被处理，请勿重复操作");
        }
        alert.setIsResolved(1); // 标记为已处理
        // 如果需要记录备注，需要在 BizElectricAlert 实体和表中添加 resolve_remark 字段
        // alert.setResolveRemark(resolveDTO.getResolveRemark());
        this.updateById(alert);
    }
}