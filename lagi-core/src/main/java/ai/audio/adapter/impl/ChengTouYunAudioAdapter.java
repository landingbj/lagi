package ai.audio.adapter.impl;

import ai.annotation.ASR;
import ai.audio.adapter.IAudioAdapter;
import ai.common.ModelService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;
import ai.oss.UniversalOSS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.*;
import java.nio.file.Files;

@ASR(company = "chengtouyun", modelNames = "chengtouyunasr")
public class ChengTouYunAudioAdapter  extends ModelService implements IAudioAdapter {
//    private static final String ASRHOST = "http://218.109.64.68:11001/asr/api";
private static final String ASRHOST = "http://20.17.39.241:8890/asr/api";
    private final Gson gson = new Gson();
    private UniversalOSS universalOSS;

    @Override
    public boolean verify() {
        return true;
    }


    @Override
    public AsrResult asr(File audio, AudioRequestParam param) {
        try {
            byte[] fileBytes = Files.readAllBytes(audio.toPath());
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(fileBytes);
            String fileName = audio.getName();
            param.setFormat("mp3");
            param.setSample_rate(16000);
            if (fileName != null){
                int dotIndex = fileName.lastIndexOf(".");
                if (dotIndex > 0) {
                    param.setFormat(fileName.substring(dotIndex + 1));
                }
            }
            Request.Builder requestBuilder = new Request.Builder()
                    .url(ASRHOST)
                    .post(body)
                    .addHeader("Content-Type", "audio/mpeg")
//                    .addHeader("access_token", getAccessToken())
                    .addHeader("audio_format", param.getFormat())
                    .addHeader("sample_rate", param.getSample_rate().toString())
                    .addHeader("domain", "common");
            Request request = requestBuilder.build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                String errorMsg = response.body() != null ? response.body().string() : "Unknown error";
                return new AsrResult("", "", response.code(), "Recognition failed: " + errorMsg);
            }

            String respBody = response.body() != null ? response.body().string() : "";
            // 创建 ObjectMapper 对象
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(respBody);

            // 从 JSON 中提取对应字段
            String task_id = rootNode.path("trace_id").asText();
            String result = rootNode.path("text").asText();
            Integer status = rootNode.path("code").asInt();
            String message = rootNode.path("info").asText();

            // 创建 AsrResult 对象并填充数据
            AsrResult asrResult = new AsrResult();
            asrResult.setTask_id(task_id);
            asrResult.setResult(result);
            asrResult.setStatus(20000000);
            asrResult.setMessage(message);

            return asrResult;

        } catch (IOException e) {
            e.printStackTrace();
            return new AsrResult("", "", -1, "Error occurred during ASR processing");
        }
    }

    @Override
    public TTSResult tts(TTSRequestParam param) {
        return null;
    }

    public static void main(String[] args) {
        // 创建 AudioRequestParam 参数对象
        AudioRequestParam param = new AudioRequestParam();
        param.setModel("default_model");
        param.setAppkey("your_app_key");
        param.setFormat("mp3");
        param.setSample_rate(8000);
        param.setEnable_punctuation_prediction(true);
        param.setEnable_inverse_text_normalization(true);
        param.setEnable_voice_detection(false);
        param.setDisfluency(true);

        // 创建 AsrService 实例
        IAudioAdapter asrService = new ChengTouYunAudioAdapter();

        // 处理音频文件
        File audioFile = new File("C:\\temp\\audiofile.mp3");
        AsrResult result = asrService.asr(audioFile, param);

        // 输出识别结果
        System.out.println("Task ID: " + result.getTask_id());
        System.out.println("Result: " + result.getResult());
        System.out.println("Status: " + result.getStatus());
        System.out.println("Message: " + result.getMessage());
    }
}
