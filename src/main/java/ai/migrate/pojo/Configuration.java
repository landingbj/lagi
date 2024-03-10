package ai.migrate.pojo;

public class Configuration {
    private String system_title;
    private String default_category;
    private LLM LLM;
    private ASR ASR;
    private TTS TTS;
    private ImageGeneration image_generation;
    private ImageCaptioning image_captioning;
    private ImageEnhance image_enhance;
    private VideoGeneration video_generation;
    private VideoTrack video_track;
    private VideoEnhance video_enhance;

    public ImageCaptioning getImage_captioning() {
        return image_captioning;
    }

    public void setImage_captioning(ImageCaptioning image_captioning) {
        this.image_captioning = image_captioning;
    }

    public ImageEnhance getImage_enhance() {
        return image_enhance;
    }

    public void setImage_enhance(ImageEnhance image_enhance) {
        this.image_enhance = image_enhance;
    }

    public VideoGeneration getVideo_generation() {
        return video_generation;
    }

    public void setVideo_generation(VideoGeneration video_generation) {
        this.video_generation = video_generation;
    }

    public VideoTrack getVideo_track() {
        return video_track;
    }

    public void setVideo_track(VideoTrack video_track) {
        this.video_track = video_track;
    }

    public LLM getLLM() {
        return LLM;
    }

    public void setLLM(LLM LLM) {
        this.LLM = LLM;
    }

    public ASR getASR() {
        return ASR;
    }

    public void setASR(ASR aSR) {
        this.ASR = aSR;
    }

    public TTS getTTS() {
        return TTS;
    }

    public void setTTS(TTS tTS) {
        this.TTS = tTS;
    }

    public ImageGeneration getImage_generation() {
        return image_generation;
    }

    public void setImage_generation(ImageGeneration image_generation) {
        this.image_generation = image_generation;
    }

    public String getSystem_title() {
        return system_title;
    }

    public void setSystem_title(String system_title) {
        this.system_title = system_title;
    }

    public VideoEnhance getVideo_enhance() {
        return video_enhance;
    }

    public void setVideo_enhance(VideoEnhance video_enhance) {
        this.video_enhance = video_enhance;
    }

    public String getDefault_category() {
        return default_category;
    }

    public void setDefault_category(String default_category) {
        this.default_category = default_category;
    }
}
