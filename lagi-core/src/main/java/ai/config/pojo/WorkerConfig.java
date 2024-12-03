package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WorkerConfig {
    private String name;
    @JsonProperty("worker")
    private String worker;
    private String agent;
    private String agents;
}
