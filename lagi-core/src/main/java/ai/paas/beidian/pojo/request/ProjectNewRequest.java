package ai.paas.beidian.pojo.request;

import ai.paas.beidian.pojo.PreModel;
import ai.paas.beidian.pojo.InData;
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
public class ProjectNewRequest {
    private String projectName;
    private String description;
    private String coverPagePath;
    private List<Integer> labelElementIds;
    private Integer accessType;
    private List<String> shareSpaceIds;
    private Integer codeType;
    private CodeRepositoryInfo codeRepositoryInfo;
    private Integer imageSource;
    private Integer imageId;
    private String imageDesc;
    private List<InData> inData;
    private List<PreModel> preModel;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class CodeRepositoryInfo {
        private String provider;
        private String url;
        private String token;
        private String defaultBranch;
        private String codeRepositoryName;
    }



}
