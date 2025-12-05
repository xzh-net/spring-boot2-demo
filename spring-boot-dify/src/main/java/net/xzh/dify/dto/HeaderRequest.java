package net.xzh.dify.dto;


import lombok.Data;

/**
 * 全局头参数实体
 * @author xzh
 *
 */

@Data
public class HeaderRequest {
    // 智能体key
    private String api_key;
    
    // 用户
    private String user_id;
}
