package ai.utils;

import ai.common.exception.RRException;
import ai.common.utils.ObservableList;
import ai.llm.utils.LLMErrorConstants;
import cn.hutool.core.text.StrFormatter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

@Slf4j
public class ApiInvokeUtil {

    private static final TrustManager[] TRUST_ALL_CERTIFICATES = new TrustManager[]{
        new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
    };


    private static final ConnectionPool CONNECTION_POOL = new ConnectionPool(
            100, // 最大空闲连接数
            120, // 保持连接的时间
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

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, TRUST_ALL_CERTIFICATES, new java.security.SecureRandom());
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(timeout, unit)
                    .readTimeout(timeout, unit)
                    .writeTimeout(timeout, unit)
                    .connectionPool(new ConnectionPool())
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) TRUST_ALL_CERTIFICATES[0])
                    .hostnameVerifier((hostname, session) -> true)
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
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    return responseBody;
                } else {
                    log.error("HTTP code {} , body {}", response.code(), responseBody);
                }
            } catch (IOException e) {
                log.error("Request failed: {}", e.getMessage());
                System.out.println("Request failed: " + e);
            }
        } catch (Exception e) {
            log.error("SSL initialization failed: {}", e.getMessage());
        }
        return null;
    }

    public static String post(String url, Map<String, String> headers, Map<String, String> form, int timeout, TimeUnit unit) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout,unit)
                .readTimeout(timeout, unit)
                .writeTimeout(timeout, unit)
                .connectionPool(CONNECTION_POOL)
                .build();
        Set<String> key = form.keySet();
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (String s : key) {
            formBuilder.add(s, form.get(s));
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(formBuilder.build());
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

    public static <R> ObservableList<R> sse(String url, Map<String, String> headers, Map<String, String> form,
                                            Integer timeout, TimeUnit timeUnit, Function<String, R> convertResponseFunc) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, timeUnit)
                .connectionPool(CONNECTION_POOL)
                .build();
        Set<String> key = form.keySet();
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (String s : key) {
            formBuilder.add(s, form.get(s));
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Accept", "text/event-stream")
                .post(formBuilder.build());
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        Request request = requestBuilder.build();
        return sse(convertResponseFunc, client, request);
    }

    private static <R> ObservableList<R> sse(Function<String, R> convertResponseFunc, OkHttpClient client, Request request) {
        EventSource.Factory factory = EventSources.createFactory(client);
        ObservableList<R> res = new ObservableList<>();
        RRException exception = new RRException();
        factory.newEventSource(request, new EventSourceListener() {
            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                int code = response.code();
                try {
                    String bodyStr = response.body().string();
                    if(code != 200) {
                        exception.setCode(code);
                        exception.setMsg(bodyStr);
                        closeConnection(eventSource);
                    } else {
                    }
                } catch (IOException e) {

                }
            }
            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                R chatCompletionResult = convertResponseFunc.apply(data);
                if (chatCompletionResult != null) {
                    res.add(chatCompletionResult);
                }
            }
            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                if(t instanceof SocketTimeoutException) {
                    exception.setCode(LLMErrorConstants.TIME_OUT);
                    exception.setMsg(StrFormatter.format("{\"error\":\"{}\"}", t.getMessage()));
                } else {
                    try {
                        String bodyStr = response.body().string();
                        exception.setCode(response.code());
                        exception.setMsg(bodyStr);
                    } catch (Exception e) {
                        exception.setCode(LLMErrorConstants.OTHER_ERROR);
                        exception.setMsg(StrFormatter.format("{\"error\":\"{}\"}", t.getMessage()));
                    }
                }
                if(t != null) {
                    log.error("model request failed error {}", t.getMessage());
                }
                closeConnection(eventSource);
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                closeConnection(eventSource);
            }

            private void closeConnection(EventSource eventSource) {
                res.onComplete();
                eventSource.cancel();
                client.dispatcher().executorService().shutdown();
            }
        });
        Iterable<R> iterable = res.getObservable().blockingIterable();
        iterable.iterator().hasNext();
        return res;
    }


    public static <R> ObservableList<R> sse(String url, Map<String, String> headers, String json,
                                                   Integer timeout, TimeUnit timeUnit, Function<String, R> convertResponseFunc) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeout, timeUnit)
                .connectionPool(CONNECTION_POOL)
                .build();
        MediaType mediaType = MediaType.get("application/json");
        RequestBody body = RequestBody.create(json, mediaType);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Accept", "text/event-stream")
                .post(body);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }
        Request request = requestBuilder.build();
        return sse(convertResponseFunc, client, request);
    }

}
