package ai.agent.customer.utils;

import okhttp3.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class HttpUtil {

    private static OkHttpClient httpClient = new OkHttpClient.Builder().build();

    public static byte[] doGet(String url, Map<String, String[]> header, Map<String, String[]> params, String expectContentType) {
        Request.Builder builder = new Request.Builder();
        addHeader(builder, header);
        addUrlParam(builder, url, params);
        return requestExec(builder.build(), expectContentType);
    }

//    public static byte[] doPost(String url, Map<String, String[]> header, Map<String, String[]> body, String expectContentType) {
//        Request.Builder builder = new Request.Builder().url(url);
//        addHeader(builder, header);
//        addBodyParam(builder, body, "POST");
//        return requestExec(builder.build(), expectContentType);
//    }

    public static byte[] doPost(String url, Map<String, String[]> header, Map<String, String[]> body, String expectContentType) throws IOException {
        HttpURLConnection connection = null;
        OutputStreamWriter writer = null;
        InputStream responseStream = null;

        try {
            // 创建连接
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true); // 允许输出
            connection.setDoInput(true);  // 允许输入

            // 添加请求头
            if (header != null) {
                for (String key : header.keySet()) {
                    String[] values = header.get(key);
                    if (values != null) {
                        for (String value : values) {
                            connection.addRequestProperty(key, value);
                        }
                    }
                }
            }

            // 写入请求体
            writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            StringBuilder bodyBuilder = new StringBuilder();
            if (body != null) {
                for (String key : body.keySet()) {
                    String[] values = body.get(key);
                    if (values != null) {
                        for (String value : values) {
                            if (bodyBuilder.length() > 0) {
                                bodyBuilder.append("&");
                            }
                            bodyBuilder.append(key).append("=").append(value);
                        }
                    }
                }
            }
            writer.write(bodyBuilder.toString());
            writer.flush();

            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                responseStream = connection.getInputStream();
                String contentType = connection.getContentType();
                if (contentType != null && !contentType.contains(expectContentType)) {
                    String res = readStream(responseStream);
                    System.out.println(res);
                    return null;
                }
                return readStreamBytes(responseStream);
            } else {
                System.out.println("request failed, http code: " + responseCode);
            }
        } finally {
            // 关闭资源
            if (writer != null) {
                writer.close();
            }
            if (responseStream != null) {
                responseStream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private static String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private static byte[] readStreamBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private static void addHeader(Request.Builder builder, Map<String, String[]> header) {
        if (header == null) {
            return;
        }
        for (String key : header.keySet()) {
            String[] values = header.get(key);
            if (values != null) {
                for (String value : values) {
                    builder.addHeader(key, value);
                }
            }
        }
    }

    private static void addUrlParam(Request.Builder builder, String url, Map<String, String[]> params) {
        if (params == null) {
            return;
        }
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (String key : params.keySet()) {
            String[] values = params.get(key);
            if (values != null) {
                for (String value : values) {
                    urlBuilder.addQueryParameter(key, value);
                }
            }
        }
        builder.url(urlBuilder.build());
    }

    private static void addBodyParam(Request.Builder builder, Map<String, String[]> body, String method) {
        if (body == null) {
            return;
        }
        FormBody.Builder formBodyBuilder = new FormBody.Builder(StandardCharsets.UTF_8);
        for (String key : body.keySet()) {
            String[] values = body.get(key);
            if (values != null) {
                for (String value : values) {
                    formBodyBuilder.add(key, value);
                }
            }
        }
        builder.method(method, formBodyBuilder.build());
    }

    private static byte[] requestExec(Request request, String expectContentType) {
        Objects.requireNonNull(request, "okHttp request is null");

        try (Response response = httpClient.newCall(request).execute()) {
            if (response.code() == 200) {
                ResponseBody body = response.body();
                if (body != null) {
                    String contentType = response.header("Content-Type");
                    if (contentType != null && !contentType.contains(expectContentType)) {
                        String res = new String(body.bytes(), StandardCharsets.UTF_8);
                        System.out.println(res);
                        return null;
                    }
                    return body.bytes();
                }
                System.out.println("response body is null");
            } else {
                System.out.println("request failed, http code: " + response.code());
            }
        } catch (IOException ioException) {
            System.out.println("request exec error: " + ioException.getMessage());
        }
        return null;
    }
}
