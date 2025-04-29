package ai.mcps.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class UrlPathExtractor {

    public static String getBaseUrl(String url) {
        try {
            URL parsedUrl = new URL(url);
            String protocol = parsedUrl.getProtocol();
            String host = parsedUrl.getHost();
            int port = parsedUrl.getPort();

            // 构建基础路径
            StringBuilder baseUrl = new StringBuilder(protocol).append("://").append(host);
            if (port != -1) {
                baseUrl.append(":").append(port);
            }
            return baseUrl.toString();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL format", e);
        }
    }

    public static String extractLastPathSegment(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();

            if (path == null || path.isEmpty()) {
                return "";
            }

            String[] segments = path.split("/");
            String lastSegment = segments[segments.length - 1];

            if (lastSegment.isEmpty() && segments.length > 1) {
                lastSegment = segments[segments.length - 2];
            }

            return "/" + lastSegment;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }

    public static void main(String[] args) {
        String testUrl = "https://www.baidu.com/a/b/c";
        System.out.println("Base URL: " + getBaseUrl(testUrl));
    }
}
