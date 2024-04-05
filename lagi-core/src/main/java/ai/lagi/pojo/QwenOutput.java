package ai.lagi.pojo;

public class QwenOutput {
    public String finish_reason;
    public String text;

    public String getFinish_reason() {
        return finish_reason;
    }

    public void setFinish_reason(String finish_reason) {
        this.finish_reason = finish_reason;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "QwenOutput [finish_reason=" + finish_reason + ", text=" + text + "]";
    }

}
