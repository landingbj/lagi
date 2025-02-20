package ai.dto;

import ai.finetune.pojo.ExportArgs;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ExportConfig {
    private String userId;
    private ExportArgs exportArgs;
}
