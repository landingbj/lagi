package ai.audio.adapter;

import java.io.File;

import ai.common.pojo.AsrResult;
import ai.common.pojo.AudioRequestParam;
import ai.common.pojo.TTSRequestParam;
import ai.common.pojo.TTSResult;

public interface IAudioAdapter {
    AsrResult asr(File audio, AudioRequestParam param);

    TTSResult tts(TTSRequestParam param);
}
