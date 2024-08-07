package ai.image.adapter.impl;

import ai.annotation.ImgGen;
import ai.common.ModelService;
import ai.common.pojo.ImageGenerationData;
import ai.common.pojo.ImageGenerationRequest;
import ai.common.pojo.ImageGenerationResult;
import ai.image.adapter.IImageGenerationAdapter;
import ai.image.pojo.*;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import okhttp3.HttpUrl;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@ImgGen(modelNames = "tti")
public class SparkImageAdapter extends ModelService implements IImageGenerationAdapter {

    @Override
    public boolean verify() {
        if(getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        if(getSecretKey() == null || getSecretKey().startsWith("you")) {
            return false;
        }
        return true;
    }

    private final String apiUrl = "https://spark-api.cn-huabei-1.xf-yun.com/v2.1/tti";

    private  final Logger log = LoggerFactory.getLogger(SparkImageAdapter.class);


    private SparkGenImgRequest convert2SparkGenImageRequest(ImageGenerationRequest request) {

        int width = 512;
        int height = 512;
        String size = request.getSize();
        if(size !=null) {
            String[] split = size.split("/*");
            if (split.length == 2) {
                width = Integer.parseInt(split[0]);
                height = Integer.parseInt(split[1]);
            }
        }
        SparkChat chat = SparkChat.builder().domain("s291394db").width(width).height(height).build();
        List<SparkText> text = Lists.newArrayList(request.getPrompt()).stream()
                .map(i -> SparkText.builder().role("user").content(i).build())
                .collect(Collectors.toList());
        SparkMessage message = SparkMessage.builder().text(text).build();
        return SparkGenImgRequest.builder()
                .header(SparkGenImgHeader.builder().app_id(getAppId()).build())
                .parameter(SparkGenImgParam.builder().chat(chat).build())
                .payload(SparkGenImgPayload.builder().message(message).build())
                .build();
    }

    private ImageGenerationResult convert2ImageGenerationResult(SparkGenImgResponse response) {
        if(response != null
                && response.getPayload() != null
                && response.getPayload().getChoices() != null
                && response.getPayload().getChoices().getText() != null) {
            List<ImageGenerationData> data = response.getPayload().getChoices().getText().stream()
                    .map(i -> ImageGenerationData.builder().base64Image(i.getContent()).build())
                    .collect(Collectors.toList());
            return ImageGenerationResult.builder()
                    .dataType("base64")
                    .data(data)
                    .build();
        }
        return null;
    }

    // 鉴权方法
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) throws Exception {
        URL url = new URL(hostUrl);
        // 时间
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = format.format(new Date());
        // date="Thu, 12 Oct 2023 03:05:28 GMT";
        // 拼接
        String preStr = "host: " + url.getHost() + "\n" + "date: " + date + "\n" + "POST " + url.getPath() + " HTTP/1.1";
        // System.err.println(preStr);
        // SHA256加密
        Mac mac = Mac.getInstance("hmacsha256");
        SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
        mac.init(spec);

        byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
        // Base64加密
        String sha = Base64.getEncoder().encodeToString(hexDigits);
        // System.err.println(sha);
        // 拼接
        String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
        // 拼接地址
        HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                addQueryParameter("date", date).//
                addQueryParameter("host", url.getHost()).//
                build();

        // System.err.println(httpUrl.toString());
        return httpUrl.toString();
    }

    private String doPostJson(String url, Map<String, String> urlParams, String json) {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        CloseableHttpResponse closeableHttpResponse = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            String asciiUrl = URI.create(url).toASCIIString();
            RequestBuilder builder = RequestBuilder.post(asciiUrl);
            builder.setCharset(StandardCharsets.UTF_8);
            if (urlParams != null) {
                for (Map.Entry<String, String> entry : urlParams.entrySet()) {
                    builder.addParameter(entry.getKey(), entry.getValue());
                }
            }
            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            builder.setEntity(entity);
            HttpUriRequest request = builder.build();
            // 执行http请求
            closeableHttpResponse = closeableHttpClient.execute(request);
            resultString = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (closeableHttpResponse != null) {
                    closeableHttpResponse.close();
                }
                if (closeableHttpClient != null) {
                    closeableHttpClient.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    @Override
    public ImageGenerationResult generations(ImageGenerationRequest request) {
        try {
            String authUrl = getAuthUrl(apiUrl, apiKey, secretKey);
            SparkGenImgRequest sparkGenImgRequest = convert2SparkGenImageRequest(request);
            String post = doPostJson(authUrl, null, JSONUtil.toJsonStr(sparkGenImgRequest));
            SparkGenImgResponse bean = JSONUtil.toBean(post, SparkGenImgResponse.class);
            return  convert2ImageGenerationResult(bean);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
//        SparkImageAdapter sparkImageAdapter = new SparkImageAdapter();
//        sparkImageAdapter.setAppId("53373608");
//        sparkImageAdapter.setSecretKey("OGY0MWY0YmE1ZTlmM2IxMjJhNWI1Y2Fi");
//        sparkImageAdapter.setApiKey("8a06adcf6ff31c5c1f2ed87fd363bdda");
//        ImageGenerationRequest request = ImageGenerationRequest.builder().prompt("画一张飞机在蓝天中飞行的画").build();
//        ImageGenerationResult generations = sparkImageAdapter.generations(request);
//        System.out.println(generations);
    }
}
