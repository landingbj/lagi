package ai.oss.impl;

import ai.oss.UniversalOSS;
import cn.hutool.core.io.FileUtil;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LandingOSS extends UniversalOSS {

    protected String endpoint = "http://lagi.saasai.top:9000";

    protected String accessKeyId = "9OMV1OGIpDH29iDq1HWC";

    protected String accessKeySecret = "Riy5PYX1dJvEp2PrlOkI6r8GFvvuBxUUVkrQ9fR3";

    protected String bucketName = "lagi";

    private String uploadMinio(File file, String objectName, String bucketName, String contentType) {
        String url = null;
        try {
            MinioClient minioClient = MinioClient.builder().endpoint(endpoint)
                    .credentials(accessKeyId, accessKeySecret).build();

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            minioClient.uploadObject(UploadObjectArgs.builder().bucket(bucketName).object(objectName)
                    .filename(file.getAbsolutePath()).build());

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

    private String getContentType(File file) {
        Map<String, String> map = new HashMap<>();
        map.put(".323", "text/h323");
        map.put(".acp", "audio/x-mei-aac");
        map.put(".aif", "audio/aiff");
        map.put(".aifc", "audio/aiff");
        map.put(".aiff", "audio/aiff");
        map.put(".asa", "text/asa");
        map.put(".asf", "video/x-ms-asf");
        map.put(".asp", "text/asp");
        map.put(".asx", "video/x-ms-asf");
        map.put(".au", "audio/basic");
        map.put(".avi", "video/avi");
        map.put(".biz", "text/xml");
        map.put(".cml", "text/xml");
        map.put(".css", "text/css");
        map.put(".dcd", "text/xml");
        map.put(".dtd", "text/xml");
        map.put(".ent", "text/xml");
        map.put(".fax", "image/fax");
        map.put(".fo", "text/xml");
        map.put(".gif", "image/gif");
        map.put(".htc", "text/x-component");
        map.put(".htm", "text/html");
        map.put(".html", "text/html");
        map.put(".htt", "text/webviewhtml");
        map.put(".htx", "text/html");
        map.put(".ico", "image/x-icon");
        map.put(".IVF", "video/x-ivf");
        map.put(".jfif", "image/jpeg");
        map.put(".jpe", "image/jpeg");
        map.put(".jpeg", "image/jpeg");
        map.put(".jpg", "image/jpeg");
        map.put(".jsp", "text/html");
        map.put(".la1", "audio/x-liquid-file");
        map.put(".lavs", "audio/x-liquid-secure");
        map.put(".lmsff", "audio/x-la-lms");
        map.put(".m1v", "video/x-mpeg");
        map.put(".m2v", "video/x-mpeg");
        map.put(".m3u", "audio/mpegurl");
        map.put(".m4e", "video/mpeg4");
        map.put(".math", "text/xml");
        map.put(".mid", "audio/mid");
        map.put(".midi", "audio/mid");
        map.put(".mml", "text/xml");
        map.put(".mnd", "audio/x-musicnet-download");
        map.put(".mns", "audio/x-musicnet-stream");
        map.put(".movie", "video/x-sgi-movie");
        map.put(".mp1", "audio/mp1");
        map.put(".mp2", "audio/mp2");
        map.put(".mp2v", "video/mpeg");
        map.put(".mp3", "audio/mp3");
        map.put(".mp4", "video/mp4");
        map.put(".mpa", "video/x-mpg");
        map.put(".mpe", "video/x-mpeg");
        map.put(".mpeg", "video/mpg");
        map.put(".mpg", "video/mpg");
        map.put(".mpga", "audio/rn-mpeg");
        map.put(".mps", "video/x-mpeg");
        map.put(".mpv", "video/mpg");
        map.put(".mpv2", "video/mpeg");
        map.put(".mtx", "text/xml");
        map.put(".net", "image/pnetvue");
        map.put(".odc", "text/x-ms-odc");
        map.put(".plg", "text/html");
        map.put(".pls", "audio/scpls");
        map.put(".png", "image/png");
        map.put(".r3t", "text/vnd.rn-realtext3d");
        map.put(".ra", "audio/vnd.rn-realaudio");
        map.put(".ram", "audio/x-pn-realaudio");
        map.put(".rdf", "text/xml");
        map.put(".rmi", "audio/mid");
        map.put(".rmm", "audio/x-pn-realaudio");
        map.put(".rp", "image/vnd.rn-realpix");
        map.put(".rpm", "audio/x-pn-realaudio-plugin");
        map.put(".rt", "text/vnd.rn-realtext");
        map.put(".rv", "video/vnd.rn-realvideo");
        map.put(".snd", "audio/basic");
        map.put(".sol", "text/plain");
        map.put(".sor", "text/plain");
        map.put(".spp", "text/xml");
        map.put(".stm", "text/html");
        map.put(".svg", "text/xml");
        map.put(".tif", "image/tiff");
        map.put(".tiff", "image/tiff");
        map.put(".tld", "text/xml");
        map.put(".tsd", "text/xml");
        map.put(".txt", "text/plain");
        map.put(".uls", "text/iuls");
        map.put(".vcf", "text/x-vcard");
        map.put(".vml", "text/xml");
        map.put(".vxml", "text/xml");
        map.put(".wav", "audio/wav");
        map.put(".wax", "audio/x-ms-wax");
        map.put(".wbmp", "image/vnd.wap.wbmp");
        map.put(".wm", "video/x-ms-wm");
        map.put(".wma", "audio/x-ms-wma");
        map.put(".wml", "text/vnd.wap.wml");
        map.put(".wmv", "video/x-ms-wmv");
        map.put(".wmx", "video/x-ms-wmx");
        map.put(".wsc", "text/scriptlet");
        map.put(".wsdl", "text/xml");
        map.put(".wvx", "video/x-ms-wvx");
        map.put(".xdr", "text/xml");
        map.put(".xhtml", "text/html");
        map.put(".xml", "text/xml");
        map.put(".xpl", "audio/scpls");
        map.put(".xq", "text/xml");
        map.put(".xql", "text/xml");
        map.put(".xquery", "text/xml");
        map.put(".xsd", "text/xml");
        map.put(".xsl", "text/xml");
        map.put(".xslt", "text/xml");
        return map.getOrDefault("."+ FileUtil.getType(file), "application/octet-stream");
    }

    @Override
    public String upload(String objectName, File file) {
        return uploadMinio(file, objectName, bucketName, getContentType(file));
    }

}
