package ai.audio.adapter;

import ai.audio.pojo.AudioTrainStatus;
import ai.audio.pojo.UploadRequest;
import ai.audio.pojo.AudioRequest;

public interface IAudioCloneAdapter {
    void upload(UploadRequest uploadRequest);
    AudioTrainStatus query(AudioRequest AudioRequest);
}
