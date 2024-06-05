package ai.audio.service;

import java.io.File;

import ai.audio.adapter.IAudioAdapter;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;
import ai.managers.ASRManager;
import ai.managers.TTSManager;

public class AudioService {


    public AsrResult asr(String audioFilePath, AudioRequestParam audioRequestParam) {
        for (IAudioAdapter adapter: ASRManager.getInstance().getAdapters()) {
            return adapter.asr(new File(audioFilePath), audioRequestParam);
        }
        return null;
    }

    public TTSResult tts(TTSRequestParam param) {
        for (IAudioAdapter adapter: TTSManager.getInstance().getAdapters()) {
            return adapter.tts(param);
        }
        return null;
    }

}
