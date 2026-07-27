package net.xzh.generator.model.entity;

import net.xzh.generator.framework.entity.SuperEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户管理持久化对象
 *
 * @author xzh
 * @date 2026-07-27 08:40:33
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
@Schema(description = "用户管理持久化对象")
public class SysUserDO extends SuperEntity<SysUserDO> {
    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    @Schema(description = "账号") 
    @TableField("username")
    private String username;

        /**
     * 密码
     */
    @Schema(description = "密码") 
    @TableField("password")
    private String password;

        /**
     * 昵称
     */
    @Schema(description = "昵称") 
    @TableField("nickname")
    private String nickname;

        /**
     * 头像
     */
    @Schema(description = "头像") 
    @TableField("head_img_url")
    private String headImgUrl;

        /**
     * 电话
     */
    @Schema(description = "电话") 
    @TableField("mobile")
    private String mobile;

        /**
     * 邮箱
     */
    @Schema(description = "邮箱") 
    @TableField("email")
    private String email;

        /**
     * 余额
     */
    @Schema(description = "余额") 
    @TableField("account_balance")
    private BigDecimal accountBalance;

        /**
     * 状态（0正常 1禁用）
     */
    @Schema(description = "状态（0正常 1禁用）") 
    @TableField("status")
    private Integer status;

        /**
     * 注册时间
     */
    @Schema(description = "注册时间") 
    @TableField("regist_time")
    private LocalDateTime registTime;

        /**
     * 排序
     */
    @Schema(description = "排序") 
    @TableField("sort_by")
    private Integer sortBy;

    }