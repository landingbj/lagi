package ai.audio.utils.xfyun.utils;

import org.apache.http.Consts;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.*;

public class HttpConnector {
    private static final Logger log = LoggerFactory.getLogger(HttpConnector.class);
    private final PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();
    private CloseableHttpClient httpClient;

    private HttpConnector() {
    }

    public static HttpConnector build(int maxConnections, int connTimeout, int soTimeout, int retryCount) {
        HttpConnector connector = ConnectorBuilder.CONNECTOR;
        connector.pool.setMaxTotal(maxConnections);
        connector.pool.setDefaultMaxPerRoute(5);

        RequestConfig.Builder builder = RequestConfig.custom().setConnectionRequestTimeout(5000)
                .setConnectTimeout(connTimeout).setSocketTimeout(soTimeout);

        HttpClientBuilder httpClientBuilder = HttpClients.custom().setDefaultRequestConfig(builder.build())
                .setConnectionManager(connector.pool);
        if (retryCount > 0) {
            HttpRequestRetryHandler retryHandler = (exception, executionCount, context) -> {
                if (executionCount > retryCount) {
                    return false;
                }
                if (exception instanceof InterruptedIOException) {
                    return false;
                }
                if (exception instanceof UnknownHostException) {
                    return false;
                }
                if (exception instanceof SSLException) {
                    return false;
                }
                log.info("HttpConnector 第" + executionCount + "次重试");
                return true;
            };
            httpClientBuilder.setRetryHandler(retryHandler);
        }
        connector.httpClient = httpClientBuilder.build();
        return connector;
    }

    private static List<NameValuePair> convertMapToPair(Map<String, String> params) {
        List<NameValuePair> pairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            pairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return pairs;
    }

    public String post(String url, Map<String, String> param) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(convertMapToPair(param), Consts.UTF_8));
        return doExecute(httpPost, Consts.UTF_8.toString());
    }

    public String postByJson(String url, String jsonStr) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        httpPost.setEntity(new StringEntity(jsonStr, Consts.UTF_8));
        return doExecute(httpPost, Consts.UTF_8.toString());
    }

    public String postByBytes(String url, Map<String, String> param, byte[] bytes) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        // 设置 header
        for (String key : param.keySet()) {
            httpPost.setHeader(key, param.get(key));
        }
        if (Objects.nonNull(bytes)) {
            httpPost.setEntity(new ByteArrayEntity(bytes));
        }
        return doExecute(httpPost, Consts.UTF_8.toString());
    }

    public String post(String url, Map<String, String> header, Map<String, String> param) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        //setHeader,添加头文件
        Set<String> keys = header.keySet();
        for (String key : keys) {
            httpPost.setHeader(key, header.get(key));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(convertMapToPair(param), Consts.UTF_8));
        return doExecute(httpPost, Consts.UTF_8.toString());
    }

    public String post(String url, String param) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");
        StringEntity httpEntity = new StringEntity(param, ContentType.APPLICATION_JSON);
        httpPost.setEntity(httpEntity);
        return doExecute(httpPost, Consts.UTF_8.toString());
    }


    private String doExecute(HttpRequestBase requestBase, String charset) throws IOException {
        String result;
        CloseableHttpResponse response = null;
        try {
            response = this.httpClient.execute(requestBase);
            int statusCode = response.getStatusLine().getStatusCode();
            result = (charset == null) ? EntityUtils.toString(response.getEntity()) : EntityUtils.toString(response.getEntity(), charset);
            if (statusCode != HttpStatus.SC_OK) {
//                log.warn("request:{} , status:{} , result:{}", requestBase.getURI(), statusCode, result);
            }

        } finally {
            if (null != response) {
                EntityUtils.consumeQuietly(response.getEntity());
            }
            if (null != requestBase) {
                requestBase.releaseConnection();
            }
        }
        return result;
    }

    public void release() {
        try {
            this.httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ConnectorBuilder {
        private static final HttpConnector CONNECTOR = new HttpConnector();
    }
}
