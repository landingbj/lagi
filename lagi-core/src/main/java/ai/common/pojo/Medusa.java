package ai.common.pojo;

import lombok.Data;

@Data
public class Medusa {
    private Boolean enable;
    private String algorithm;
    private Boolean enableL2;
    private Boolean enableLlmDiver;
    private Boolean enableTreeDiver;
    private Boolean enableRagDiver;
    private Boolean enablePageDiver;
    private Long consumeDelay;
    private Long preDelay;
    private Double lcsRatioPromptInput;
    private Double similarityCutoff;
}
