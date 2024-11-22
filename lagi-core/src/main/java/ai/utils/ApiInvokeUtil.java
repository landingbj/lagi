package ai.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ApiInvokeUtil {

    private static final ConnectionPool CONNECTION_POOL = new ConnectionPool(
            10, // 最大空闲连接数
            15, // 保持连接的时间
            TimeUnit.MINUTES
    );

    public static String buildUrlByQuery(String url, Map<String, String> queryParams) {
        if(queryParams == null || queryParams.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        int count = 0;
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if(count == 0) {
                sb.append("?").append(entry.getKey()).append("=").append(entry.getValue());
            } else {
                sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
            }
            count++;
        }
        return sb.toString();
    }

    public static String post(String url, Map<String, String> headers, String bodyStr, int timeout, TimeUnit unit) {
         OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout,unit)
                .connectionPool(CONNECTION_POOL)
                .build();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(bodyStr, JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute();){
            String reponseBody = response.body().string();
            if(response.isSuccessful()) {
                return reponseBody;
            }
            else {
                log.error("http code {} , body {}", response.code(), reponseBody);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static String get(String url, Map<String, String> queryParams, Map<String, String> headers, int timeout, TimeUnit unit) {
        url = buildUrlByQuery(url, queryParams);
         OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout,unit)
                .connectionPool(CONNECTION_POOL)
                .build();
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .get();
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        Request request = requestBuilder.build();
        try (Response response = client.newCall(request).execute();){
            String reponseBody = response.body().string();
            if(response.isSuccessful()) {
                return reponseBody;
            }
            else {
                log.error("http code {} , body {}", response.code(), reponseBody);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

}
