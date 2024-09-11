package ai.audio.adapter.impl;

import ai.annotation.ASR;
import ai.annotation.TTS;
import ai.audio.adapter.IAudioAdapter;
import ai.audio.pojo.FileTansAsrResult;
import ai.audio.service.AlibabaAsrService;
import ai.audio.service.AlibabaFileTransAsrService;
import ai.audio.service.AlibabaTtsService;
import ai.common.ModelService;
import ai.common.pojo.*;
import ai.oss.UniversalOSS;
import ai.utils.FileUploadUtil;
import ai.utils.LagiGlobal;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.File;
import java.io.FileOutputStream;

@ASR(company = "alibaba", modelNames = "asr")
@TTS(company = "alibaba", modelNames = "tts")
public class AlibabaAudioAdapter extends ModelService implements IAudioAdapter {
    private final Gson gson = new Gson();
    private UniversalOSS universalOSS;

    @Override
    public boolean verify() {
        if (getAppKey() == null || getAppKey().startsWith("you")) {
            return false;
        }
        if (getAccessKeyId() == null || getAccessKeyId().startsWith("you")) {
            return false;
        }
        if (getAccessKeySecret() == null || getAccessKeySecret().startsWith("you")) {
            return false;
        }
        return true;
    }

    @Override
    public AsrResult asr(File audio, AudioRequestParam param) {
        return fileTansAsr(audio);
    }

    public AsrResult fileTansAsr(File audio) {
        AlibabaFileTransAsrService demo = new AlibabaFileTransAsrService(
                getAccessKeyId(),
                getAccessKeySecret(),
                getAppKey());

        String url = universalOSS.upload("asr/" + audio.getName(), audio);
        String taskId = demo.submitFileTransRequest(url);
        if (taskId != null) {
            System.out.println("录音文件识别请求成功，task_id: " + taskId);
        } else {
            System.out.println("录音文件识别请求失败！");
            return null;
        }
        // 第二步：根据任务ID轮询识别结果。
        String result = demo.getFileTransResult(taskId);
        if (result != null) {
            System.out.println("录音文件识别结果查询成功：" + result);
        } else {
            System.out.println("录音文件识别结果查询失败！");
        }
        FileTansAsrResult fileTansAsrResult = gson.fromJson(result, FileTansAsrResult.class);
        StringBuilder sb = new StringBuilder();
        for (FileTansAsrResult.Sentence sentence : fileTansAsrResult.getSentences()) {
            sb.append(sentence.getText());
        }
        return AsrResult.builder()
                .status(LagiGlobal.ASR_STATUS_SUCCESS)
                .result(sb.toString())
                .build();
    }

    @Override
    public TTSResult tts(TTSRequestParam param) {
        AlibabaTtsService ttsService = new AlibabaTtsService(
                getAppKey(),
                getAccessKeyId(),
                getAccessKeySecret()
        );
        param.setSample_rate(16000);
        param.setFormat("wav");
        Request request = ttsService.getRequest(param);
        TTSResult result = new TTSResult();
        try {
            OkHttpClient client = new OkHttpClient();
            okhttp3.Response response = client.newCall(request).execute();
            String contentType = response.header("Content-Type");
            if ("audio/mpeg".equals(contentType)) {
                String tempDir = System.getProperty("java.io.tmpdir");
                String tempFile = tempDir + FileUploadUtil.generateRandomFileName("wav");
                File audio = new File(tempFile);
                FileOutputStream fout = new FileOutputStream(audio);
                fout.write(response.body().bytes());
                fout.close();
                String url = universalOSS.upload("tts/" + audio.getName(), audio);
                audio.delete();
                result.setStatus(LagiGlobal.TTS_STATUS_SUCCESS);
                result.setResult(url);
            } else {
                String errorMessage = response.body().string();
                result = gson.fromJson(errorMessage, TTSResult.class);
            }
            response.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
