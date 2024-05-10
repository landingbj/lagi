package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WorkerConfig {
    private String name;
    @JsonProperty("class")
    private String workerClass;
    private String agent;
}
