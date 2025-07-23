package ai.worker.chengtouyun;

import ai.utils.HttpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.utils.URIBuilder;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Demo {
    // 接口地址（文档中提供的GET接口）
    private static final String VEHICLE_PAGE_URL = "http://192.168.254.182:20088/maintenance/v1/zhiPu/busInfo/detail";

    public static void main(String[] args) {
            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("pageSize", "5");
            params.put("currentPage", "1");
//            params.put("busId", "1001000028");
            long timestamp = System.currentTimeMillis() / 1000;
            // 生成签名
            Map<String, String> signature = SignUtil.generateSign(params, timestamp);

            // 将签名和时间戳加入请求参数
            params.put("signature", signature.get("signature"));
            params.put("timestamp", signature.get("timestamp"));

            System.out.println("请求参数: " + params);

        try {
            String jsonInputString = "{ \"busId\": 1001000028, \"signature\": \""+signature.get("signature")+"\", \"timestamp\": "+timestamp+" }";
            HttpResponse response = HttpRequest.get(VEHICLE_PAGE_URL)
                    .header("Content-Type", "application/json")
                    .body(jsonInputString)  // 将请求体作为 JSON 数据传入
                    .execute();
            System.out.println("Response Code: " + response.getStatus());

            // 打印响应内容
            if (response.getStatus() == 200) {
                System.out.println("Response Content: " + response.body());
            } else {
                System.out.println("Request failed with status code: " + response.getStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
