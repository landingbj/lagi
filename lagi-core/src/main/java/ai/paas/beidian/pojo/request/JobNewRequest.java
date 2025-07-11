package ai.paas.beidian.pojo.request;

import ai.paas.beidian.pojo.Cache;
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
public class JobNewRequest {
    private String projectId;
    private String projectName;
    private Integer trainType;
    private long imageId;
    private String description;
    private List<InData> datasetInData;
    private InData preInData;
    private List<TaskRole> taskroles;
    private Integer maxRunHour;
    private Integer isCache;
    private Cache cache;
    private Long benefitId;
    private Boolean isUseBenefit;
    private Boolean mountCode;

    private int envType;
    private String jobName;
    private String spaceId;

    private long codeId;
    private List<InData> inData;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class InData {
        private int dataType;
        private long dataId;
        private String dataPath;
        private String dataBucket;
        private String version;
        private String versionId;
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
        private int cpu;
        private int memory;
        private int storage;
        private int gpu;
        private String gpuType;
        private int vgpu;
        private String vgpuType;
        private boolean is_cache;
        private Cache cache;
    }

//    @Data
//    @Builder
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @ToString
//    public static class Cache {
//        private int mem;
//        private int local;
//    }

}
