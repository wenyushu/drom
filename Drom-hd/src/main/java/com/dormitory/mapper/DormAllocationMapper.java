package com.dormitory.mapper;

import com.dormitory.entity.DormBed; // 导入 DormBed
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 宿舍分配相关复杂查询 Mapper 接口
 *
 * 学生、教职工等分配
 */
@Mapper
public interface DormAllocationMapper {
    
    /**
     * 查找符合条件的可用空闲床位
     * @param campusId 校区ID
     * @param gender 学生性别 ('0' 或 '1')
     * @param limit 返回的最大床位数量 (可选)
     * @return 可用床位列表 (包含房间和楼栋的部分信息，或仅返回 DormBed)
     */
    List<DormBed> findAvailableBeds(@Param("campusId") Long campusId,
                                    @Param("gender") String gender,
                                    @Param("limit") Integer limit);
    
    
    /**
     * 查找符合条件的【教职工/后勤】可用空闲床位
     * @param campusId 校区ID
     * @param limit 返回的最大床位数量 (可选)
     * @return 可用床位列表
     */
    List<DormBed> findAvailableStaffBeds(@Param("campusId") Long campusId,
                                         @Param("limit") Integer limit);
    
    
    // 未来可能需要更复杂的查询，例如带有偏好权重的查询
}