package ai.oss;

import lombok.Setter;

import java.io.File;

@Setter
public abstract class UniversalOSS {
    protected String endpoint;
    protected String accessKeyId;
    protected String accessKeySecret;
    protected String bucketName;

    public abstract String upload(String objectName, File file);
}
