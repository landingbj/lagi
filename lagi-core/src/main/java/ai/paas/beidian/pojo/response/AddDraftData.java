package ai.paas.beidian.pojo.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddDraftData {
    private String baseVersion;
    private String baseVersionId;
    private String modelId;
}
