package com.dormitory.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 * 为了让 BaseEntity 中的 createTime 和 updateTime 字段能够自动填充，我们需要实现 MyBatis-Plus 的 MetaObjectHandler 接口。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    // 假设我们通过某种方式获取当前操作用户ID或名称，这里先用 SYSTEM 代替
    private static final String CURRENT_USER = "SYSTEM";
    
    @Override
    public void insertFill(MetaObject metaObject) {
        // 插入时，填充创建时间和创建者
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createBy", String.class, CURRENT_USER);
        
        // 虽然 updateTime 应该在 update 时填充，但有些场景下 insert 也需要初始化
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateBy", String.class, CURRENT_USER);
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时，填充更新时间和更新者
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updateBy", String.class, CURRENT_USER);
    }
}