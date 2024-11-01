package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import lombok.Value;

@ToString
@Data
@Value
public class ChatConfig {
    @JsonProperty("context_length")
    Integer contextLength = 4096;
    String policy = "parallel";
    @JsonProperty("freeze_time")
    Integer freezeTime = 7200;
}
