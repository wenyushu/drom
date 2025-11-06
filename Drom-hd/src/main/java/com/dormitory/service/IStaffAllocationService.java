package com.dormitory.service;

import java.util.List;
import java.util.Map;

/**
 * 教职工/后勤宿舍分配业务服务接口
 */
public interface IStaffAllocationService {
    
    /**
     * 批量为教职工分配宿舍 (自动)
     * @param staffUserIds 需要分配的 SysUser ID 列表
     * @return 分配结果
     */
    Map<Long, String> allocateRoomsForStaff(List<Long> staffUserIds);
    
    
    /**
     * 手动将教职工分配到指定床位 (Admin 操作)
     * @param staffUserId 教职工的 SysUser ID
     * @param targetBedId 目标床位ID
     */
    void assignStaffToBed(Long staffUserId, Long targetBedId);
    
    
    /**
     * 将教职工从当前床位迁出 (离职/搬离)
     * @param staffUserId 教职工的 SysUser ID
     * @param reason 迁出原因
     */
    void checkoutStaffFromBed(Long staffUserId, String reason);
}