package ai.audio.adapter.impl;

import ai.annotation.ASR;
import ai.annotation.TTS;
import ai.audio.adapter.IAudioAdapter;
import ai.audio.service.AlibabaAsrService;
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
        if(getAppKey() == null || getAppKey().startsWith("you")) {
            return false;
        }
        if(getAccessKeyId() == null || getAccessKeyId().startsWith("you")) {
            return false;
        }
        if(getAccessKeySecret() == null || getAccessKeySecret().startsWith("you")) {
            return false;
        }
        return true;
    }

    @Override
    public AsrResult asr(File audio, AudioRequestParam param) {
        AlibabaAsrService asrService = new AlibabaAsrService(
                getAppKey(),
                getAccessKeyId(),
                getAccessKeySecret()
        );
        return gson.fromJson(asrService.asr(audio), AsrResult.class);
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
