package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfig {
    private String name;
    @JsonProperty("driver")
    private String driver;
    private String apiKey;
    private String token;
    private String appId;
    private String userId;
    private String wrongCase;
    private String endpoint;
    private String mcps;
}
