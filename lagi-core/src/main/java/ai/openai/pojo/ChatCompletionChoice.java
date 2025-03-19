package ai.openai.pojo;

public class ChatCompletionChoice {
    private int index;
    private ChatMessage message;
    private ChatMessage delta;
    private String finish_reason;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ChatMessage getMessage() {
        return message;
    }

    public void setMessage(ChatMessage message) {
        this.message = message;
    }

    public String getFinish_reason() {
        return finish_reason;
    }

    public void setFinish_reason(String finish_reason) {
        this.finish_reason = finish_reason;
    }

    public ChatMessage getDelta() {
        return delta;
    }

    public void setDelta(ChatMessage delta) {
        this.delta = delta;
    }

    @Override
    public String toString() {
        return "ChatCompletionChoice [index=" + index + ", message=" + message + ", delta=" + delta
                + ", finish_reason=" + finish_reason + "]";
    }
}
