package ai.paas.beidian.pojo;

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
public class JobStatusDetail {
    private long statusId;
    private String status;
    private String execStatus;
    private String errMsg;
    private long createTime;
    private long updateTime;
    private String instanceName;
    private List<JobStatusDetail> subStatuses; // 支持递归嵌套
}
