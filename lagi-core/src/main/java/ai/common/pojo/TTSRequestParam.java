package ai.common.pojo;

public class TTSRequestParam {
    private String appkey;
    private String text;
    private String token;
    private String format;
    private Integer sample_rate;
    private String voice;
    private Integer volume;
    private Integer speech_rate;
    private Integer pitch_rate;

    private String emotion;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public Integer getSpeech_rate() {
        return speech_rate;
    }

    public void setSpeech_rate(Integer speech_rate) {
        this.speech_rate = speech_rate;
    }

    public Integer getPitch_rate() {
        return pitch_rate;
    }

    public void setPitch_rate(Integer pitch_rate) {
        this.pitch_rate = pitch_rate;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }
}
