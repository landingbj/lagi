package ai.config.pojo;

import lombok.Data;

@Data
public class OSSConfig {
    private String name;
    private String driver;
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private Boolean enable;
}
