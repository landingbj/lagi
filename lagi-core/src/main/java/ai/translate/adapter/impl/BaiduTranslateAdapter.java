package ai.translate.adapter.impl;

import ai.common.ModelService;
import ai.translate.adapter.TranslateAdapter;
import ai.translate.pojo.TranslateResponse;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaiduTranslateAdapter extends ModelService implements TranslateAdapter {

    //    console : https://fanyi-api.baidu.com/api/trans/product/desktop

    private final String API_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";

    private final Logger log = LoggerFactory.getLogger(BaiduTranslateAdapter.class);

    @Override
    public boolean verify() {
        if(getAppId() == null || getAppId().startsWith("you")) {
            return false;
        }
        if(getSecurityKey() == null || getSecurityKey().startsWith("you")) {
            return false;
        }
        return true;
    }


    private Map<String, Object> buildParams(String query, String from, String to) {
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);
        params.put("appid", appId);
        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);
        // 签名
        String src = appId + query + salt + securityKey; // 加密前的原文
        params.put("sign", MD5.create().digestHex(src));

        return params;
    }

    private TranslateResponse getTransResult(String query, String to) {
        Map<String, Object> params = buildParams(query, "auto", to);
        String s = HttpUtil.get(API_URL, params);
        return JSONUtil.toBean(s, TranslateResponse.class);
    }

    @Override
    public String toEnglish(String text) {
        try {
            TranslateResponse en = getTransResult(text, "en");
            return en.getTransResult().get(0).getDst();
        } catch (Exception e) {
            log.error("to english error", e);
        }
        return null ;
    }


    private static String unicodeDecode(String string) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(string);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            string = string.replace(matcher.group(1), ch + "");
        }
        return string;
    }

    @Override
    public String toChinese(String text) {
        try {
            TranslateResponse zh = getTransResult(text, "zh");
            String unicodeChinese = zh.getTransResult().get(0).getDst();
            return unicodeDecode(unicodeChinese);
        } catch (Exception e) {
            log.error("to english error", e);
        }
        return null ;
    }

}
