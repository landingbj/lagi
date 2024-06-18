package ai.audio.adapter.impl;

import ai.audio.adapter.IAudioAdapter;
import ai.audio.service.AlibabaAsrService;
import ai.audio.service.AlibabaTtsService;
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
        AlibabaTtsService ttsService = new AlibabaTtsService(
                getAppKey(),
                getAccessKeyId(),
                getAccessKeySecret()
        );
        ttsService.tts("你好");
        return null;
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
