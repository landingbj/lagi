package ai.deploy.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DeployRequest {
    private Integer id;
    private String userId;
    private String model;
    private String adapterPath;
    private String finetuningType;
    private String port;
}
