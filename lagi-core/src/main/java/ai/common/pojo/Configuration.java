package ai.common.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration {
    private String systemTitle;
    @JsonProperty("LLM")
    private LLM LLM;
    private VectorStoreConfig vectorStore;
    @JsonProperty("ASR")
    private ASR ASR;
    @JsonProperty("TTS")
    private TTS TTS;
    private ImageGeneration imageGeneration;
    private ImageCaptioning imageCaptioning;
    private ImageEnhance imageEnhance;
    private VideoGeneration videoGeneration;
    private VideoTrack videoTrack;
    private VideoEnhance videoEnhance;

    public String getSystemTitle() {
        return systemTitle;
    }

    public void setSystemTitle(String systemTitle) {
        this.systemTitle = systemTitle;
    }

    public ai.common.pojo.LLM getLLM() {
        return LLM;
    }

    public void setLLM(ai.common.pojo.LLM LLM) {
        this.LLM = LLM;
    }

    public VectorStoreConfig getVectorStore() {
        return vectorStore;
    }

    public void setVectorStore(VectorStoreConfig vectorStore) {
        this.vectorStore = vectorStore;
    }

    public ai.common.pojo.ASR getASR() {
        return ASR;
    }

    public void setASR(ai.common.pojo.ASR ASR) {
        this.ASR = ASR;
    }

    public ai.common.pojo.TTS getTTS() {
        return TTS;
    }

    public void setTTS(ai.common.pojo.TTS TTS) {
        this.TTS = TTS;
    }

    public ImageGeneration getImageGeneration() {
        return imageGeneration;
    }

    public void setImageGeneration(ImageGeneration imageGeneration) {
        this.imageGeneration = imageGeneration;
    }

    public ImageCaptioning getImageCaptioning() {
        return imageCaptioning;
    }

    public void setImageCaptioning(ImageCaptioning imageCaptioning) {
        this.imageCaptioning = imageCaptioning;
    }

    public ImageEnhance getImageEnhance() {
        return imageEnhance;
    }

    public void setImageEnhance(ImageEnhance imageEnhance) {
        this.imageEnhance = imageEnhance;
    }

    public VideoGeneration getVideoGeneration() {
        return videoGeneration;
    }

    public void setVideoGeneration(VideoGeneration videoGeneration) {
        this.videoGeneration = videoGeneration;
    }

    public VideoTrack getVideoTrack() {
        return videoTrack;
    }

    public void setVideoTrack(VideoTrack videoTrack) {
        this.videoTrack = videoTrack;
    }

    public VideoEnhance getVideoEnhance() {
        return videoEnhance;
    }

    public void setVideoEnhance(VideoEnhance videoEnhance) {
        this.videoEnhance = videoEnhance;
    }
}
