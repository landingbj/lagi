package ai.audio.service;

import java.io.File;

import ai.audio.adapter.IAudioAdapter;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;
import ai.manager.ASRManager;
import ai.manager.TTSManager;

public class AudioService {


    public AsrResult asr(String audioFilePath, AudioRequestParam audioRequestParam) {
        if(audioRequestParam.getModel() !=null) {
            IAudioAdapter adapter = ASRManager.getInstance().getAdapter(audioRequestParam.getModel());
            if(adapter != null) {
                return adapter.asr(new File(audioFilePath), audioRequestParam);
            }
        }
        for (IAudioAdapter adapter: ASRManager.getInstance().getAdapters()) {
            return adapter.asr(new File(audioFilePath), audioRequestParam);
        }
        return null;
    }

    public TTSResult tts(TTSRequestParam param) {
        if(param.getModel() != null) {
            IAudioAdapter adapter = TTSManager.getInstance().getAdapter(param.getModel());
            if(adapter != null) {
                return adapter.tts(param);
            }
        }
        for (IAudioAdapter adapter: TTSManager.getInstance().getAdapters()) {
            return adapter.tts(param);
        }
        return null;
    }

}
