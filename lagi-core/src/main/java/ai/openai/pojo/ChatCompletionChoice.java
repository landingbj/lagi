package ai.openai.pojo;

import lombok.Data;

@Data
public class ChatCompletionChoice {
    private int index;
    private ChatMessage message;
    private ChatMessage delta;
    private String finish_reason;
}
