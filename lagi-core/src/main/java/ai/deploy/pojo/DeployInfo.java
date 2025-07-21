package ai.deploy.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DeployInfo {
    private Integer id;
    private Integer type;
    private String userId;
    private String modelId;
    private String modelPath;
    private String template;
    private String adapterPath;
    private String finetuningType;
    private String port;

    private String inferenceId;
    private String versionId;
    private String apiAddress;
    private Integer running;
    private Integer replicas;
}
