package ai.audio.service;

import java.io.File;

import ai.audio.adapter.IAudioAdapter;
import ai.audio.adapter.IAudioCloneAdapter;
import ai.audio.pojo.AudioRequest;
import ai.audio.pojo.AudioTrainStatus;
import ai.audio.pojo.UploadRequest;
import ai.common.ModelService;
import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;
import ai.manager.ASRManager;
import ai.manager.SoundCloneManager;
import ai.manager.TTSManager;
import cn.hutool.core.util.StrUtil;

public class AudioService {


    public AsrResult asr(String audioFilePath, AudioRequestParam audioRequestParam) {
        if(StrUtil.isNotBlank(audioRequestParam.getModel()) && !"default".equals(audioRequestParam.getModel())) {
            IAudioAdapter adapter = ASRManager.getInstance().getAdapter(audioRequestParam.getModel());
            if(adapter != null) {
                return adapter.asr(new File(audioFilePath), audioRequestParam);
            }
        } else {
            for (IAudioAdapter adapter: ASRManager.getInstance().getAdapters()) {
                return adapter.asr(new File(audioFilePath), audioRequestParam);
            }
        }
        return null;
    }

    public TTSResult tts(TTSRequestParam param) {
        if(StrUtil.isNotBlank(param.getModel()) && !"default".equals(param.getModel())) {
            IAudioAdapter adapter = TTSManager.getInstance().getAdapter(param.getModel());
            if(adapter != null) {
                return adapter.tts(param);
            }
        } else {
            for (IAudioAdapter adapter: TTSManager.getInstance().getAdapters()) {
                return adapter.tts(param);
            }
        }
        return null;
    }

    public void train(UploadRequest uploadRequest) {
        if(StrUtil.isNotBlank(uploadRequest.getModel()) && !"default".equals(uploadRequest.getModel())) {
            IAudioCloneAdapter adapter = SoundCloneManager.getInstance().getAdapter(uploadRequest.getModel());
            if(adapter != null) {
                adapter.upload(uploadRequest);
            }
        } else {
            for (IAudioCloneAdapter adapter: SoundCloneManager.getInstance().getAdapters()) {
                adapter.upload(uploadRequest);
                return;
            }
        }
    }

    public AudioTrainStatus query(AudioRequest audioRequest) {
        if(StrUtil.isNotBlank(audioRequest.getModel()) && !"default".equals(audioRequest.getModel())) {
            IAudioCloneAdapter adapter = SoundCloneManager.getInstance().getAdapter(audioRequest.getModel());
            if(adapter != null) {
                return adapter.query(audioRequest);
            }
        }else {
            for (IAudioCloneAdapter adapter : SoundCloneManager.getInstance().getAdapters()) {
                return adapter.query(audioRequest);
            }
        }
        return null;
    }

    public String getOthers(String model) {
        IAudioCloneAdapter adapter = null;
        if(StrUtil.isNotBlank(model) && !"default".equals(model)) {
            adapter = SoundCloneManager.getInstance().getAdapter(model);
        }else {
            if(!SoundCloneManager.getInstance().getAdapters().isEmpty()) {
                adapter = SoundCloneManager.getInstance().getAdapters().get(0);
            }
        }
        if(adapter instanceof ModelService) {
            ModelService modelService = (ModelService) adapter;
            return modelService.getOthers();
        }
        return null;
    }

}
