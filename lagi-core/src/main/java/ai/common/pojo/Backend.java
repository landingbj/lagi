package ai.common.pojo;

import lombok.Data;
import lombok.ToString;

@Data
public class Backend {
    private String name;
    private String driver;
    private String type;
    private Boolean enable;
    private Integer priority;
    private String model;
    private String apiAddress;
    private String appId;
    private String apiKey;
    private String secretKey;
    private String appKey;
    private String accessKeyId;
    private String accessKeySecret;
}
