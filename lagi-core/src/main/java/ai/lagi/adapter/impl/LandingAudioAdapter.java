package ai.lagi.adapter.impl;

import java.io.File;

import com.google.gson.Gson;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;
import ai.lagi.adapter.IAudioAdapter;
import ai.learning.pojo.Response;
import ai.migrate.pojo.AsrResult;
import ai.migrate.pojo.AudioRequestParam;
import ai.migrate.pojo.TTSRequestParam;
import ai.migrate.pojo.TTSResult;
import ai.migrate.pojo.Text2VoiceEntity;
import ai.migrate.pojo.AsrRequest;
import ai.utils.FileUploadUtil;
import ai.utils.LagiGlobal;

public class LandingAudioAdapter implements IAudioAdapter {
    private Gson gson = new Gson();
    private AiServiceCall call = new AiServiceCall();

    @Override
    public AsrResult asr(File audio, AudioRequestParam param) {
        String url = FileUploadUtil.asrUpload(audio);
        AsrRequest asrRequest = new AsrRequest();
        asrRequest.setLang("Chinese");
        asrRequest.setAudioUrl(url);
        Object[] params = { gson.toJson(asrRequest) };
        String[] result = call.callWS(AiServiceInfo.WSVocUrl, "asr", params);
        Response response = gson.fromJson(result[0], Response.class);
        return toAsrResult(response);
    }

    @Override
    public TTSResult tts(TTSRequestParam param) {
        Text2VoiceEntity text2VoiceEntity = new Text2VoiceEntity();
        text2VoiceEntity.setText(param.getText());
        text2VoiceEntity.setModel("default");
        text2VoiceEntity.setEmotion(param.getEmotion());
        Object[] params = { gson.toJson(text2VoiceEntity) };
        String[] result = call.callWS(AiServiceInfo.WSVocUrl, "text2Voice", params);
        Response response = gson.fromJson(result[0], Response.class);
        return toTTSResult(response);
    }

    private AsrResult toAsrResult(Response response) {
        AsrResult result = new AsrResult();
        if (response.getStatus().equals("success")) {
            result.setStatus(LagiGlobal.ASR_STATUS_SUCCESS);
            result.setResult(response.getData());
        } else {
            result.setStatus(LagiGlobal.ASR_STATUS_FAILURE);
        }
        return result;
    }

    private TTSResult toTTSResult(Response response) {
        TTSResult result = new TTSResult();
        if (response.getStatus().equals("success")) {
            result.setStatus(LagiGlobal.TTS_STATUS_SUCCESS);
            result.setResult(response.getData());
        } else {
            result.setStatus(LagiGlobal.TTS_STATUS_FAILURE);
        }
        return result;
    }
}
