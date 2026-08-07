package net.xzh.rsa.controller;

import net.xzh.rsa.crypto.KeyManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 加密辅助控制器，对外暴露获取 RSA 公钥的接口。
 */
@RestController
@RequestMapping("/api")
public class CryptoController {

    @Resource
    private KeyManager keyManager;

    /**
     * GET /api/publicKey
     * <p>返回 Base64 编码的 RSA 公钥。</p>
     *
     * @return 包含 code、message 和 data.publicKey 的响应体
     */
    @GetMapping("/publicKey")
    public Map<String, Object> getPublicKey() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "ok");
        Map<String, Object> data = new HashMap<>();
        data.put("publicKey", keyManager.getPublicKeyBase64());
        result.put("data", data);
        return result;
    }
}
