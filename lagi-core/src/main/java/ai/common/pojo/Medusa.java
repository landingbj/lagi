package ai.common.pojo;

import lombok.Data;

@Data
public class Medusa {
    private Boolean enable;
    private String algorithm;
    private Boolean enableLlmDiver;
    private Boolean enableTreeDiver;
    private Boolean enableRagDiver;
    private Boolean enablePageDiver;
}
