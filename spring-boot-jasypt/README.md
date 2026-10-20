# Jasypt 配置文件加密

官方地址：http://www.jasypt.org/

Jasypt（Java Simplified Encryption）是一个Java库，用于简化加密操作。它提供了一种简单的方式来在Java应用程序中执行加密和解密操作。Jasypt本身不直接支持SM4，使用自定义解密器利用 Bouncy Castle 库实现了 SM4 加密算法，不依赖 Hutool 等工具库。

操作步骤

1. 运行 KeyGeneratorUtil 类生成 SM4 密钥，将输出的密钥保存。
2. 在 application.yaml 中设置 jasypt.encryptor.sm4-key 为上一步生成的密钥
3. 在 application.yaml 中使用 ENC(加密后的密码) 格式配置数据库密码

> 生产环境中，SM4 密钥应该通过环境变量或安全密钥管理系统传递，而不是硬编码在配置文件中



**国密 (SM系列算法)和国标的区别和联系**

| 主要用途               | 国产商用密码    | 对标国际商用密码 | 应用场景                       |
| :--------------------- | :-------------- | :--------------- | :----------------------------- |
| **数据加密保护**       | SM1、SM4、SM7   | AES、DES、3DES   | 数据库加密、文件加密、通信加密 |
| **身份认证与数字签名** | SM2、SM9        | RSA、ECDSA、DSA  | 数字证书、电子签章、身份验证   |
| **数据完整性验证**     | SM3             | SHA-256、MD5     | 数据防篡改、数字指纹           |
| **流数据加密**         | ZUC(祖冲之算法) | RC4、ChaCha20    | 无线通信、实时音视频加密       |
| **密钥协商交换**       | SM2(密钥交换)   | DH、ECDH         | SSL/TLS、安全通信建立          |
