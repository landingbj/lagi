package ai.config.pojo;

import ai.llm.utils.PolicyConstants;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class Policy {
    @JsonProperty("enable_queue_handle")
    private Boolean enableQueueHandle;
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
            @JsonProperty("enable_queue_handle") Boolean enableQueueHandle,
            @JsonProperty("handle") String handle,
            @JsonProperty("grace_time") Integer graceTime,
            @JsonProperty("maxgen") Integer maxGen,
            @JsonProperty("context_length") Integer contextLength
    ) {
        this.enableQueueHandle = enableQueueHandle != null && enableQueueHandle;
        this.handle = handle == null ? PolicyConstants.PARALLEL : handle;
        this.graceTime = graceTime == null ? 3600 : graceTime;
        this.maxGen = maxGen == null ? Integer.MAX_VALUE : maxGen ;
        this.contextLength = contextLength == null ? 4096 : contextLength;
    }
}
