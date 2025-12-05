package net.xzh.dify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息请求DTO
 * 对应 curl -X POST 'http://172.17.18.33/v1/chat-messages'
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    
    /**
     * 输入参数，可用于传递额外的参数
     */
    private Map<String, Object> inputs;
    
    /**
     * 用户查询内容
     */
    @NotBlank(message = "内容不能为空")
    private String query;
    
    /**
     * 响应模式
     * 可选值：streaming（流式响应）, blocking（阻塞式响应）
     */
    private String response_mode;
    
    /**
     * 对话ID，用于维持对话上下文
     * 空字符串表示开始新对话
     */
    private String conversation_id;
    
    /**
     * 用户标识
     */
    private String user;
    
    /**
     * 文件列表
     */
    private List<FileInfo> files;
    
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private String type;
        
        private String transfer_method;
        
        private String url;
    }
}