package com.dormitory.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotNull; // 确保导入
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime; // 导入 LocalDateTime

/**
 * 学生住宿偏好实体类
 */
@EqualsAndHashCode(callSuper = true) // 继承 BaseEntity
@Data
@TableName("biz_user_preference")
public class UserPreference extends BaseEntity { // 继承 BaseEntity
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 用户 ID (主键, 关联 user_id ) */
    @TableId(value = "user_id") // 主键不是自增
    @NotNull(message = "用户 ID 不能为空")
    private Long userId;
    
    /** 是否吸烟 (0: 否, 1: 是) */
    private Integer isSmoker;
    
    /** 是否饮酒 (0: 否, 1: 是) */
    private Integer isDrinker;
    
    /** 起床习惯 (0: 早起, 1: 随性, 2: 晚起) */
    private String wakeType;
    
    /** 作息习惯 (0: 早睡, 1: 晚睡, 2: 熬夜) */
    private String sleepType;
    
    /** 是否浅眠 */
    private Integer isLightSleeper;
    
    /** 是否有夜间学习/工作习惯 */
    private Integer studyAtNight;
    
    /** 手游频率 (0: 不玩, 1: 偶尔, 2: 经常) */
    private String mobileGameFreq;
    
    /** 对桌游的兴趣 */
    private Integer boardGameInterest;
    
    /** 卫生整洁度 (0: 邋遢, 1: 一般, 2: 整洁) */
    private String cleanlinessLevel;
    
    /** 空调/温度偏好 (0: 适中, 1: 喜欢冷, 2: 喜欢热) */
    private String airConditionPref;
    
    /** 噪音容忍度 (0: 低, 1: 一般, 2: 高) */
    private String noiseTolerance;
    
    /** 爱好标签 (逗号分隔) */
    private String hobbyTags;
    
    /** 组队分配码 */
    private String groupCode;
    
    /** 访客频率 */
    private String guestFrequency;
    
    /** 学习地点偏好 */
    private String studyLocationPref;
    
    /** 室内噪音水平 */
    private String inRoomNoiseLevel;
    
    /** 气味敏感度 */
    private String smellSensitivity;
    
    /** 最后更新时间 (使用 BaseEntity 中的 updateTime) */
    private LocalDateTime lastUpdated; // 已包含在 BaseEntity 中
}