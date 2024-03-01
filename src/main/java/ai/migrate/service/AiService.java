package ai.migrate.service;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;
import ai.migrate.pojo.Txt2imgRequest;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.utils.HttpServiceCall;
import ai.utils.MigrateGlobal;

public class AiService {
    private static String TXT2IMG_URL = MigrateGlobal.STABLE_DIFFUSION_URL + "/txt2img";
    private static String SD_GET_IMAGE_URL = MigrateGlobal.STABLE_DIFFUSION_URL + "/get_image?image_path=";
    private Gson gson = new Gson();
    private AiServiceCall wsCall = new AiServiceCall();

    public String generateImage(Txt2imgRequest txt2imgRequest) throws IOException {
        String result = HttpServiceCall.httpPost(TXT2IMG_URL, gson.toJson(txt2imgRequest));

        JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
        if (jsonObject.get("status").getAsString().equals("success")) {
            jsonObject.addProperty("result", SD_GET_IMAGE_URL + jsonObject.get("result").getAsString());
            result = gson.toJson(jsonObject);
        }

        return result;
    }

    public ChatCompletionResult gptCompletions(ChatCompletionRequest chatCompletionRequest) throws IOException {
        Object[] params = { gson.toJson(chatCompletionRequest) };
        String returnStr = wsCall.callWS(AiServiceInfo.WSKngUrl, "gptCompletions", params)[0];
        ChatCompletionResult result = gson.fromJson(returnStr, ChatCompletionResult.class);
        return result;
    }
}
