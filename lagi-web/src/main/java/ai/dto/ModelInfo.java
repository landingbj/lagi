package ai.dto;

import lombok.*;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ModelInfo {
    private String model;
    private Boolean enabled;
    private String company;
    private String description;
}
