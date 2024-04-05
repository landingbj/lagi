package ai.lagi.pojo;

import java.util.List;

public class QwenInput {
    private String prompt;
    private List<QwenMessage> messages;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<QwenMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<QwenMessage> messages) {
        this.messages = messages;
    }
}
