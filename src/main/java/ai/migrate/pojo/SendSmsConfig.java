package ai.migrate.pojo;

public class SendSmsConfig {
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;
    private String smsEndpoint;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getSmsEndpoint() {
        return smsEndpoint;
    }

    public void setSmsEndpoint(String smsEndpoint) {
        this.smsEndpoint = smsEndpoint;
    }

    @Override
    public String toString() {
        return "SendSmsConfig [accessKeyId=" + accessKeyId + ", accessKeySecret=" + accessKeySecret + ", signName="
                + signName + ", templateCode=" + templateCode + ", smsEndpoint=" + smsEndpoint + "]";
    }
}
