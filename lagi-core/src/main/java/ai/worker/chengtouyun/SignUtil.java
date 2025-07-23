package ai.worker.chengtouyun;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SignUtil {
    private static final String SECRET_KEY = "b9ac29bd97ff45d294e6b4a299b087f9";
    /**
     * 生成签名
     * @param params 请求参数
     * @param timestamp 时间戳
     * @return 包含签名和时间戳的 Map
     */
    public static Map<String, String> generateSign(Map<String, String> params, long timestamp) {
        // 1. 参数按 Key 排序
        Map<String, String> sortedParams = new TreeMap<>(params);
        // 2. 拼接参数字符串
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        sb.append("timestamp=").append(timestamp);
        String signature = hmacSha256(SECRET_KEY, sb.toString());
        Map<String, String> result = new HashMap<>();
        result.put("signature", signature);
        result.put("timestamp", String.valueOf(timestamp));
        return result;
    }

    /**
     * HMAC-SHA256加密
     */
    private static String hmacSha256(String secretKey, String data) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate HMAC-SHA256 signature", e);
        }
    }

    /**
     * 字节数组转16进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>();
        params.put("param1", "value1");
        params.put("param2", "value2");
        long timestamp = System.currentTimeMillis();
        String secretKey = "yourSecretKey";
        Map<String, String> signResult = generateSign(params, timestamp);
        System.out.println("Signature: " + signResult.get("signature"));
        System.out.println("Timestamp: " + signResult.get("timestamp"));
    }
}
