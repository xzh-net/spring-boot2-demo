package net.xzh.generator.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.xzh.generator.common.vo.BasePage;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * 用户管理分页查询条件
 *
 * @author xzh
 * @date 2026-07-27 09:21:37
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户管理分页查询条件")
public class SysUserPageQuery extends BasePage {


    /**
     * 账号
     */
            @Schema(description = "账号")
    private String username;
    
    /**
     * 密码
     */
            @Schema(description = "密码")
    private String password;
    
    /**
     * 昵称
     */
            @Schema(description = "昵称")
    private String nickname;
    
    /**
     * 头像
     */
            @Schema(description = "头像")
    private String headImgUrl;
    
    /**
     * 电话
     */
            @Schema(description = "电话")
    private String mobile;
    
    /**
     * 邮箱
     */
            @Schema(description = "邮箱")
    private String email;
    
    /**
     * 余额
     */
            @Schema(description = "余额")
    private BigDecimal accountBalance;
    
    /**
     * 状态（0正常 1禁用）
     */
            @Schema(description = "状态（0正常 1禁用）")
    private Integer status;
    
    /**
     * 注册时间
     */
            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
        @Schema(description = "注册时间")
    private LocalDateTime registTime;
    
    /**
     * 排序
     */
            @Schema(description = "排序")
    private Integer sortBy;
    
}