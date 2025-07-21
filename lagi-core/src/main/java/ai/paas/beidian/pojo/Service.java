package ai.paas.beidian.pojo;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Service {
    private Integer serviceId;
    private String proJobId;
    private Integer proJobenvId;
    private String updateTime;
    private String createTime;
    private String clusterId;
    private Integer status;
    private Integer targetPort;
    private Integer nodePort;
    private String protocol;
    private String serviceName;
    private String remark;
    private String innerIp;
    private String outerIp;
}

