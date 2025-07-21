package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.InData;
import ai.paas.beidian.pojo.Label;
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
public class ProjectDetailData {
    private String projectId;
    private String projectName;
    private String description;
    private String coverPagePath;
    private String spaceId;
    private int accessType;
    private String accessTypeName;
    private List<ShareSpace> shareSpaces;
    private List<List<Label>> labels;
    private String ownerUserId;
    private String ownerUserDisplayName;
    private String ownerUserEmail;
    private long createTime;
    private long updateTime;
    private int codeType;
    private String codesetId;
    private int codeRepositoryId;
    private CodeRepositoryInfo codeRepositoryInfo;
    private CodesetInfo codesetInfo;
    private JobenvSpecConfig jobenvSpecConfig;
    private JobSpecConfig jobSpecConfig;
    private int jobenvImageId;
    private JobenvImageInfo jobenvImageInfo;
    private int jobImageId;
    private JobImageInfo jobImageInfo;
    private List<InData> datasetInData;
    private List<InData> preInData;
    private int jobenvId;
    private int visualenvId;
    private String inferenceId;
    private int permissions;
    private SaveImageInfo saveImageInfo;
    private PublishProjectInfo publishProjectInfo;
    private CloneProjectInfo cloneProjectInfo;
    private int status;
    private boolean mountCode;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class ShareSpace {
        private String spaceId;
        private String spaceName;
    }


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
        private String createTime;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class CodesetInfo {
        private String codeset_id;
        private String pavo_status;
        private String codesetSize;
        private String sizeSync;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class JobenvSpecConfig {
        private int cpu;
        private int memory;
        private int storage;
        private String gpuType;
        private int gpu;
        private String vgpuType;
        private int vgpu;
        private int vgpuMemory;
        private int vgpuRatio;
        private int specInstanceId;
        private String specInstanceName;
        private String deviceType;
        private String npuTemplate;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class JobSpecConfig {
        private int trainType;
        private List<TaskRole> taskRoles;
        private String runScript;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class TaskRole {
        private String runScript;
        private int instance;
        private int specInstanceId;
        private String specInstanceName;
        private int cpu;
        private int memory;
        private int storage;
        private String gpuType;
        private int gpu;
        private String vgpuType;
        private int vgpu;
        private int vgpuMemory;
        private int vgpuRatio;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class JobenvImageInfo {
        private int imageId;
        private int imageRepositoryId;
        private String imageName;
        private String imageTag;
        private String originalName;
        private String repositoryDisplayName;
        private String imageDesc;
        private long createTime;
        private int createUserId;
        private String createDisplayName;
        private long imageSize;
        private int isOfficial;
        private int repositoryType;
        private List<Label> labels;
        private int layers;
        private int accessType;
        private String accessTypeName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class JobImageInfo {
        private int imageId;
        private int imageRepositoryId;
        private String imageName;
        private String imageTag;
        private String originalName;
        private String repositoryDisplayName;
        private String imageDesc;
        private long createTime;
        private int createUserId;
        private String createDisplayName;
        private long imageSize;
        private int isOfficial;
        private int repositoryType;
        private List<List<Label>> labels;
        private int layers;
        private int accessType;
        private String accessTypeName;
    }




    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class SaveImageInfo {
        private String latestSaveAsImageId;
        private String saveImageStatus;
        private String saveImageMsg;
        private String saveImageRepoName;
        private String saveImageTag;
        private String imageType;
        private String saveImageAction;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class PublishProjectInfo {
        private String projectId;
        private String versionId;
        private String projectName;
        private int status;
        private boolean isFirstPublish;
        private RelateVersion relateVersion;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class RelateVersion {
        private String versionId;
        private int status;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class CloneProjectInfo {
        private String projectId;
        private int protocolId;
        private String protocolName;
    }

}
