package ai.audio.adapter.impl;

import ai.audio.adapter.IAudioAdapter;
import ai.audio.service.AlibabaAsrService;
import ai.common.ModelService;
import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.common.pojo.*;
import ai.learning.pojo.Response;
import ai.utils.LagiGlobal;
import com.google.gson.Gson;

import java.io.File;

public class AlibabaAudioAdapter extends ModelService implements IAudioAdapter {
    private final Gson gson = new Gson();
    private final AiServiceCall call = new AiServiceCall();


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
        Text2VoiceEntity text2VoiceEntity = new Text2VoiceEntity();
        text2VoiceEntity.setText(param.getText());
        text2VoiceEntity.setModel("default");
        text2VoiceEntity.setEmotion(param.getEmotion());
        Object[] params = {gson.toJson(text2VoiceEntity)};
        String[] result = call.callWS(AiServiceInfo.WSVocUrl, "text2Voice", params);
        Response response = gson.fromJson(result[0], Response.class);
        return toTTSResult(response);
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
