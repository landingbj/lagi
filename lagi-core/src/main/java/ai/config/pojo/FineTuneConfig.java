package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class FineTuneConfig {

    // local llamafactory
    @JsonProperty("llama_factory")
    private LlamaFactoryConfig llamaFactoryConfig;

    // 1. llamafactory  2. beidian
    @JsonProperty("platform")
    private String platformName;

    // beidian pass
    @JsonProperty("beidian_paas")
    private BeiDianPaasConfig beiDianPaasConfig;
}
