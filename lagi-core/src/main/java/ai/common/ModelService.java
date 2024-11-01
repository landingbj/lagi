package ai.common;


import lombok.Data;
@Data
public class ModelService implements ModelVerify{

    protected String appId;
    protected String backend;
    protected String apiKey;
    protected String secretKey;
    protected String appKey;
    protected String accessKeyId;
    protected String accessKeySecret;
    protected Integer priority;
    protected String model;
    protected String type;
    protected String apiAddress;
    protected String endpoint;
    protected String deployment;
    protected String apiVersion;
    protected String securityKey;
    protected String accessToken;
    private String others;
    protected String alias;
    protected Boolean enable;
    protected String router;

    @Override
    public boolean verify() {
        if(getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        return true;
    }

}
