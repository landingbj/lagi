package ai.migrate.service;

import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.utils.LagiGlobal;
import ai.utils.WhisperResponse;
import ai.worker.audio.FlightAudio;
import com.google.gson.Gson;


public class AudioService {
    private final Gson gson = new Gson();
    private final ai.audio.service.AudioService audioService = new ai.audio.service.AudioService();

    public String getVoiceResult(String resPath) {
        AudioRequestParam audioRequestParam = new AudioRequestParam();
        AsrResult result = audioService.asr(resPath, audioRequestParam);
        if (result.getStatus() == LagiGlobal.ASR_STATUS_SUCCESS) {
            String text = result.getResult();
            text = FlightAudio.correctFlightNumber(text);
            if (text != null) {
                return gson.toJson(new WhisperResponse(0, text));
            }
        }
        return gson.toJson(new WhisperResponse(1, "识别失败"));
    }
}
