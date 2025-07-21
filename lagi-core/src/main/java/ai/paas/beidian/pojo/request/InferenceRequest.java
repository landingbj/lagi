package ai.paas.beidian.pojo.request;

import ai.paas.beidian.pojo.InData;
import ai.paas.beidian.pojo.Service;
import ai.paas.beidian.pojo.TaskRole;
import ai.paas.beidian.pojo.TaskRoleInfo;
import lombok.*;

import java.util.List;

/**
 * 推理升级请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InferenceRequest {
    private String projectId;
    private String description;
    private Long imageId;
    private Integer replicas;
    private Boolean mountCode;
    private List<TaskRoleInfo> taskroles;
    private List<InData> datasetInData;
    private List<InData> preInData;
    private List<Service> services;
//    private String jobenvName;
//    private int imageId;
//    private int codeId;
//    private String codeBranch;
//    private String codeTag;
//    private List<InData> inData;
//    private int cpu;
//    private long memory;
//    private long storage;
//    private List<Tool> tools;
//    private Service services;
//    private String vgpuType;
//    private int gpu;
//    private String description;
//    private int vgpuMemory;
//    private int vgpu;
//    private String gpuType;
//    private int codeType;
//    private int vgpuRatio;
//
//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class InData {
//        private int dataType;
//        private int dataId;
//        private String dataDesc;
//        private String dataBucket;
//        private String dataPath;
//    }
//
//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class Tool {
//        private String name;
//        private Config config;
//    }
//
//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class Config {
//        private String password;
//        private String pubkey_id;
//    }
//

}
