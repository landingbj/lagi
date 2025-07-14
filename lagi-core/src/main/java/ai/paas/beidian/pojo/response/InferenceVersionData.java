package ai.paas.beidian.pojo.response;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 推理服务版本数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InferenceVersionData {

    private String projectId;
    private int inferenceId;
    private String inferenceName;
    private String description;
    private int current_version_id;
    private String current_version_name;
    private int replicas;
    private int listCount;

    private List<InferenceVersionItem> list;

    /**
     * 推理服务版本列表项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InferenceVersionItem {
        private String inferenceId;
        private int versionId;
        private String versionName;
        private int runningReplicas;
        private String usedDisplayGpu;

        private List<TaskRoleInfo> taskroleList;

        private int imageId;
        private String imageDesc;
        private boolean mountCode;

        private List<DatasetInData> datasetInData;
        private PreInData preInData;

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
    }

    /**
     * 任务角色信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskRoleInfo {
        private String runScript;
        private int instance;
        private int specInstanceId;
        private SpecInstanceInfo specInstanceInfo;
    }

    /**
     * 规格实例信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecInstanceInfo {
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
        private double price;
        private String specSeries;
    }

    /**
     * 数据集输入信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatasetInData {
        private int dataType;
        private String dataId;
        private String dataDesc;
        private String dataPath;
        private String dataBucket;
        private int dataSize;
        private String usePath;
        private String envName;
        private String version;
        private String versionId;

        private List<AZInfo> azList;
    }

    /**
     * 预输入数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreInData {
        private int dataType;
        private String dataId;
        private String dataDesc;
        private String dataPath;
        private String dataBucket;
        private int dataSize;
        private String usePath;
        private String envName;
        private String version;
        private String versionId;

        private List<AZInfo> azList;
    }

    /**
     * 区域信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AZInfo {
        private String clusterId;
        private String clusterName;
        private String azId;
        private String azName;
        private String azShowColor;
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

        private List<LabelElement> labelslabels;
    }

    /**
     * 标签元素
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabelElement {
        private int labelElementId;
        private String labelElementName;
    }
}
