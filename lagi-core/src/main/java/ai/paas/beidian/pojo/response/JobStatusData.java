package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.JobPrepareInfo;
import ai.paas.beidian.pojo.JobStatusDetail;
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
public class JobStatusData {
    private String jobId;
    private String status;
    private List<JobStatusDetail> jobStatuses;
    private List<JobPrepareInfo> jobPrepareInfo;
}
