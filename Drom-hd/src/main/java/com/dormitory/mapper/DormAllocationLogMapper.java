package com.dormitory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormitory.entity.DormAllocationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 宿舍分配/调动记录 Mapper 接口
 */
@Mapper
public interface DormAllocationLogMapper extends BaseMapper<DormAllocationLog> {
    
    /**
     * 将指定学生在指定床位上的所有【历史】入住/分配日志记录标记为非活动 (is_active = 0)
     * 用于确保一个学生在一个床位上只有一个当前的活动日志
     * @param studentId 学生ID
     * @param bedId 床位ID
     * @param excludeLogId 可选，排除当前正在创建的新日志ID，防止把自己标记为非活动
     */
    @Update("UPDATE dorm_allocation_log SET is_active = 0 WHERE student_id = #{studentId} AND bed_id = #{bedId} AND is_active = 1" +
            " AND (#{excludeLogId} IS NULL OR log_id != #{excludeLogId})")
    void deactivatePreviousLogs(@Param("studentId") Long studentId, @Param("bedId") Long bedId, @Param("excludeLogId") Long excludeLogId);
    
}