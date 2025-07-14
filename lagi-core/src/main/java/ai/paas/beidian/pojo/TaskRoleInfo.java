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
public class TaskRoleInfo {
    private long taskroleId;
    private String jobId;
    private String taskroleName;
    private String runScript;
    private int instance;
    private String specInstanceId;
    private SpecInstanceInfo specInstanceInfo;
    private List<InstanceInfo> instanceList;
}
