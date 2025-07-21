package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DeployConfig {

    @JsonProperty("llama_factory")
    private LlamaFactoryConfig llamaFactoryConfig;

    // 1. llamafactory  2. beidian
    @JsonProperty("platform")
    private String platformName;

    @JsonProperty("beidian")
    private BeiDianPaasConfig beiDianPaasConfig;
}
