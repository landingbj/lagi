package ai.oss.impl;

import ai.oss.UniversalOSS;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Date;

public class AlibabaOSS extends UniversalOSS {

    private final Logger log = LoggerFactory.getLogger(AlibabaOSS.class);

    protected String endpoint = "https://oss-cn-shanghai.aliyuncs.com";

    private  com.aliyun.oss.OSS buildCredentialsProvider(String accessKeyId , String  secretAccessKey) {
        DefaultCredentialProvider defaultCredentialProvider = CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, secretAccessKey);
        return new OSSClientBuilder().build(endpoint, defaultCredentialProvider);
    };

    public  boolean upload(String accessKeyId , String  secretAccessKey, String bucketName, String objectName , File file) {
        OSS ossClient = buildCredentialsProvider(accessKeyId, secretAccessKey);
        try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, file);
            ossClient.putObject(putObjectRequest);
            return true;
        } catch (OSSException oe) {
            log.error("upload    Error Message {} , Error Code:{} , Request ID:{} , Host ID:{}", oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe.getHostId());
        } catch (ClientException ce) {
            log.error("upload    Error Message {}", ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return false;
    }

    public  String getObjUrl(String accessKeyId , String  secretAccessKey, String bucketName, String objectName) {
        OSS ossClient = buildCredentialsProvider(accessKeyId, secretAccessKey);
        try {
            // 创建PutObjectRequest对象。
            Date expiration = new Date(new Date().getTime() + 3600 * 1000L);
            URL url = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
            return url.toString();
        } catch (OSSException oe) {
            log.error("getObjUrl  Error Message {} , Error Code:{} , Request ID:{} , Host ID:{}", oe.getErrorMessage(), oe.getErrorCode(), oe.getRequestId(), oe.getHostId());
        } catch (ClientException ce) {
            log.error("getObjUrl Error Message:{}", ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }

    private String uploadAndGetUrl(String accessKeyId , String  secretAccessKey, String bucketName, String objectName , File file) {
        boolean upload = upload(accessKeyId, secretAccessKey, bucketName, objectName, file);
        if(upload) {
            return getObjUrl(accessKeyId, secretAccessKey, bucketName, objectName);
        }
        return null;
    }

    @Override
    public String upload(String objectName, File file) {
        return uploadAndGetUrl(accessKeyId, accessKeySecret, bucketName, objectName, file);
    }
}
