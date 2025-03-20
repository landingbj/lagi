package ai.common.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Medusa {
    private Boolean enable;
    private String algorithm;
    private Boolean enableL2;
    private Boolean enableReasonDiver;
    private Long consumeDelay;
    private Long preDelay;
    private Double lcsRatioPromptInput;
    private Double similarityCutoff;
    @JsonProperty("reason_model")
    private String reasonModel;
    private String inits;
    @JsonProperty("producer_thread_num")
    private Integer producerThreadNum;
    @JsonProperty("consumer_thread_num")
    private Integer consumerThreadNum;
}
