package ai.finetune.pojo;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ExportArgs {
    private String modelPath;
    private String adapterPath;
    private String template;
    private String finetuningType;
    private String exportDir;
    private String exportSize;
    private String exportDevice;
    private Boolean exportLegacyFormat;
}
