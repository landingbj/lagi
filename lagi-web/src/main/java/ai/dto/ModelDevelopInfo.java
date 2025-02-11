package ai.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ModelDevelopInfo {
    private Integer id;
    private String userId;
    private String modelPath;
    private String template;
    private String adapterPath;
    private String finetuningType;
    private String port;
    private Integer running;
}
