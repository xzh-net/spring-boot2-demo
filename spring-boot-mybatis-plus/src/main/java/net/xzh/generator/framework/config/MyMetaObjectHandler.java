package net.xzh.generator.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "delFlag", Integer.class, 0);

        // ============ 操作人ID自动填充 ============
        // 获取当前登录用户ID，根据实际项目情况选择以下方式之一

        // 方式1：Spring Security 获取当前用户
        // Long userId = null;
        // try {
        //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //     if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
        //         // 假设 UserDetails 实现类中有 getId() 方法
        //         userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        //     }
        // } catch (Exception e) {
        //     // 非登录状态下可能为空，不做处理
        // }

        // 方式2：从 ThreadLocal 获取（适用于自定义用户上下文）
        // Long userId = UserContext.getUserId();

        // 方式3：从 HttpSession 获取（需要注入 HttpServletRequest）
        // Long userId = (Long) request.getSession().getAttribute("userId");

        // 如果获取到用户ID，则填充
        // if (userId != null) {
        //     this.strictInsertFill(metaObject, "createUserId", Long.class, userId);
        //     this.strictInsertFill(metaObject, "updateUserId", Long.class, userId);
        // }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());

        // ============ 更新操作人ID自动填充 ============
        // 获取当前登录用户ID，方式同 insertFill

        // Long userId = null;
        // try {
        //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //     if (authentication != null && !authentication.getPrincipal().equals("anonymousUser")) {
        //         userId = ((UserPrincipal) authentication.getPrincipal()).getId();
        //     }
        // } catch (Exception e) {
        // }

        // if (userId != null) {
        //     this.strictUpdateFill(metaObject, "updateUserId", Long.class, userId);
        // }
    }
}