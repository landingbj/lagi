package ai.lagi.adapter;

import java.io.File;

import ai.migrate.pojo.AsrResult;
import ai.migrate.pojo.AudioRequestParam;
import ai.migrate.pojo.TTSRequestParam;
import ai.migrate.pojo.TTSResult;

public interface IAudioAdapter {
    AsrResult asr(File audio, AudioRequestParam param);

    TTSResult tts(TTSRequestParam param);
}
