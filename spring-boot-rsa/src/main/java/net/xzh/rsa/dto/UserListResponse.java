package net.xzh.rsa.dto;

import java.util.List;

/**
 * 用户列表响应 DTO，标准三层包装结构 {code, message, data}。
 * <p>
 * 用于封装查询用户列表接口的返回结果，
 * 遵循 RESTful API 统一响应规范。
 * </p>
 */
public class UserListResponse {
    /** 状态码，0 表示成功，非 0 表示失败 */
    private int code;
    /** 提示信息，成功时通常为 "success"，失败时包含错误描述 */
    private String message;
    /** 用户列表数据 */
    private List<User> data;

    public UserListResponse() {
    }

    public UserListResponse(int code, String message, List<User> data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<User> getData() {
        return data;
    }

    public void setData(List<User> data) {
        this.data = data;
    }
}
