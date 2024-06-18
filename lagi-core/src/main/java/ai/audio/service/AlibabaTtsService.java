package ai.audio.service;

import ai.utils.OkHttpUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AlibabaTtsService extends AlibabaAudioService{
    private final String appKey;
    private final Gson gson = new Gson();

    public AlibabaTtsService(String appKey, String accessKeyId, String accessKeySecret) {
        super(accessKeyId, accessKeySecret);
        this.appKey = appKey;
        System.out.println("appKey: " + appKey);
        System.out.println("accessKeyId: " + accessKeyId);
        System.out.println("accessKeySecret: " + accessKeySecret);
    }

    public void processPOSTRequest(String text, String audioSaveFile, String format, int sampleRate, String voice) {
        /**
         * 设置HTTPS POST请求：
         * 1.使用HTTPS协议
         * 2.语音合成服务域名：nls-gateway-cn-shanghai.aliyuncs.com
         * 3.语音合成接口请求路径：/stream/v1/tts
         * 4.设置必须请求参数：appkey、token、text、format、sample_rate
         * 5.设置可选请求参数：voice、volume、speech_rate、pitch_rate
         */
        HashMap<String, String> headers = new HashMap<>();
        String token = getCache().getIfPresent("token");
        if (token == null) {
            token = getToken();
            if (token != null) {
                getCache().put("token", token);
            }
        }
//        headers.put("X-NLS-Token", token);


        String url = "https://nls-gateway-cn-shanghai.aliyuncs.com/stream/v1/tts";
        JsonObject taskObject = new JsonObject();
        taskObject.addProperty("appkey", appKey);
        taskObject.addProperty("token", token);
        taskObject.addProperty("text", text);
        taskObject.addProperty("format", format);
        taskObject.addProperty("voice", voice);
        taskObject.addProperty("sample_rate", sampleRate);
        // voice 发音人，可选，默认是xiaoyun。
        // taskObject.put("voice", "xiaoyun");
        // volume 音量，范围是0~100，可选，默认50。
        // taskObject.put("volume", 50);
        // speech_rate 语速，范围是-500~500，可选，默认是0。
        // taskObject.put("speech_rate", 0);
        // pitch_rate 语调，范围是-500~500，可选，默认是0。
        // taskObject.put("pitch_rate", 0);
        String bodyContent = gson.toJson(taskObject);
        System.out.println("POST Body Content: " + bodyContent);
        RequestBody reqBody = RequestBody.create(MediaType.parse("application/json"), bodyContent);
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(reqBody)
                .build();
        try {
            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            String contentType = response.header("Content-Type");
            if ("audio/mpeg".equals(contentType)) {
                File f = new File(audioSaveFile);
                FileOutputStream fout = new FileOutputStream(f);
                fout.write(response.body().bytes());
                fout.close();
                System.out.println("The POST request succeed!");
            }
            else {
                // ContentType 为 null 或者为 "application/json"
                String errorMessage = response.body().string();
                System.out.println("The POST request failed: " + errorMessage);
            }
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tts(String text) {
        String audioSaveFile = "syAudio.wav";
        String format = "wav";
        int sampleRate = 16000;
        processPOSTRequest(text, audioSaveFile, format, sampleRate, "siyue");
    }
}