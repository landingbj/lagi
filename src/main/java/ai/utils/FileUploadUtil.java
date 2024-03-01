package ai.utils;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FileUploadUtil {
    public static String generateRandomFileName(String extension) {
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + extension;
    }

    public static String asrUpload(File file) {
        return uploadMinio(file, "asr", "audio/*");
    }
    
    public static String imageCaptioningUpload(File file) {
        return uploadMinio(file, "sam", "image/*");
    }
    
    public static String enhanceImageUpload(File file) {
        return uploadMinio(file, "realesrgan", "image/*");
    }
    
    public static String svdUpload(File file) {
        return uploadMinio(file, "svd", "image/*");
    }
    
    public static String mmtrackingUpload(File file) {
        return uploadMinio(file, "mmtracking", "video/*");
    }

    public static String mmeditingUpload(File file) {
        return uploadMinio(file, "mmediting", "video/*");
    }

    public static String uploadMinio(File file, String bucketName, String contentType) {
        String url = null;
        try {
            MinioClient minioClient = MinioClient.builder().endpoint("http://116.255.226.214:9000")
                    .credentials("9OMV1OGIpDH29iDq1HWC", "Riy5PYX1dJvEp2PrlOkI6r8GFvvuBxUUVkrQ9fR3").build();

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String objectName = file.getName();
            String localFilePath = file.getAbsolutePath();

            minioClient.uploadObject(UploadObjectArgs.builder().bucket(bucketName).object(objectName)
                    .filename(localFilePath).build());

            Map<String, String> reqParams = new HashMap<String, String>();
            reqParams.put("response-content-type", contentType);

            url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET)
                    .bucket(bucketName).object(objectName).expiry(120, TimeUnit.HOURS).extraQueryParams(reqParams)
                    .build());
        } catch (MinioException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException
                | IOException e) {
            e.printStackTrace();
        }
        return url;
    }
}
