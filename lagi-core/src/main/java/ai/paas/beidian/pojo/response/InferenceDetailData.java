package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.InData;
import ai.paas.beidian.pojo.Label;
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
    private String projectName;
    private int inferenceId;
    private String inferenceName;
    private String description;
    private int currentVersionId;
    private String currentVersionName;
    private int replicas;
    private int runningReplicas;

    private List<ReplicaInfo> replicasList;
    private List<TaskRoleInfo> taskroleList;

    private int imageId;
    private String imageDesc;
    private boolean mountCode;

    private List<InData> datasetInData;
    private InData preInData;

    private String spaceId;
    private String spaceName;
    private int createUserId;
    private String createUserName;
    private String createDisplayName;
    private long createTime;
    private long updateTime;
    private int permissions;
    private String status;

    private List<ServiceInfo> services;

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
     * 服务信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo {
        private int serviceId;
        private String proJobId;
        private int proJobenvId;
        private String updateTime;
        private String createTime;
        private String clusterId;
        private int status;
        private int targetPort;
        private int nodePort;
        private String protocol;
        private String serviceName;
        private String remark;
        private String innerIp;
        private String outerIp;
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
        private int imageSize;
        private int layers;

        private List<Label> labelslabels;
    }

}
