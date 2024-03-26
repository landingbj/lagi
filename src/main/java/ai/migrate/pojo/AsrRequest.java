package ai.migrate.pojo;

public class AsrRequest {
    private String lang;
    private String audioUrl;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    @Override
    public String toString() {
        return "AsrRequest [lang=" + lang + ", audioUrl=" + audioUrl + "]";
    }
}
