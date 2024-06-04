package ai.common;


import lombok.Data;
import lombok.Getter;

@Data
public class ModelService {

    protected String appId;
    protected String apiKey;
    protected String secretKey;
    protected String appKey;
    protected String accessKeyId;
    protected String accessKeySecret;
    protected Integer priority;
    protected String model;
    protected String type;

}
