package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Policy {
    @JsonProperty("handle")
    private String handle;
    @JsonProperty("grace_time")
    private Integer graceTime;
    @JsonProperty("maxgen")
    private Integer maxGen;
    @JsonProperty("context_length")
    private Integer contextLength;

    @JsonCreator
    public Policy(
            @JsonProperty("handle") String handle,
            @JsonProperty("grace_time") Integer graceTime,
            @JsonProperty("max_gen") Integer maxGen,
            @JsonProperty("context_length") Integer contextLength
    ) {
        this.handle = handle == null ? "parallel" : handle;
        this.graceTime = graceTime == null ? 3600 : graceTime;
        this.maxGen = maxGen == null ? 1 : maxGen ;
        this.contextLength = contextLength == null ? 4096 : contextLength;
    }
}
