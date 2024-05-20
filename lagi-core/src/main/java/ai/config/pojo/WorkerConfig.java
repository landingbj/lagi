package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WorkerConfig {
    private String name;
    @JsonProperty("driver")
    private String driver;
    private String agent;
}
