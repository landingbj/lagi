package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AgentConfig {
    private String name;
    @JsonProperty("class")
    private String agentClass;
    private String apiKey;
}
