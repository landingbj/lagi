package ai.llm.adapter.impl;

import ai.annotation.LLM;
import ai.common.ModelService;
import ai.common.utils.ObservableList;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.GeminiRequest;
import ai.llm.pojo.GeminiResponse;
import ai.llm.utils.ServerSentEventUtil;
import ai.openai.pojo.*;
import ai.utils.LagiGlobal;
import ai.utils.OkHttpUtil;
import ai.utils.qa.ChatCompletionUtil;
import com.google.gson.Gson;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@LLM(modelNames = {"gemini-1.5-flash-latest","gemini-1.5-flash-latest","gemini-1.0-pro","gemini-1.5-pro-latest"})
public class GeminiAdapter extends ModelService implements ILlmAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GeminiAdapter.class);
    private final Gson gson = new Gson();
    private static final String COMPLETIONS_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent";
    private static final String STEAM_COMPLETIONS_URL = "https://generativelanguage.googleapis.com/v1beta/models/{model}:streamGenerateContent";


    @Override
    public boolean verify() {
        if(getApiKey() == null || getApiKey().startsWith("you")) {
            return false;
        }
        return true;
    }


    @Override
    public ChatCompletionResult completions(ChatCompletionRequest chatCompletionRequest) {
        String url = COMPLETIONS_URL.replace("{model}", getModel());
        Map<String, String> params = new HashMap<>();
        params.put("key", getApiKey());
        String jsonResult = null;
        GeminiRequest geminiRequest = convertRequest(chatCompletionRequest);
        try {
            jsonResult = OkHttpUtil.post(url, params, gson.toJson(geminiRequest));
        } catch (IOException e) {
            logger.error("", e);
        }
        if (jsonResult == null) {
            return null;
        }
        GeminiResponse response = gson.fromJson(jsonResult, GeminiResponse.class);
        if (response == null || response.getCandidates().isEmpty()) {
            return null;
        }
        return convertResponse(response);
    }

    @Override
    public Observable<ChatCompletionResult> streamCompletions(ChatCompletionRequest chatCompletionRequest) {
        String json = gson.toJson(convertRequest(chatCompletionRequest));
        String apiKey = getApiKey();
        String url = STEAM_COMPLETIONS_URL.replace("{model}", getModel()) + "?key=" + apiKey + "&alt=sse";
        Function<String, ChatCompletionResult> convertFunc = e -> {
            GeminiResponse result = gson.fromJson(e, GeminiResponse.class);
            return convertResponse(result);
        };
        ObservableList<ChatCompletionResult> result = ServerSentEventUtil.streamCompletions(json, url, null, convertFunc, this);
        Iterable<ChatCompletionResult> iterable = result.getObservable().blockingIterable();
        return Observable.fromIterable(iterable);
    }

    private GeminiRequest convertRequest(ChatCompletionRequest request) {
        GeminiRequest result = new GeminiRequest();
        List<GeminiRequest.Content> contents = new ArrayList<>();
        for (ChatMessage chatMessage : request.getMessages()) {
            List<GeminiRequest.Part> parts = new ArrayList<>();
            GeminiRequest.Part part = GeminiRequest.Part.builder().text(chatMessage.getContent()).build();
            parts.add(part);
            if (chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_ASSISTANT)) {
                contents.add(GeminiRequest.Content.builder().role("model").parts(parts).build());
            } else if (chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_USER)) {
                contents.add(GeminiRequest.Content.builder().role("user").parts(parts).build());
            } else if (chatMessage.getRole().equals(LagiGlobal.LLM_ROLE_SYSTEM)) {
                GeminiRequest.SystemInstruction systemInstruction = new GeminiRequest.SystemInstruction();
                systemInstruction.setParts(part);
                result.setSystemInstruction(systemInstruction);
            }
        }
        GeminiRequest.GenerationConfig config = new GeminiRequest.GenerationConfig();
        config.setTemperature(request.getTemperature());
        config.setMaxOutputTokens(request.getMax_tokens());
        result.setGenerationConfig(config);
        result.setContents(contents);
        return result;
    }

    private ChatCompletionResult convertResponse(GeminiResponse response) {
        ChatCompletionResult result = new ChatCompletionResult();
        result.setCreated(ChatCompletionUtil.getCurrentUnixTimestamp());
        List<ChatCompletionChoice> choices = new ArrayList<>();
        for (GeminiResponse.Candidate candidate : response.getCandidates()) {
            ChatCompletionChoice choice = new ChatCompletionChoice();
            choice.setIndex(candidate.getIndex());
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent(candidate.getContent().getParts().get(0).getText());
            chatMessage.setRole(LagiGlobal.LLM_ROLE_ASSISTANT);
            choice.setMessage(chatMessage);
            choice.setFinish_reason("stop");
            choices.add(choice);
        }
        result.setChoices(choices);
        Usage usage = new Usage();
        usage.setCompletion_tokens(response.getUsageMetadata().getCandidatesTokenCount());
        usage.setPrompt_tokens(response.getUsageMetadata().getPromptTokenCount());
        usage.setTotal_tokens(response.getUsageMetadata().getTotalTokenCount());
        result.setUsage(usage);
        return result;
    }
}
