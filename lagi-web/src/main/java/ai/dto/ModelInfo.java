package ai.dto;

import lombok.*;

import java.io.Serializable;

@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ModelInfo implements Serializable {
    private String model;
    private Boolean enabled;
    private String company;
    private String description;
    private Boolean activate;
}
