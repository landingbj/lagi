package ai.audio.service;

import ai.common.pojo.TTSRequestParam;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

public class AlibabaTtsService extends AlibabaAudioService {
    private final String appKey;
    private final Gson gson = new Gson();

    public AlibabaTtsService(String appKey, String accessKeyId, String accessKeySecret) {
        super(accessKeyId, accessKeySecret);
        this.appKey = appKey;
    }

    public Request getRequest(TTSRequestParam param) {
        String token = getCache().getIfPresent("token");
        if (token == null) {
            token = getToken();
            if (token != null) {
                getCache().put("token", token);
            }
        }
        String text = param.getText();
        if (param.getEmotion() != null) {
            text = String.format("<speak  voice=\"zhitian_emo\">" +
                    "<emotion category=\"%s\" intensity=\"1.0\">%s</emotion>" +
                    "</speak>", param.getEmotion(), text);
        }
        String format = param.getFormat();
        int sampleRate = param.getSample_rate();
        String voice = param.getVoice();
        String url = "https://nls-gateway-cn-shanghai.aliyuncs.com/stream/v1/tts";
        JsonObject taskObject = new JsonObject();
        taskObject.addProperty("appkey", appKey);
        taskObject.addProperty("token", token);
        taskObject.addProperty("text", text);
        taskObject.addProperty("format", format);
        taskObject.addProperty("voice", voice);
        taskObject.addProperty("sample_rate", sampleRate);

        String bodyContent = gson.toJson(taskObject);
        RequestBody reqBody = RequestBody.create(MediaType.parse("application/json"), bodyContent);
        return new Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(reqBody)
                .build();
    }
}