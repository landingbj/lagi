package ai.utils;

import javax.servlet.http.HttpServletRequest;

public class ClientIpAddressUtil {

    public static String getClientIpAddress(HttpServletRequest request) {
        // 尝试从 X-Forwarded-For 头获取客户端 IP 地址
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            // 如果 X-Forwarded-For 头不存在或无效，使用 getRemoteAddr() 方法
            ipAddress = request.getRemoteAddr();
        } else {
            // X-Forwarded-For 头可能包含多个 IP 地址，取第一个作为客户端 IP 地址
            String[] ipAddresses = ipAddress.split(",");
            if (ipAddresses.length > 0) {
                ipAddress = ipAddresses[0].trim();
            }
        }

        return ipAddress;
    }
}