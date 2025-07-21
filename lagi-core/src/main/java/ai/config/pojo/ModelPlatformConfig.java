package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ModelPlatformConfig {
    @JsonProperty("finetune")
    private FineTuneConfig fineTuneConfig;
    @JsonProperty("deploy")
    private DeployConfig deployConfig;
    private Boolean remote;
    private String remoteServiceUrl;
}
