package ai.openai.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
@Data
public class ChatCompletionResult implements Serializable {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<ChatCompletionChoice> choices;
    private Usage usage;
    private String system_fingerprint;
}
