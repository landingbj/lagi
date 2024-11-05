package ai.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Driver {
    private String backend;
    private String model;
    private String driver;
    private String oss;
    private String appId;
    private String apiKey;
    private String secretKey;
    private String appKey;
    private String accessKeyId;
    private String accessKeySecret;
    private String accessToken;
    private String securityKey;
    private String endpoint;
    private String apiAddress;
}
