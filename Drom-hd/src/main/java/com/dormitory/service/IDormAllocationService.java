package com.dormitory.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.dto.RoomChangeApprovalDTO;
import com.dormitory.dto.RoomChangeQueryDTO;
import com.dormitory.dto.RoomChangeRequestDTO;
import com.dormitory.entity.DormAllocationLog;
import com.dormitory.vo.RoomChangeRequestVO;

import java.util.List;
import java.util.Map;

/**
 * 宿舍分配与调宿核心业务服务接口
 */
public interface IDormAllocationService {
    
    /**
     * 为指定学生列表执行宿舍分配 (核心方法)
     */
    Map<Long, String> allocateRoomsForStudents(List<Long> studentIds);
    
    /**
     * 学生提交调宿申请
     */
    void submitRoomChangeRequest(RoomChangeRequestDTO dto, Long loginId);
    
    /**
     * 管理员审批调宿申请 (批准或驳回)
     */
    void approveRoomChangeRequest(RoomChangeApprovalDTO dto, Long adminUserId);
    
    /**
     * 【【【【【 2. 修改参数 】】】】】
     * 管理员分页查询所有调宿申请
     */
    Page<RoomChangeRequestVO> selectRoomChangeRequestPage(RoomChangeQueryDTO queryDTO);
    
    /**
     * 学生查询自己的调宿申请
     */
    List<RoomChangeRequestVO> getMyRoomChangeRequests(Long loginId);
    
    
    /**
     * 手动将学生分配到指定床位 (管理员操作)
     */
    DormAllocationLog assignStudentToBed(Long studentId, Long targetBedId);
    
    
    /**
     * 将学生从当前床位迁出 (退宿/毕业/休学/调宿前置操作)
     */
    void checkoutStudentFromBed(Long studentId, String actionType, String reason);
}