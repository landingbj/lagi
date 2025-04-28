package ai.mcps.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlPathExtractor {
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
        String url = "http://example.com/sse/page?name=John&age=30";
        String lastPathSegment = extractLastPathSegment(url);
        System.out.println(lastPathSegment);
    }
}
