package ai.paas.beidian.pojo;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class InstanceInfo {
    private long instanceId;
    private String instanceName;
    private String jobId;
    private long taskroleId;
    private int taskroleIndex;
    private String status;
    private String updateTime;
    private String accessNodeIp;
    private int sshPort;
    private String sshConnection;
    private String terminalUrl;
    private String jupyterUrl;
    private String visualUrl;
}

