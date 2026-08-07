package net.xzh.rsa.controller;

import net.xzh.rsa.dto.User;
import net.xzh.rsa.dto.UserListResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户业务控制器，演示加密请求/响应的透明处理。
 * <p>请求解密、响应加密由 Advice 自动完成，Controller 本身只关注业务逻辑。</p>
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * POST /api/user/list
     * <p>返回硬编码的 5 个用户列表。</p>
     *
     * @param request 解密后的明文请求体（由 DecryptRequestBodyAdvice 自动解密注入）
     * @return 包含 code、message 和用户列表的响应体（由 EncryptResponseBodyAdvice 自动加密返回）
     */
    @PostMapping("/list")
    public UserListResponse getUserList(@RequestBody Map<String, Object> request) {
        List<User> users = new ArrayList<>();
        users.add(new User(1L, "Alice", "alice@example.com", 25));
        users.add(new User(2L, "Bob", "bob@example.com", 30));
        users.add(new User(3L, "Charlie", "charlie@example.com", 35));
        users.add(new User(4L, "Diana", "diana@example.com", 28));
        users.add(new User(5L, "Eve", "eve@example.com", 22));
        return new UserListResponse(0, "ok", users);
    }
}
