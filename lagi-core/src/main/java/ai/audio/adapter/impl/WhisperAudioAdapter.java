package ai.audio.adapter.impl;

import ai.audio.adapter.IAudioAdapter;
import ai.common.ModelService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;
import ai.utils.LagiGlobal;
import ai.utils.OkHttpUtil;
import com.google.gson.Gson;
import lombok.Data;
import okhttp3.*;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class WhisperAudioAdapter extends ModelService implements IAudioAdapter {

    private final Gson gson = new Gson();

    private String getUploadUrl() {
        return getEndpoint() + "/upload_file";
    }
    private String getAudio2TextUrl() {
        return getEndpoint() + "/audio2text";
    }
    private String getText2Audio() {
        return getEndpoint() + "/text2audio";
    }


    @Override
    public AsrResult asr(File audio, AudioRequestParam param) {
        AsrResult result = new AsrResult();
        try {
            String filename = uploadFile(audio);
            Map<String, Object> bodyObj = new HashMap<>();
            bodyObj.put("filename", filename);
            bodyObj.put("language", "zh");
            String post = OkHttpUtil.post(getAudio2TextUrl(), null, gson.toJson(bodyObj));
            IAsrResult parse = gson.fromJson(post, IAsrResult.class);
            if(!(parse.getStatus().equals("success"))) {
                throw new RuntimeException("parse audio failed");
            }
            String textResult =  parse.getResult();
            result.setStatus(LagiGlobal.ASR_STATUS_SUCCESS);
            result.setResult(textResult);
        } catch (Exception e) {
            result.setStatus(LagiGlobal.ASR_STATUS_FAILURE);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    // TextToAudio
    @Override
    public TTSResult tts(TTSRequestParam param) {
        TTSResult result = new TTSResult();
        try {
            Map<String, Object> bodyObj = new HashMap<>();
            bodyObj.put("emotion", param.getEmotion());
            bodyObj.put("text", param.getText());
            String body = gson.toJson(bodyObj);
            String post = OkHttpUtil.post(getText2Audio(), body);
            ITtsResult apiResult = gson.fromJson(post, ITtsResult.class);
            if(!(apiResult.getStatus().equals("success"))) {
                throw new RuntimeException("Text to voiced voice processing failed");
            }
            String url =  apiResult.getData();
            result.setStatus(LagiGlobal.TTS_STATUS_SUCCESS);
            result.setResult(url);
        } catch (Exception e) {
            result.setStatus(LagiGlobal.TTS_STATUS_FAILURE);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @Data
    static
    class ITtsResult {
        private String status;
        private String data;
    }

    @Data
    static
    class IAsrResult {
        private String status;
        private String result;
    }

    @Data
    static
    class IUploadResult {
        private String status;
        private String filename;
    }


    private String uploadFile(File file) {
        // 创建 OkHttpClient 实例
        OkHttpClient client = new OkHttpClient();

        // 创建 RequestBody，用于封装文件
        RequestBody fileBody = RequestBody.create(MediaType.parse("audio/mpeg"), file);
        // 创建 MultipartBody，用于封装多个部分的数据
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(Headers.of("Content-Disposition", "form-data; name=\"file\"; filename=\"" + file.getName() + "\""),
                        fileBody)
                .build();
        // 创建请求
        Request request = new Request.Builder()
                .url(getUploadUrl())
                .post(requestBody)
                .build();

        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                IUploadResult iUploadResult = gson.fromJson(response.body().string(), IUploadResult.class);
                return iUploadResult.getFilename();
            } else {
                throw new RuntimeException("Upload failed");
            }
        } catch (IOException e) {
            throw new RuntimeException("Upload failed");
        }
    }

    public static void main(String[] args) {
        WhisperAudioAdapter whisperAudioAdapter = new WhisperAudioAdapter();
        whisperAudioAdapter.setEndpoint("http://127.0.0.1:9100");
//        TTSRequestParam ttsRequestParam = new TTSRequestParam();
//        ttsRequestParam.setText("你好");
//        ttsRequestParam.setEmotion("default");
//        TTSResult tts = whisperAudioAdapter.tts(ttsRequestParam);
//        System.out.println(tts);
        AsrResult asr = whisperAudioAdapter.asr(new File("C:\\Users\\Administrator\\Desktop\\asaki.mp3"), null);
        System.out.println(asr);
    }
}
