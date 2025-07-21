package ai.paas.beidian.pojo;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PreModel {
    private String modelId;
    private String version;
    private String versionId;
}