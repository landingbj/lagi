package ai.audio.service;

import ai.utils.OkHttpUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AlibabaAudioService {
    private static final Cache<String, String> cache;
    private static final int CACHE_SIZE = 100;
    private static final long EXPIRE_SECONDS = 60 * 60 * 24;

    private static final String TIME_ZONE = "GMT";
    private static final String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String URL_ENCODING = "UTF-8";
    private static final String ALGORITHM_NAME = "HmacSHA1";
    private static final String ENCODING = "UTF-8";

    private final String accessKeyId;
    private final String accessKeySecret;

    private final Gson gson = new Gson();

    static {
        cache = initCache();
    }

    public AlibabaAudioService(String accessKeyId, String accessKeySecret) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
    }

    private String getISO8601Time() {
        Date nowDate = new Date();
        SimpleDateFormat df = new SimpleDateFormat(FORMAT_ISO8601);
        df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));
        return df.format(nowDate);
    }

    private String getUniqueNonce() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    private String percentEncode(String value) throws UnsupportedEncodingException {
        return value != null ? URLEncoder.encode(value, URL_ENCODING).replace("+", "%20")
                .replace("*", "%2A").replace("%7E", "~") : null;
    }

    private String canonicalizedQuery(Map<String, String> queryParamsMap) {
        String[] sortedKeys = queryParamsMap.keySet().toArray(new String[]{});
        Arrays.sort(sortedKeys);
        String queryString = null;
        try {
            StringBuilder canonicalizedQueryString = new StringBuilder();
            for (String key : sortedKeys) {
                canonicalizedQueryString.append("&")
                        .append(percentEncode(key)).append("=")
                        .append(percentEncode(queryParamsMap.get(key)));
            }
            queryString = canonicalizedQueryString.toString().substring(1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return queryString;
    }

    private String createStringToSign(String method, String urlPath, String queryString) {
        String stringToSign = null;
        try {
            StringBuilder strBuilderSign = new StringBuilder();
            strBuilderSign.append(method);
            strBuilderSign.append("&");
            strBuilderSign.append(percentEncode(urlPath));
            strBuilderSign.append("&");
            strBuilderSign.append(percentEncode(queryString));
            stringToSign = strBuilderSign.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return stringToSign;
    }

    private String sign(String stringToSign, String accessKeySecret) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM_NAME);
            mac.init(new SecretKeySpec(
                    accessKeySecret.getBytes(ENCODING),
                    ALGORITHM_NAME
            ));
            byte[] signData = mac.doFinal(stringToSign.getBytes(ENCODING));
            String signBase64 = DatatypeConverter.printBase64Binary(signData);
            return percentEncode(signBase64);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    private String processGETRequest(String queryString) {
        String token = null;
        String url = "http://nls-meta.cn-shanghai.aliyuncs.com";
        url = url + "/";
        url = url + "?" + queryString;
        try {
            String result = OkHttpUtil.get(url);
            JsonObject rootObj = gson.fromJson(result, JsonObject.class);
            JsonObject tokenObj = rootObj.getAsJsonObject("Token");
            if (tokenObj != null) {
                token = tokenObj.get("Id").getAsString();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return token;
    }

    String getToken() {
        Map<String, String> queryParamsMap = new HashMap<String, String>();
        queryParamsMap.put("AccessKeyId", accessKeyId);
        queryParamsMap.put("Action", "CreateToken");
        queryParamsMap.put("Version", "2019-02-28");
        queryParamsMap.put("Timestamp", getISO8601Time());
        queryParamsMap.put("Format", "JSON");
        queryParamsMap.put("RegionId", "cn-shanghai");
        queryParamsMap.put("SignatureMethod", "HMAC-SHA1");
        queryParamsMap.put("SignatureVersion", "1.0");
        queryParamsMap.put("SignatureNonce", getUniqueNonce());
        String queryString = canonicalizedQuery(queryParamsMap);
        if (null == queryString) {
            return null;
        }
        String method = "GET";
        String urlPath = "/";
        String stringToSign = createStringToSign(method, urlPath, queryString);
        if (null == stringToSign) {
            return null;
        }
        String signature = sign(stringToSign, accessKeySecret + "&");
        if (null == signature) {
            return null;
        }
        String queryStringWithSign = "Signature=" + signature + "&" + queryString;
        return processGETRequest(queryStringWithSign);
    }

    private static Cache<String, String> initCache() {
        return CacheBuilder.newBuilder().maximumSize(AlibabaAudioService.CACHE_SIZE)
                .expireAfterWrite(AlibabaAudioService.EXPIRE_SECONDS, TimeUnit.SECONDS).build();
    }

    public Cache<String, String> getCache() {
        return cache;
    }
}