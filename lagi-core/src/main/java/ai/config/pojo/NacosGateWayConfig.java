package ai.config.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class NacosGateWayConfig {
    private Boolean enable;
    @JsonProperty("local_ip")
    private String localIp;
    @JsonProperty("local_port")
    private Integer localPort;
    @JsonProperty("gateway_ip")
    private String gatewayIp;
    @JsonProperty("gateway_port")
    private Integer gatewayPort;
    @JsonProperty("service_name")
    private String serviceName;
}
