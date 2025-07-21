package ai.paas.beidian.pojo.response;

import ai.paas.beidian.pojo.JobInfo;
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
public class ProjectListData {
    private int listCount;
    private List<JobInfo> jobList;
}
