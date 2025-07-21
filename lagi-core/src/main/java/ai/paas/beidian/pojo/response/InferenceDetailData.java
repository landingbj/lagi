package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.InData;
import ai.paas.beidian.pojo.Label;
import ai.paas.beidian.pojo.Service;
import ai.paas.beidian.pojo.TaskRoleInfo;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推理服务详情数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceDetailData {

    private String projectId;
    private List<InData> datasetInData;
    private List<InData> preInData;
    private boolean mountCode;
    private Long imageId;
    private String description;
    private Integer replicas;
    private List<Service> services;
    private List<TaskRoleInfo> taskroleList;


    private String projectName;
    private String inferenceId;
    private String inferenceName;
    private Integer currentVersionId;
    private String currentVersionName;
    private int runningReplicas;

    private List<ReplicaInfo> replicasList;

    private String imageDesc;


    private String spaceId;
    private String spaceName;
    private int createUserId;
    private String createUserName;
    private String createDisplayName;
    private long createTime;
    private long updateTime;
    private int permissions;
    private String status;


    private ImageDetailInfo imageDetailInfo;

    private long startTime;
    private long endTime;

    /**
     * 副本信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReplicaInfo {
        private String replicaId;
        private String replicaName;
        private String status;
        private String terminalUrl;
        private long createTime;
        private long startTime;
        private long endTime;
        private String errMsg;
    }





    /**
     * 镜像详细信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageDetailInfo {
        private int imageRepositoryId;
        private int imageId;
        private String libraStatus;
        private String repositoryDisplayName;
        private String description;
        private String spaceId;
        private String spaceName;
        private int createUserId;
        private String createUserName;
        private String createUserDisplayName;
        private long createTime;
        private long updateTime;
        private int isOfficial;
        private Long imageSize;
        private int layers;

        private List<Label> labelslabels;
    }

}
