package ai.common.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Medusa {
    private Boolean enable;
    private String algorithm;
    private Boolean enableL2;
    @JsonProperty("enable_llm_diver")
    private Boolean enableLlmDiver;
    @JsonProperty("enable_tree_diver")
    private Boolean enableTreeDiver;
    @JsonProperty("enable_rag_diver")
    private Boolean enableRagDiver;
    @JsonProperty("enable_page_diver")
    private Boolean enablePageDiver;
    private Long consumeDelay;
    private Long preDelay;
    private Double lcsRatioPromptInput;
    private Double similarityCutoff;
}
