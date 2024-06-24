package ai.common.pojo;

import ai.config.pojo.OSSConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Driver {
    private String model;
    private String driver;
    private OSSConfig oss;

    private String appId;
    private String apiKey;
    private String secretKey;
    private String appKey;
    private String accessKeyId;
    private String accessKeySecret;
    private String securityKey;
}
