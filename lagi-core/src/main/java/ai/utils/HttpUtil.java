package ai.utils;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpUtil {
    private static int _HTTP_TIME_OUT = 4 * 100;

    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static Gson gson = new Gson();

    public static String httpPost(String url, Map<String, String> headers, Object data) throws IOException {
        return httpPost(url, headers, data, -1);
    }

    public static String httpPost(String url, Map<String, String> headers, Object data, int timeout) throws IOException {
        return httpPost(url, headers, gson.toJson(data), timeout);
    }

    public static String httpPost(String url, Map<String, String> headers, String json, int timeout) {
        if (url == null) {
            return null;
        }
        CloseableHttpClient httpClient;
        if (timeout < 0) {
            httpClient = HttpClientBuilder.create().build();
        } else {
            RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();
            httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        }

        HttpPost httpPost = new HttpPost(url);
        for (Entry<String, String> entry : headers.entrySet()) {
            httpPost.setHeader(entry.getKey(), entry.getValue());
        }
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        String responseMessage = null;
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");
            } else {
                String message = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info("Http Get " + url + " statusCode = " + statusCode + " response: " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseMessage;
    }

    public static CloseableHttpResponse httpGetResponse(String url, Map<String, String> data, int timeout) throws IOException {
        RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).build();

        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

        HttpGet httpGet = new HttpGet(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Entry<String, String> entry : data.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        URI uri = null;
        try {
            uri = new URIBuilder(httpGet.getURI()).addParameters(params).build();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
        httpGet.setURI(uri);

        CloseableHttpResponse response = httpClient.execute(httpGet);

        return response;
    }

    public static CloseableHttpResponse httpGetResponse(String url, Map<String, String> data) throws IOException {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient httpClient = httpClientBuilder.build();
        HttpGet httpGet = new HttpGet(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (Entry<String, String> entry : data.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        URI uri = null;
        try {
            uri = new URIBuilder(httpGet.getURI()).addParameters(params).build();
        } catch (URISyntaxException e1) {
            e1.printStackTrace();
        }
        httpGet.setURI(uri);

        CloseableHttpResponse response = httpClient.execute(httpGet);

        return response;
    }

    public static int httpGetStatusCode(String url, Map<String, String> data) {
        int statusCode = -1;

        try {
            CloseableHttpResponse response = httpGetResponse(url, data, _HTTP_TIME_OUT * 8);
            statusCode = response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            statusCode = -1;
        }
        return statusCode;
    }

    public static String httpGet(String url, Map<String, String> data) throws ParseException, IOException {
        if (url == null) {
            return null;
        }
        String responseMessage = null;
        CloseableHttpResponse response = httpGetResponse(url, data);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");
        } else {
            String message = EntityUtils.toString(response.getEntity(), "UTF-8");
            logger.info("Http Get " + url + " statusCode = " + statusCode + " response: " + message);
        }
        return responseMessage;
    }

    public static String httpGet(String url, Map<String, String> data, int timeout) {
        if (url == null) {
            return null;
        }
        String responseMessage = null;
        try {
            CloseableHttpResponse response = httpGetResponse(url, data, timeout);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        } catch (IOException e) {
            logger.info("Http Get " + url + " Exception: ", e);
        }
        return responseMessage;
    }

    public static String multipartUpload(String url, String filePramName, List<File> fileList, Map<String, String> formParmMap) {
        return multipartUpload(url, filePramName, fileList, formParmMap, new HashMap<>());
    }

    public static String multipartUpload(String url, String filePramName, List<File> fileList, Map<String, String> formParmMap
            , Map<String, String> headers) {
        HttpPost post = new HttpPost(url);

        for (Entry<String, String> entry : headers.entrySet()) {
            post.setHeader(entry.getKey(), entry.getValue());
        }

        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.STRICT);
        for (File file : fileList) {
            builder.addPart(filePramName, new FileBody(file, ContentType.DEFAULT_BINARY));
        }
        for (Entry<String, String> entry : formParmMap.entrySet()) {
            builder.addPart(entry.getKey(), new StringBody(entry.getValue(), ContentType.MULTIPART_FORM_DATA));
        }
        final HttpEntity entity = builder.build();
        post.setEntity(entity);
        String responseMessage = null;
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpResponse response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                responseMessage = EntityUtils.toString(response.getEntity(), "UTF-8");
            } else {
                String message = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info("Http multipartUpload " + url + " statusCode = " + statusCode + " response: " + message);
            }
        } catch (IOException e) {
            logger.info("Http multipartUpload " + url + " Exception: ", e);
        }
        return responseMessage;
    }

    public static String getBaseUrl(String url) {
        try {
            URI uri = new URI(url);
            StringBuilder baseUrl = new StringBuilder();
            baseUrl.append(uri.getScheme()).append("://");
            baseUrl.append(uri.getHost());
            if (uri.getPort() != -1) {
                baseUrl.append(":").append(uri.getPort());
            }
            return baseUrl.toString();
        } catch (URISyntaxException e) {
            System.err.println("无效的 URL: " + e.getMessage());
            return null;
        }
    }

    public static String uploadFile(String uploadUrl, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("找不到文件: " + filePath);
        }

        String boundary = "---------------------------" + System.currentTimeMillis();
        URL url = new URL(uploadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // 配置请求
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = connection.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true)) {

            // 添加文件
            writer.append("--" + boundary).append("\r\n");
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"").append("\r\n");
            writer.append("Content-Type: " + Files.probeContentType(Paths.get(filePath))).append("\r\n");
            writer.append("Content-Transfer-Encoding: binary").append("\r\n");
            writer.append("\r\n");
            writer.flush();

            // 写入文件内容
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            // 结束表单
            writer.append("\r\n");
            writer.append("--" + boundary + "--").append("\r\n");
            writer.flush();
        }

        // 读取响应
        int responseCode = connection.getResponseCode();
        System.out.println("HTTP响应代码: " + responseCode);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getResponseCode() < 400 ?
                        connection.getInputStream() : connection.getErrorStream()))) {
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();
        }
    }

}
