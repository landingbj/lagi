package ai.paas.beidian.pojo;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ImageDetailInfo {
    private long imageRepositoryId;
    private long imageId;
    private String libraStatus;
    private String repositoryDisplayName;
    private String spaceId;
    private String spaceName;
    private long createUserId;
    private String userName;
    private String displayName;
    private int accessType;
    private String createTime;
    private int permissions;
    private int isOfficial;
    private long imageSize;
    private String labelFrame;
    private String labelPython;
    private String labelOs;
    private String labelCuda;
    private String labelCalculateType;
}
