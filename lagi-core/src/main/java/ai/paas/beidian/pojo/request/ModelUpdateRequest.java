package ai.paas.beidian.pojo.request;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ModelUpdateRequest {
    private String modelName;
    private String description;
    private String coverPagePath;
    private List<Integer> labelElementIds;
    private int accessType;
    private List<String> shareSpaceIds;
    private String modelType;
}
