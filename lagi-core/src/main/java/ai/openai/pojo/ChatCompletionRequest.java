package ai.openai.pojo;

import ai.common.pojo.KnowledgeBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
@Data
@EqualsAndHashCode
public class ChatCompletionRequest implements Serializable {
    private String sessionId;
    private String model;
    private double temperature;
    private Integer max_tokens;
    private String category;
    private List<ChatMessage> messages;
    private Boolean stream;
    private List<Tool> tools;
    private String tool_choice;
    private Boolean parallel_tool_calls;
    private Double presence_penalty;
    private Double frequency_penalty;
    private Double top_p;
    private KnowledgeBase knowledgeBase;
}
