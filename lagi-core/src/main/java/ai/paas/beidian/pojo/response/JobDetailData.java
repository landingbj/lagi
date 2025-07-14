package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.Cache;
import ai.paas.beidian.pojo.ImageDetailInfo;
import ai.paas.beidian.pojo.TaskRoleInfo;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JobDetailData {
    private String projectId;
    private String projectName;
    private String jobId;
    private String jobName;
    private int trainType;
    private String description;
    private int globalPriority;
    private long imageId;
    private String imageDesc;
    private long codeId;
    private String codeDesc;
    private int codeType;
    private String codeRepositoryUrl;
    private String codeBranch;
    private String codeTag;
    private String codeCommit;
    private List<Map<String, Object>> dataInData;
    private Map<String, Object> preInData;
    private long createTime;
    private long callTime;
    private long startTime;
    private long endTime;
    private int maxRunHour;
    private String status;
    private String subStatus;
    private String statusMsg;
    private String errMsg;
    private long outputId;
    private String outputName;
    private long outputSize;
    private int sizeSync;
    private int outputPavoStatus;
    private int taskroleCnt;
    private int statInstance;
    private int statCpu;
    private int statMemory;
    private int statStorage;
    private int statGpu;
    private int statVgpu;
    private int statVgpuMemory;
    private int statVgpuRatio;
    private long updateTime;
    private String spaceId;
    private String spaceName;
    private long createUserId;
    private String createUserName;
    private String CreateDisplayName;
    private int permissions;

    private List<TaskRoleInfo> taskroleList;
    private int isCache;
    private Cache cache;
    private ImageDetailInfo imageDetailInfo;
    private int usedDisplayGpu;
}
