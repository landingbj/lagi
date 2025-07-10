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
public class ImageRepositoryInfoUpdateRequest {
    private String repositoryDisplayName;
    private String description;
    private Integer accessType;
    private List<Integer> labelElementIds;
    private List<String> shareSpaceIds;
}
