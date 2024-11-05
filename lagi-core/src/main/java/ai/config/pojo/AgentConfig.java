package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AgentConfig {
    private String name;
    @JsonProperty("driver")
    private String driver;
    private String apiKey;
    private String token;
    private String appId;
}
