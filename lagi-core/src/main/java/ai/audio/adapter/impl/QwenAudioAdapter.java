package ai.audio.adapter.impl;

import ai.annotation.ASR;
import ai.annotation.TTS;
import ai.audio.adapter.IAudioAdapter;
import ai.common.ModelService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;
import ai.utils.ApiInvokeUtil;
import ai.utils.LagiGlobal;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Builder;
import lombok.Data;
import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@TTS(company = "qwen", modelNames = "qwen-tts")
@ASR(company = "qwen", modelNames = "paraformer-realtime-v2")
public class QwenAudioAdapter extends ModelService implements IAudioAdapter {

    private final static String ENDPOINT = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";


    private final Gson gson = new Gson();

    private final static List<String> voice = Lists.newArrayList("Cherry",
            "Serena",
            "Ethan",
            "Chelsie");

    private final static Map<String, Integer> map = new HashMap<>();

    static {
        map.put("neutral", 0);
        map.put("happy", 1);
        map.put("sad", 2);
        map.put("fear", 3);
        map.put("hate", 0);
        map.put("surprise", 1);
    }


    @Override
    public boolean verify() {
        if(getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        return true;
    }


    private static final String TTS_MODEL = "qwen-tts";
    private static final String ASR_MODEL = "paraformer-realtime-v2";

    @Override
    public AsrResult asr(File audio, AudioRequestParam param) {
        String format = audio.getName().split("\\.")[1];
        Recognition recognizer = new Recognition();
        RecognitionParam param1 =
                RecognitionParam.builder()
                        // 若没有将API Key配置到环境变量中，需将下面这行代码注释放开，并将apiKey替换为自己的API Key
                         .apiKey(getApiKey())
                        .model(ASR_MODEL)
                        .format(format)
                        .sampleRate(16000)
                        // “language_hints”只支持paraformer-v2和paraformer-realtime-v2模型
                        .parameter("language_hints", new String[]{"zh", "en"})
                        .build();

        try {
            String result = recognizer.call(param1, audio);
            System.out.println("识别结果：" + result);
            return AsrResult.builder().status(LagiGlobal.ASR_STATUS_SUCCESS).result(result).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Builder
    @Data
    static class QwenTTSRequest {
        private String model;
        private QwenTTSInput input;
    }

    @Builder
    @Data
    static class QwenTTSInput {
        private String text;
        private String voice;
    }

    @Override
    public TTSResult tts(TTSRequestParam param) {
        if(param.getEmotion() == null) {
            param.setEmotion(voice.get(map.getOrDefault("neutral", 0)));
        } else {
            param.setEmotion(voice.get(map.getOrDefault(param.getEmotion(), 0)));
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + getApiKey());
        headers.put("Content-Type", "application/json");

        String text = param.getText();

        if (text.length() > 512) {
            text = text.substring(0, 512);
        }

        QwenTTSRequest body = QwenTTSRequest.builder()
                .model(TTS_MODEL)
                .input(QwenTTSInput.builder().text(text).voice(param.getEmotion()).build())
                .build();
        String post = ApiInvokeUtil.post(ENDPOINT, headers, gson.toJson(body), 60, TimeUnit.SECONDS);
        Map<String, Object> ttsResult = gson.fromJson(post, new TypeToken<Map<String, Object>>() {
        });
        if (ttsResult == null || ttsResult.get("output") == null) {
            return TTSResult.builder().result("").build();
        }
        Map<String, Object> output = (Map<String, Object>) ttsResult.get("output");
        if(output == null || output.get("audio") == null){
            return TTSResult.builder().status(LagiGlobal.TTS_STATUS_FAILURE).status(LagiGlobal.TTS_STATUS_FAILURE).result("").build();
        }
        Map<String, Object> audio = (Map<String, Object>) output.get("audio");
        if(audio == null || audio.get("url") == null) {
            return TTSResult.builder().status(LagiGlobal.TTS_STATUS_FAILURE).result("").build();
        }
        String url = (String) audio.get("url");
        return TTSResult.builder().status(LagiGlobal.TTS_STATUS_SUCCESS).result(url).build();

    }

    public List<String> getVoice() {
        return voice;
    }


}
