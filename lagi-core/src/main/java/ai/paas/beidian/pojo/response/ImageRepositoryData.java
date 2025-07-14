package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.Label;
import ai.paas.beidian.pojo.ShareSpace;
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
public class ImageRepositoryData {
    private Integer imageRepositoryId;
    private Integer imageId;
    private Integer pavoStatus;
    private String repositoryDisplayName;
    private Integer repositoryType;
    private String description;
    private String spaceId;
    private String spaceName;
    private Integer createUserId;
    private String createDisplayName;
    private String ownerUserEmail;
    private Integer accessType;
    private Integer permissions;
    private Integer isOfficial;
    private Integer imageSize;
    private Long createTime;
    private Long updateTime;
    private String layers;
    private Integer buildType;
    private Integer jobenvId;
    private List<Label> labels;
    private List<ShareSpace> shareSpaces;
}
