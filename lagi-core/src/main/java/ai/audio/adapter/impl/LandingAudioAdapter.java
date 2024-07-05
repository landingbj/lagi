package ai.audio.adapter.impl;

import java.io.File;

import ai.annotation.ASR;
import ai.annotation.TTS;
import ai.audio.adapter.IAudioAdapter;
import ai.common.ModelService;
import ai.common.pojo.*;
import ai.oss.UniversalOSS;
import com.google.gson.Gson;

import ai.common.client.AiServiceCall;
import ai.common.client.AiServiceInfo;
import ai.learning.pojo.Response;
import ai.utils.LagiGlobal;

@ASR(company = "landingbj", modelNames = "landing-asr")
@TTS(company = "landingbj", modelNames = "landing-tts")
public class LandingAudioAdapter extends ModelService implements IAudioAdapter {
    private Gson gson = new Gson();
    private AiServiceCall call = new AiServiceCall();

    private UniversalOSS universalOSS;


    @Override
    public AsrResult asr(File audio, AudioRequestParam param) {
        String url = universalOSS.upload("asr/" + audio.getName(), audio);
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
