package ai.audio.utils.xfyun.utils;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HttpUtil {
    private HttpUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);

    private static final String UTF8 = "UTF-8";
    /**
     * 组件的httpClient
     */
    private static CloseableHttpClient httpClient;

    static {
        // 听见服务、流控组件连接池
        PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();
        pool.setMaxTotal(600);//客户端总并行链接最大数
        pool.setDefaultMaxPerRoute(200);//每个主机的最大并行链接数
        httpClient = HttpClients.createMinimal(pool);
    }

    /**
     * 请求的upload接口, 发送音频创建转写订单
     *
     * @param url       请求地址
     * @param in        需要转写的音频流
     * @return 返回结果
     */
    public static String iflyrecUpload(String url,  InputStream in) {
        // 1、准备参数
        HttpPost httpPost = new HttpPost(url);
        // 设置超时时间, 防止504的时候会耗时30分钟
        RequestConfig requestConfig = RequestConfig.custom()
                //从连接池中获取连接的超时时间
                .setConnectionRequestTimeout(5000)
                //与服务器连接超时时间, 指的是连接一个url的连接等待时间
                .setConnectTimeout(600000)
                // 读取超时, 指的是连接上一个url，获取response的返回等待时间
                .setSocketTimeout(600000).build();
        httpPost.setConfig(requestConfig);
        HttpEntity requestEntity = new InputStreamEntity(in, ContentType.APPLICATION_JSON);
        //System.out.println("---"+requestEntity);
        httpPost.setEntity(requestEntity);

        // 2、执行请求
        return doExecute(httpPost, null);
    }

    /**
     * 请求听见的获取结果接口
     *
     * @param url       请求路径
     * @return 返回结果
     */
    public static String iflyrecGet(String url) {
        // 1、准备参数
        HttpGet httpget = new HttpGet(url);
        // 2、执行请求
        return doExecute(httpget, UTF8);
    }

    /**
     * 流控组件调用
     *
     * @param url 请求路径
     * @return 返回结果
     */
    public static String flowCtrlGet(String url) {
        // 1、准备参数
        HttpGet httpget = new HttpGet(url);
        RequestConfig requestConfig = RequestConfig.custom()
                //从连接池中获取连接的超时时间
                .setConnectionRequestTimeout(500)
                //与服务器连接超时时间
                .setConnectTimeout(500)
                // 读取超时
                .setSocketTimeout(500).build();
        httpget.setConfig(requestConfig);
        // 2、执行请求
        return doExecute(httpget, null);
    }

    /**
     * 流传输的post
     *
     * @param url  请求路径
     * @param body 字节流数据
     * @return 返回结果
     */
    public static String post(String url, byte[] body) {
        // 1、准备参数
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new ByteArrayEntity(body, ContentType.create("application/octet-stream", Consts.UTF_8)));
        // 2、执行请求
        return doExecute(httpPost, UTF8);
    }

    /**
     * 带字符串参数的post请求
     *
     * @param url   请求路径
     * @param param 字符串参数
     * @return 返回结果
     */
    public static String post(String url, String param) {
        // 1、准备参数
        HttpPost httpPost = new HttpPost(url);
        // 设置超时时间
        RequestConfig requestConfig = RequestConfig.custom()
                //从连接池中获取连接的超时时间
                .setConnectionRequestTimeout(1000)
                //与服务器连接超时时间, 指的是连接一个url的连接等待时间
                .setConnectTimeout(10000)
                // 读取超时, 指的是连接上一个url，获取response的返回等待时间
                .setSocketTimeout(10000).build();
        httpPost.setConfig(requestConfig);
        httpPost.setEntity(new StringEntity(param, ContentType.APPLICATION_JSON));
        // 2、执行请求
        return doExecute(httpPost, null);
    }

    /**
     * 发送HttpGet请求
     *
     * @param url 请求路径
     * @return 返回结果
     */
    public static String sendGet(String url) {
        // 1、准备参数
        HttpGet httpget = new HttpGet(url);
        // 2、执行请求
        return doExecute(httpget, null);
    }

    /**
     * 执行网络请求
     *
     * @param requestBase http请求对象
     * @param charset     字符集
     * @return 返回结果
     */
    private static String doExecute(HttpRequestBase requestBase, String charset) {
        String result = null;
        try (CloseableHttpResponse response = httpClient.execute(requestBase)) {
            // 3、检查结果状态
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.error("网络异常");
                return null;
            }
            // 4、获取结果
            result = charset == null
                    ? EntityUtils.toString(response.getEntity())
                    : EntityUtils.toString(response.getEntity(), charset);
        } catch (Exception e) {
            LOGGER.error("网络异常", e);
        }
        return result;
    }

    /**
     * 发送post请求
     *
     * @param url    请求路径
     * @param header 请求头
     * @param body   请求数据
     * @return 返回结果
     */
    public static String postWithHeader(String url, Map<String, String> header, String body) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        PrintWriter out = null;
        InputStreamReader is = null;
        try {
            // 设置 url
            URL realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            // 设置 header
            for (Entry<String, String> entry : header.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            // 设置请求 body
            connection.setDoOutput(true);
            connection.setDoInput(true);
            out = new PrintWriter(connection.getOutputStream());
            // 保存body
            out.print(body);
            // 发送body
            out.flush();
            // 获取响应body
            is = new InputStreamReader(connection.getInputStream());
            in = new BufferedReader(is);
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            LOGGER.error("HttpUtil postWithHeader Exception!", e);
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("close IO resource Exception!", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error("close IO resource Exception!", e);
                }
            }
            if (out != null) {
                out.close();
            }
        }
        return result.toString();
    }

    /**
     * 将集合转换为路径参数
     *
     * @param param 集合参数
     * @return 路径参数
     * @author white
     */
    public static String parseMapToPathParam(Map<String, Object> param) {
        StringBuilder sb = new StringBuilder();
        try {
            Set<Entry<String, Object>> entryset = param.entrySet();
            boolean isFirst = true;
            for (Entry<String, Object> entry : entryset) {
                if (!isFirst) {
                    sb.append("&");
                } else {
                    isFirst = false;
                }
                sb.append(URLEncoder.encode(entry.getKey(), UTF8));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue().toString(), UTF8));
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("HttpUtil parseMapToPathParam Exception!", e);
        }

        return sb.toString();
    }
}
