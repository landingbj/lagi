package ai.utils;


import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {
    private static final OkHttpClient client = new OkHttpClient.Builder()
//            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 7890)))
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(300, TimeUnit.SECONDS)
            .build();

    public static String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }

    public static String get(String url, Map<String, String> params) throws IOException {
        return get(url, params, new HashMap<>());
    }

    public static String get(String url, Map<String, String> params, Map<String, String> headers) throws IOException {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            httpBuilder.addQueryParameter(param.getKey(), param.getValue());
        }
        Request.Builder requestBuilder = new Request.Builder();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            requestBuilder.addHeader(header.getKey(), header.getValue());
        }
        Request request = requestBuilder
                .url(httpBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }

    public static String post(String url, Map<String, String> params) throws IOException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }

    public static String post(String url, String json) throws IOException {
        return post(url, new HashMap<>(), new HashMap<>(), json);
    }

    public static String post(String url, Map<String, String> params, String json) throws IOException {
        return post(url, new HashMap<>(), params, json);
    }

    public static String post(String url, Map<String, String> headers, Map<String, String> params, String json) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                urlBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }
        HttpUrl finalUrl = urlBuilder.build();

        RequestBody body = RequestBody.create(json, JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(finalUrl)
                .post(body);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                requestBuilder.addHeader(header.getKey(), header.getValue());
            }
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }

    public static String postForm(String url, Map<String, String> formData) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        for (Map.Entry<String, String> data : formData.entrySet()) {
            builder.addFormDataPart(data.getKey(), data.getValue());
        }
        RequestBody body = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .method("POST", body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return response.body().string();
        }
    }

    public static String postFile(String url, HashMap<String, String> headers, String fileName) {
        RequestBody body;
        File file = new File(fileName);
        if (!file.isFile()) {
            return null;
        } else {
            body = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        }
        Headers.Builder hb = new Headers.Builder();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                hb.add(entry.getKey(), entry.getValue());
            }
        }
        Request request = new Request.Builder()
                .url(url)
                .headers(hb.build())
                .post(body)
                .build();

        String ret = null;
        try {
            Response s = client.newCall(request).execute();
            ret = s.body().string();
            s.close();
        } catch (IOException e) {
            System.err.println("get result error " + e.getMessage());
        }
        return ret;
    }
}