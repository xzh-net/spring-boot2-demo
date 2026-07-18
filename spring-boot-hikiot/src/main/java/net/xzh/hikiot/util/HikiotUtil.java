package net.xzh.hikiot.util;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;

public class HikiotUtil {

    public static String encryptByPrivateKey(String data, String secret) throws Exception {
        if (StringUtils.isBlank(secret)) {
            return data;
        }
        RSAPrivateKey privateKey = getPrivateKey(secret);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] dataBytes = URLEncoder.encode(data, "UTF-8").getBytes(StandardCharsets.UTF_8);
        byte[] enBytes = null;
        for (int i = 0; i < dataBytes.length; i += 117) {
            byte[] subBytes = cipher.doFinal(ArrayUtils.subarray(dataBytes, i, i + 117));
            enBytes = ArrayUtils.addAll(enBytes, subBytes);
        }
        return Base64.encodeBase64String(enBytes);
    }

    public static String decryptByPrivateKey(String data, String secret) throws Exception {
        if (StringUtils.isBlank(secret)) {
            return data;
        }
        RSAPrivateKey privateKey = getPrivateKey(secret);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] dataBytes = Base64.decodeBase64(data.getBytes(StandardCharsets.UTF_8));
        int inputLen = dataBytes.length;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int offset = 0;
            byte[] cache;
            int i = 0;
            while (inputLen - offset > 0) {
                if (inputLen - offset > 128) {
                    cache = cipher.doFinal(dataBytes, offset, 128);
                } else {
                    cache = cipher.doFinal(dataBytes, offset, inputLen - offset);
                }
                out.write(cache, 0, cache.length);
                i += 1;
                offset = i * 128;
            }
            String outStr = out.toString("UTF-8");
            return URLDecoder.decode(outStr, "UTF-8");
        }
    }

    private static RSAPrivateKey getPrivateKey(String privateKey) throws Exception {
        byte[] keyBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(keySpec);
    }
}