package ai.dto;


import ai.finetune.pojo.FineTuneArgs;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class TrainConfig {
    private String userId;
    private FineTuneArgs fineTuneArgs;
}
