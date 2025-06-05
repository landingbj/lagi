package ai.audio.utils.xfyun.sign;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SignatureException;

public abstract class AbstractSignature {
    /**
     * 签名ID
     */
    private String id;

    /**
     * 加密key
     */
    private String key;

    /**
     * 服务url
     */
    private String url;

    /**
     * 加密算法
     */
    private String encryptType;

    /**
     * 待加密原始字符
     */
    private String originSign;

    /**
     * 最终生成的签名
     */
    protected String signa;

    /**
     * 时间戳timestamp
     */
    private String ts;

    /**
     * 请求类型，默认get
     */
    protected String requestMethod = "GET";

    /**
     * @param id
     * @param key
     * @param url
     */
    public AbstractSignature(String id, String key, String url) {
        this.id = id;
        this.key = key;
        this.url = url;
        this.ts = generateTs();
    }

    /**
     * 可设置请求类型
     * @param id
     * @param key
     * @param url
     * @param isPost 是否为POST
     */
    public AbstractSignature(String id, String key, String url, boolean isPost) {
        this.id = id;
        this.key = key;
        this.url = url;
        if (isPost) {
            this.requestMethod = "POST";
        }else{
            this.requestMethod = "GET";
        }

        this.ts = generateTs();
    }

    /**
     * 生成ts时间
     */
    public String generateTs() {
        return String.valueOf(System.currentTimeMillis() / 1000L);
    }


    /**
     * 完成签名，返回完整签名
     *
     * @return
     * @throws SignatureException
     */
    public abstract String getSigna() throws SignatureException;

    public String generateOriginSign() throws SignatureException {
        try {
            URL url = new URL(this.getUrl());

            return "host: " + url.getHost() + "\n" +
                    "date: " + this.getTs() + "\n" +
                    "GET " + url.getPath() + " HTTP/1.1";
        } catch (MalformedURLException e) {
            throw new SignatureException("MalformedURLException:" + e.getMessage());
        }
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOriginSign() {
        return originSign;
    }

    public void setOriginSign(String originSign) {
        this.originSign = originSign;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(String encryptType) {
        this.encryptType = encryptType;
    }
}
