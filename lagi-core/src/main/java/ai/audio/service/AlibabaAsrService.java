package ai.audio.service;

import ai.utils.OkHttpUtil;

import java.io.File;
import java.util.*;

public class AlibabaAsrService extends AlibabaAudioService{
    private final String appKey;

    public AlibabaAsrService(String appKey, String accessKeyId, String accessKeySecret) {
        super(accessKeyId, accessKeySecret);
        this.appKey = appKey;
    }

    public String process(String fileName, String format, int sampleRate,
                          boolean enablePunctuationPrediction,
                          boolean enableInverseTextNormalization,
                          boolean enableVoiceDetection) {
        String request = "https://nls-gateway-cn-shanghai.aliyuncs.com/stream/v1/asr";
        request = request + "?appkey=" + appKey;
        request = request + "&format=" + format;
        request = request + "&sample_rate=" + sampleRate;
        if (enablePunctuationPrediction) {
            request = request + "&enable_punctuation_prediction=" + true;
        }
        if (enableInverseTextNormalization) {
            request = request + "&enable_inverse_text_normalization=" + true;
        }
        if (enableVoiceDetection) {
            request = request + "&enable_voice_detection=" + true;
        }
        HashMap<String, String> headers = new HashMap<>();
        String token = getCache().getIfPresent("token");
        if (token == null) {
            token = getToken();
            if (token != null) {
                getCache().put("token", token);
            }
        }
        headers.put("X-NLS-Token", token);
        headers.put("Content-Type", "application/octet-stream");

        return OkHttpUtil.postFile(request, headers, fileName);
    }

    public String asr(File file) {
        String filePath = file.getAbsolutePath();
        String[] strs = file.getName().split("\\.");
        String format = strs[strs.length - 1];
        int sampleRate = 16000;
        boolean enablePunctuationPrediction = true;
        boolean enableInverseTextNormalization = true;
        boolean enableVoiceDetection = false;
        return process(filePath, format, sampleRate, enablePunctuationPrediction, enableInverseTextNormalization, enableVoiceDetection);
    }
}