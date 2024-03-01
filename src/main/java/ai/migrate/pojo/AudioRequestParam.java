package ai.migrate.pojo;

public class AudioRequestParam {
    private String appkey;
    private String format;
    private Integer sample_rate;
    private String vocabulary_id;
    private String customization_id;
    private Boolean enable_punctuation_prediction;
    private Boolean enable_inverse_text_normalization;
    private Boolean enable_voice_detection;
    private Boolean disfluency;
    private String audio_address;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getSample_rate() {
        return sample_rate;
    }

    public void setSample_rate(Integer sample_rate) {
        this.sample_rate = sample_rate;
    }

    public String getVocabulary_id() {
        return vocabulary_id;
    }

    public void setVocabulary_id(String vocabulary_id) {
        this.vocabulary_id = vocabulary_id;
    }

    public String getCustomization_id() {
        return customization_id;
    }

    public void setCustomization_id(String customization_id) {
        this.customization_id = customization_id;
    }

    public Boolean getEnable_punctuation_prediction() {
        return enable_punctuation_prediction;
    }

    public void setEnable_punctuation_prediction(Boolean enable_punctuation_prediction) {
        this.enable_punctuation_prediction = enable_punctuation_prediction;
    }

    public Boolean getEnable_inverse_text_normalization() {
        return enable_inverse_text_normalization;
    }

    public void setEnable_inverse_text_normalization(Boolean enable_inverse_text_normalization) {
        this.enable_inverse_text_normalization = enable_inverse_text_normalization;
    }

    public Boolean getEnable_voice_detection() {
        return enable_voice_detection;
    }

    public void setEnable_voice_detection(Boolean enable_voice_detection) {
        this.enable_voice_detection = enable_voice_detection;
    }

    public Boolean getDisfluency() {
        return disfluency;
    }

    public void setDisfluency(Boolean disfluency) {
        this.disfluency = disfluency;
    }

    public String getAudio_address() {
        return audio_address;
    }

    public void setAudio_address(String audio_address) {
        this.audio_address = audio_address;
    }
}
