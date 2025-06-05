package ai.audio.adapter.impl;

import ai.annotation.ASR;
import ai.audio.adapter.IAudioAdapter;
import ai.audio.utils.xfyun.sign.LfasrSignature;
import ai.audio.utils.xfyun.utils.HttpUtil;
import ai.common.ModelService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;
import ai.utils.LagiGlobal;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SignatureException;
import java.util.HashMap;

@ASR(company = "spark", modelNames = "asr")
public class SparkAudioAdapter extends ModelService implements IAudioAdapter {
    private static final String ASRHOST = "https://raasr.xfyun.cn";
    @Override
    public boolean verify() {
        if(getAppId() == null || getAppId().startsWith("you")) {
            return false;
        }
        if(getSecretKey() == null || getSecretKey().startsWith("you")) {
            return false;
        }
        return true;
    }
    private static final Gson gson = new Gson();

    @Override
    public AsrResult asr(File audio, AudioRequestParam param) {
        try {
            String result = upload(audio);
            String jsonStr = StringEscapeUtils.unescapeJavaScript(result);
            String orderId = String.valueOf(JSONUtil.getByPath(JSONUtil.parse(jsonStr), "content.orderId"));
            String message = getContent(getResult(orderId));
            AsrResult asrResult = AsrResult.builder().task_id(orderId).result(message).status(LagiGlobal.ASR_STATUS_SUCCESS).message("success").build();
            return asrResult;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public TTSResult tts(TTSRequestParam param) {
        return null;
    }

    private String upload(File audio) throws SignatureException, FileNotFoundException {
        HashMap<String, Object> map = new HashMap<>(16);
        String fileName = audio.getName();
        long fileSize = audio.length();
        map.put("appId", getAppId());
        map.put("fileSize", fileSize);
        map.put("fileName", fileName);
        map.put("duration", "200");
        LfasrSignature lfasrSignature = new LfasrSignature(getAppId(), getSecretKey());
        map.put("signa", lfasrSignature.getSigna());
        map.put("ts", lfasrSignature.getTs());

        String paramString = HttpUtil.parseMapToPathParam(map);
        System.out.println("upload paramString:" + paramString);

        String url = ASRHOST + "/v2/api/upload" + "?" + paramString;
        String response = HttpUtil.iflyrecUpload(url, new FileInputStream(audio));
        return response;
    }

    private String getResult(String orderId) throws SignatureException, InterruptedException, IOException {
        HashMap<String, Object> map = new HashMap<>(16);
        map.put("orderId", orderId);
        LfasrSignature lfasrSignature = new LfasrSignature(getAppId(), getSecretKey());
        map.put("signa", lfasrSignature.getSigna());
        map.put("ts", lfasrSignature.getTs());
        map.put("appId", getAppId());
        map.put("resultType", "transfer,predict");
        String paramString = HttpUtil.parseMapToPathParam(map);
        String url = ASRHOST + "/v2/api/getResult" + "?" + paramString;
        System.out.println("\nget_result_url:" + url);
        while (true) {
            String response = HttpUtil.iflyrecGet(url);
            JsonParse jsonParse = gson.fromJson(response,JsonParse.class);
            if (jsonParse.content.orderInfo.status == 4 || jsonParse.content.orderInfo.status == -1) {
                return response;
            } else {
                //建议使用回调的方式查询结果，查询接口有请求频率限制
                Thread.sleep(7000);
            }
        }
    }

    private static String getContent(String jsonString) {
        try {
            if (jsonString == null || jsonString.trim().isEmpty()) {
                throw new IllegalArgumentException("传入的 JSON 字符串为空");
            }
            jsonString = jsonString
                    .replaceAll("[\\uFEFF\\u200B\\u200C\\u200D\\u200E\\u200F\\uFFFC\\uFFF9\\uFFFA\\uFFFB\\uFFFC\\uFFFD]", "")
                    .trim();

            if (!jsonString.startsWith("{")) {
                throw new JSONException("JSON 字符串不是对象格式，实际内容为: " + jsonString);
            }
            JSONObject outerJson = new JSONObject(jsonString);
            JSONObject content = outerJson.getJSONObject("content");
            String orderResultJsonStr = content.getString("orderResult");
            JSONObject orderResult = new JSONObject(orderResultJsonStr);
            JSONArray lattice = orderResult.getJSONArray("lattice");
            JSONObject firstLattice = lattice.getJSONObject(0);
            String json1bestStr = firstLattice.getString("json_1best");
            JSONObject json1best = new JSONObject(json1bestStr);
            JSONObject st = json1best.getJSONObject("st");
            JSONArray rtArray = st.getJSONArray("rt");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < rtArray.length(); i++) {
                JSONObject rtItem = rtArray.getJSONObject(i);
                JSONArray wsArray = rtItem.getJSONArray("ws");
                for (int j = 0; j < wsArray.length(); j++) {
                    JSONObject wsItem = wsArray.getJSONObject(j);
                    JSONArray cwArray = wsItem.getJSONArray("cw");
                    for (int k = 0; k < cwArray.length(); k++) {
                        JSONObject cwItem = cwArray.getJSONObject(k);
                        result.append(cwItem.getString("w"));
                    }
                }
            }
            return result.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    class JsonParse {
        Content content;
    }

    class Content {
        OrderInfo orderInfo;
    }

    class OrderInfo {
        Integer status;
    }
}
