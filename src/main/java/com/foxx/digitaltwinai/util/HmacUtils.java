package com.foxx.digitaltwinai.util;

import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * HMAC签名工具类
 * (HMAC Signature Utility)
 */
@Component
public class HmacUtils {
    
    /**
     * 默认算法
     * (Default Algorithm)
     */
    private static final String DEFAULT_ALGORITHM = "HmacSHA256";
    
    /**
     * 使用HMAC-SHA256签名
     * (Sign with HMAC-SHA256)
     * 
     * @param data 待签名数据 (Data to sign)
     * @param key 密钥 (Secret key)
     * @return 十六进制签名结果 (Hexadecimal signature result)
     */
    public String sign(String data, String key) {
        try {
            Mac mac = Mac.getInstance(DEFAULT_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), DEFAULT_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC签名失败 (HMAC signature failed)", e);
        }
    }
    
    /**
     * 验证HMAC签名
     * (Verify HMAC Signature)
     * 
     * @param data 待验证数据 (Data to verify)
     * @param key 密钥 (Secret key)
     * @param signature 签名 (Signature)
     * @return 是否有效 (Whether it's valid)
     */
    public boolean verify(String data, String key, String signature) {
        String calculatedSignature = sign(data, key);
        return calculatedSignature.equals(signature);
    }
} 