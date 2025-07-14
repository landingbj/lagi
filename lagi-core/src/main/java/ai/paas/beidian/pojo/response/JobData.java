package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.*;
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
public class JobData {
    private String projectId;
    private String projectName;
    private String jobName;
    private String description;
    private int trainType;
    private long imageId; // 根据实际类型选择 long 或 Integer
    private String imageDesc;
    private List<CodeAz> codeAzList;
    private List<DatasetInData> datasetInData;
    private PreInData preInData;
    private List<TaskRole> taskroles;
    private int isCache;
    private Cache cache;
    private List<JobCloneError> jobCloneErrors;
    private int maxRunHour;
    private boolean mountCode;
}
