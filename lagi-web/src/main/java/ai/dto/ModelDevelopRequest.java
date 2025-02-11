package ai.dto;

import lombok.Data;

@Data
public class ModelDevelopRequest {
    private String modelPath;
    private String adapterPath;
    private String template;
    private String finetuningType;
    private String port;
}
