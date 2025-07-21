package ai.paas.beidian.pojo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JobInfo {
    private String jobId;
    private String jobName;
    private int trainType;
    private String description;
    private int globalPriority;
    private String createTime;
    private String callTime;
    private String startTime;
    private String endTime;
    private String status;
    private String subStatus;
    private String statusMsg;
    private String errMsg;
    private int outputId;
    private String outputName;
    private int outputSize;
    private int taskroleCnt;
    private int statInstance;
    private int statCpu;
    private int statMemory;
    private int statStorage;
    private int statGpu;
    private int statVgpu;
    private int statVgpuMemory;
    private int statVgpuRatio;
    private String updateTime;
    private int createUserId;
    private String userName;
    private String displayName;
    private int permissions;
}