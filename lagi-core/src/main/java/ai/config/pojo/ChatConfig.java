package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class ChatConfig {
    Integer contextLength;
    String policy;
    Integer freezeTime;

    @JsonCreator
    public ChatConfig(
            @JsonProperty("context_length") Integer contextLength,
            @JsonProperty("policy") String policy,
            @JsonProperty("freeze_time") Integer freezeTime
    ) {
        this.contextLength = contextLength == null ? 4096 : contextLength;
        this.policy = policy == null ? "parallel" : policy;
        this.freezeTime = freezeTime == null ? 7200 : freezeTime;
    }
}
