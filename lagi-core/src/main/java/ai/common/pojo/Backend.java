package ai.common.pojo;

import ai.config.pojo.OSSConfig;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class Backend {
    private String backend;
    private String name;
    private List<Driver> drivers;
    private String type;
    private Boolean enable;
    private Integer priority;
    private String model;
    private String driver;
    private String apiAddress;
    private String appId;
    private String apiKey;
    private String secretKey;
    private String appKey;
    private String accessKeyId;
    private String accessKeySecret;
    private String securityKey;
    private Boolean stream;
    private OSSConfig oss;
}
