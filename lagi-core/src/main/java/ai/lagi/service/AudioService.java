package ai.lagi.service;

import java.io.File;

import ai.lagi.adapter.IAudioAdapter;
import ai.lagi.adapter.impl.LandingAudioAdapter;
import ai.migrate.pojo.ASR;
import ai.migrate.pojo.AsrResult;
import ai.migrate.pojo.AudioRequestParam;
import ai.migrate.pojo.Backend;
import ai.migrate.pojo.Configuration;
import ai.migrate.pojo.TTS;
import ai.migrate.pojo.TTSRequestParam;
import ai.migrate.pojo.TTSResult;
import ai.utils.LagiGlobal;

public class AudioService {
    private Configuration config;

    public AudioService(Configuration config) {
        this.config = config;
    }

    public AsrResult asr(String audioFilePath, AudioRequestParam audioRequestParam) {
        IAudioAdapter audioAdapter = getAudioAdapter(config.getASR());
        AsrResult result = audioAdapter.asr(new File(audioFilePath), audioRequestParam);
        return result;
    }

    public TTSResult tts(TTSRequestParam param) {
        IAudioAdapter audioAdapter = getAudioAdapter(config.getTTS());
        TTSResult result = audioAdapter.tts(param);
        return result;
    }

    private IAudioAdapter getAudioAdapter(ASR asrConfig) {
        IAudioAdapter adapter = null;
        int maxPriority = Integer.MIN_VALUE;
        for (Backend backend : asrConfig.getBackends()) {
            if (backend.getEnable() && backend.getPriority() > maxPriority) {
                adapter = getAudioAdapter(backend.getType());
            }
        }
        return adapter;
    }

    private IAudioAdapter getAudioAdapter(TTS ttsConfig) {
        IAudioAdapter adapter = null;
        int maxPriority = Integer.MIN_VALUE;
        for (Backend backend : ttsConfig.getBackends()) {
            if (backend.getEnable() && backend.getPriority() > maxPriority) {
                adapter = getAudioAdapter(backend.getType());
            }
        }
        return adapter;
    }

    private IAudioAdapter getAudioAdapter(String type) {
        IAudioAdapter adapter = null;
        if (type.equals(LagiGlobal.AUDIO_TYPE_LANDING)) {
            adapter = new LandingAudioAdapter();
        } else if (type.equals(LagiGlobal.AUDIO_TYPE_ALIBABA)) {
        }
        return adapter;
    }
}
