package ai.lagi.adapter.impl;

import ai.lagi.adapter.ILlmAdapter;
import ai.lagi.pojo.*;
import ai.migrate.pojo.Backend;
import ai.openai.pojo.*;
import ai.utils.qa.ChatCompletionUtil;
import ai.utils.qa.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;

public class ErnieAdapter implements ILlmAdapter {
    private static Gson gson = new Gson();
    private static final String COMPLETIONS_URL = "https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/eb-instant";
    private Backend backendConfig;

    public ErnieAdapter(Backend backendConfig) {
        this.backendConfig = backendConfig;
    }

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient().newBuilder().build();

    private String getAccessToken() throws IOException {
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "grant_type=client_credentials&client_id=" +
                this.backendConfig.getApi_key() + "&client_secret=" + this.backendConfig.getSecret_key());
        Request request = new Request.Builder().url("https://aip.baidubce.com/oauth/2.0/token").method("POST", body).addHeader("Content-Type", "application/x-www-form-urlencoded").build();
        Response response = HTTP_CLIENT.newCall(request).execute();
        return new JSONObject(response.body().string()).getString("access_token");
    }

    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        String bodyJson = gson.toJson(convertRequest(chatCompletionRequest));
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, bodyJson);
        ErnieResponse ernieResponse = null;
        try {
            Request request = new Request.Builder().url(COMPLETIONS_URL + "?access_token=" + getAccessToken()).method("POST", body).addHeader("Content-Type", "application/json").build();
            Response response = HTTP_CLIENT.newCall(request).execute();
            String json = response.body().string();
            ernieResponse = gson.fromJson(json, ErnieResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convertResponse(ernieResponse);
    }

    private ErnieCompletionRequest convertRequest(ChatCompletionRequest request) {
        ErnieCompletionRequest result = new ErnieCompletionRequest();
        List<ErnieMessage> messages = new ArrayList<>();
        for (ChatMessage chatMessage : request.getMessages()) {
            ErnieMessage message = new ErnieMessage();
            message.setRole(chatMessage.getRole());
            message.setContent(chatMessage.getContent());
            messages.add(message);
        }
        result.setMessages(messages);
        result.setTemperature(request.getTemperature());
        return result;
    }

    private ChatCompletionResult convertResponse(ErnieResponse response) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setId(response.getId());
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        result.setModel(this.backendConfig.getModel());
        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setIndex(0);
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(response.getResult());
        chatMessage.setRole("assistant");
        choice.setMessage(chatMessage);
        choice.setFinish_reason("stop");
        List<ChatCompletionChoice> choices = new ArrayList<>();
        choices.add(choice);
        result.setChoices(choices);
        Usage usage = new Usage();
        usage.setCompletion_tokens(response.getUsage().getCompletion_tokens());
        usage.setPrompt_tokens(response.getUsage().getPrompt_tokens());
        usage.setTotal_tokens(response.getUsage().getTotal_tokens());
        result.setUsage(usage);
        return result;
    }
}
