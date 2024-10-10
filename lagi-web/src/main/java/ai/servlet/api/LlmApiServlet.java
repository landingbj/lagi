package ai.servlet.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ai.common.ModelService;
import ai.config.ContextLoader;
import ai.dto.ModelPreferenceDto;
import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingRequest;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.GetRagContext;
import ai.llm.utils.CacheManager;
import ai.llm.utils.CompletionUtil;
import ai.manager.LlmManager;
import ai.medusa.MedusaService;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.llm.service.CompletionsService;
import ai.common.pojo.Configuration;
import ai.common.pojo.IndexSearchData;
import ai.medusa.utils.PromptCacheConfig;
import ai.medusa.utils.PromptCacheTrigger;
import ai.medusa.utils.PromptInputUtil;
import ai.migrate.service.VectorDbService;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import ai.utils.HttpUtil;
import ai.utils.LagiGlobal;
import ai.utils.MigrateGlobal;
import ai.utils.SensitiveWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorCacheLoader;
import ai.worker.meeting.MeetingWorker;
import ai.worker.pojo.AddMeetingRequest;
import ai.worker.pojo.MeetingInfo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LlmApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private static final Configuration config = MigrateGlobal.config;
    private final CompletionsService completionsService = new CompletionsService();
    private final VectorDbService vectorDbService = new VectorDbService(config);
    private final Logger logger = LoggerFactory.getLogger(LlmApiServlet.class);
    private final MedusaService medusaService = new MedusaService();
    private final boolean BASEDONCONTEXT = ContextLoader.configuration.getStores().getRag().get(0).getDependingOnTheContext();
    private final MeetingWorker meetingWorker = new MeetingWorker();

    static {
        VectorCacheLoader.load();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("completions")) {
            this.completions(req, resp);
        } else if (method.equals("embeddings")) {
            this.embeddings(req, resp);
        } else if (method.equals("conversationtApi")) {
            this.conversationtApi(req, resp);
        } else if (method.equals("extractAddMeetingInfo")) {
            this.extractAddMeetingInfo(req, resp);
        }
    }

    private void extractAddMeetingInfo(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        AddMeetingRequest addMeetingRequest = gson.fromJson(requestToJson(req), AddMeetingRequest.class);
        String message = addMeetingRequest.getMessage();
        MeetingInfo currentMeetingInfo = addMeetingRequest.getMeetingInfo();
        MeetingInfo meetingInfo = meetingWorker.extractAddMeetingInfo(message, currentMeetingInfo);
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("meetingInfo", meetingInfo);
        responsePrint(resp, toJson(result));
    }

    public static String removePeopleDescription(String text) {
        // 正则表达式匹配数字后面紧跟着“人”或“个人”
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:人|个人)|(人数)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        // 替换匹配到的部分为空字符串
        return matcher.replaceAll("，会议类型是例会，");
    }

    private void conversationtApi(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        Map<String, String> entity = gson.fromJson(requestToJson(req), Map.class);
        String apiurl = "http://test.digimeta.com.cn:8080/nlt/intent/api/conversation";
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        if (entity.get("msg") != null) {
            String cleanedText = removePeopleDescription(entity.get("msg"));
            entity.put("msg", cleanedText);

//            String[] places = {"东四", "西单", "酒仙桥", "洋桥", "大郊亭", "房山", "顺义", "昌平", "大兴", "平谷", "密云", "延庆", "通州", "海淀北", "德胜门", "兆泰"};
//            for (String place : places) {
//                if (entity.get("msg").contains(place)) {
//                    if (entity.get("entity")!=null){
//                        Map<String, Object> obj = gson.fromJson(entity.get("entity"), Map.class);
//                        obj.put("location", place);
//                        entity.put("entity", gson.toJson(obj));
//                    }
//                  return;
//                }
//            }

        }

        String jsonResult = null;
        try {
            jsonResult = HttpUtil.httpPost(apiurl, headers, entity, 2 * 1000);
        } catch (Exception e) {
            System.out.println(e);
        }
//        Map<String, String> map = null;
//
////        if (jsonResult != null){
//         //   map = gson.fromJson(requestToJson(req), Map.class);
//        //}

        responsePrint(resp, jsonResult);
    }

    private void completions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        ModelPreferenceDto preference = JSONUtil.toBean((String) session.getAttribute("preference"), ModelPreferenceDto.class);
        ChatCompletionRequest chatCompletionRequest = reqBodyToObj(req, ChatCompletionRequest.class);
        if (chatCompletionRequest.getModel() == null
                && preference != null
                && preference.getLlm() != null) {
            chatCompletionRequest.setModel(preference.getLlm());
        }
        ChatCompletionResult chatCompletionResult = null;
        ChatCompletionRequest medusaRequest = getCompletionRequest(chatCompletionRequest);
        PromptInput promptInput = medusaService.getPromptInput(medusaRequest);
        List<IndexSearchData> indexSearchDataList = new ArrayList<>();
        if (LagiGlobal.RAG_ENABLE) {
            indexSearchDataList = vectorDbService.searchByContext(chatCompletionRequest);
        }
        if (BASEDONCONTEXT && indexSearchDataList.size() <= 0) {
            ChatCompletionResult temp = new ChatCompletionResult();
            ChatCompletionChoice choice = new ChatCompletionChoice();
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setContent("您好！ 您的问题内容较为简略，请提供更详细的信息或具体化您的问题，以便我们能更准确地为您提供帮助。");
            choice.setMessage(chatMessage);
            temp.setChoices(Lists.newArrayList(choice));
            responsePrint(resp, toJson(temp));
            return;
        } else {
            chatCompletionResult = medusaService.locate(promptInput);
        }

        if (chatCompletionResult != null) {
            if (chatCompletionRequest.getStream() != null && chatCompletionRequest.getStream()) {
                resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
                out.print("data: " + toJson(chatCompletionResult) + "\n\n");
                out.print("data: " + "[DONE]" + "\n\n");
                out.flush();
                out.close();
            } else {
                responsePrint(resp, toJson(chatCompletionResult));
            }
            logger.info("Cache hit: {}", PromptInputUtil.getNewestPrompt(promptInput));
            return;
        } else {
            medusaService.triggerCachePut(promptInput);
            if (medusaService.getPromptPool() != null) {
                medusaService.getPromptPool().put(PooledPrompt.builder()
                        .promptInput(promptInput).status(PromptCacheConfig.POOL_INITIAL).build());
            }
        }

        boolean hasTruncate = false;
        GetRagContext context = null;
        if (chatCompletionRequest.getCategory() != null && LagiGlobal.RAG_ENABLE) {
            String lastMessage = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
            String answer = VectorCacheLoader.get2L2(lastMessage);
            if (StrUtil.isNotBlank(answer)) {
                ChatCompletionResult temp = new ChatCompletionResult();
                ChatCompletionChoice choice = new ChatCompletionChoice();
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setContent(answer);
                choice.setMessage(chatMessage);
                temp.setChoices(Lists.newArrayList(choice));
                responsePrint(resp, toJson(temp));
                return;
            }

            if (indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
                context = completionsService.getRagContext(indexSearchDataList);
                String contextStr = CompletionUtil.truncate(context.getContext());
                context.setContext(contextStr);
                completionsService.addVectorDBContext(chatCompletionRequest, contextStr);
                ChatMessage chatMessage = chatCompletionRequest.getMessages().get(chatCompletionRequest.getMessages().size() - 1);
                chatCompletionRequest.setMessages(Lists.newArrayList(chatMessage));
                hasTruncate = true;
            }
        } else {
            indexSearchDataList = null;
        }

        if (!hasTruncate) {
            List<ChatMessage> chatMessages = CompletionUtil.truncateChatMessages(chatCompletionRequest.getMessages());
            chatCompletionRequest.setMessages(chatMessages);
        }
        if (chatCompletionRequest.getStream() != null && chatCompletionRequest.getStream()) {
            resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
            streamOutPrint(chatCompletionRequest, context, indexSearchDataList, out, LlmManager.getInstance().getAdapters().size());
        } else {
            ChatCompletionResult result = completionsService.completions(chatCompletionRequest, indexSearchDataList);
            CompletionUtil.populateContext(result, indexSearchDataList, context.getContext());
            responsePrint(resp, toJson(result));
        }
    }

    private static ChatCompletionRequest getCompletionRequest(ChatCompletionRequest chatCompletionRequest) {
        List<Integer> integers = PromptCacheTrigger.analyzeChatBoundariesForIntent(chatCompletionRequest);
        ChatCompletionRequest medusaRequest = null;
        if (!integers.isEmpty()) {
            Integer i = integers.get(0);
            List<ChatMessage> chatMessages = chatCompletionRequest.getMessages().subList(i, chatCompletionRequest.getMessages().size());
            medusaRequest = new ChatCompletionRequest();
            medusaRequest.setTemperature(chatCompletionRequest.getTemperature());
            medusaRequest.setMax_tokens(chatCompletionRequest.getMax_tokens());
            medusaRequest.setCategory(chatCompletionRequest.getCategory());
            medusaRequest.setMessages(chatMessages);
        } else {
            medusaRequest = chatCompletionRequest;
        }
        return medusaRequest;
    }

    private void streamOutPrint(ChatCompletionRequest chatCompletionRequest, GetRagContext context, List<IndexSearchData> indexSearchDataList, PrintWriter out, int limit) {
        if (limit <= 0) {
            out.close();
            return;
        }
        ChatCompletionRequest request = new ChatCompletionRequest();
        BeanUtil.copyProperties(chatCompletionRequest, request);
        ILlmAdapter ragAdapter = completionsService.getRagAdapter(request, indexSearchDataList);
        Observable<ChatCompletionResult> observable = completionsService.streamCompletions(ragAdapter, request);
        final ChatCompletionResult[] lastResult = {null, null};
        int finalLimit = limit - 1;
        observable.subscribe(
                data -> {
                    lastResult[0] = data;
                    ChatCompletionResult filter = SensitiveWordUtil.filter(data);
                    String msg = gson.toJson(filter);
                    out.print("data: " + msg + "\n\n");
                    out.flush();
                    if (lastResult[1] == null) {
                        lastResult[1] = data;
                    } else {
                        for (int i = 0; i < lastResult[1].getChoices().size(); i++) {
                            ChatCompletionChoice choice = lastResult[1].getChoices().get(i);
                            ChatCompletionChoice chunkChoice = data.getChoices().get(i);
                            String chunkContent = chunkChoice.getMessage().getContent();
                            String content = choice.getMessage().getContent();
                            choice.getMessage().setContent(content + chunkContent);
                        }
                    }
                },
                e -> {
                    logger.error("", e);
                    ModelService modelService = (ModelService) ragAdapter;
                    CacheManager.put(modelService.getModel(), false);
                    streamOutPrint(chatCompletionRequest, context, indexSearchDataList, out, finalLimit);
                },
                () -> {
                    if (lastResult[0] == null) {
                        streamOutPrint(chatCompletionRequest, context, indexSearchDataList, out, finalLimit);
                        return;
                    }
                    extracted(lastResult, indexSearchDataList, context, out);
                    lastResult[0].setChoices(lastResult[1].getChoices());
                    out.flush();
                    out.close();
                }
        );
    }

    private void extracted(ChatCompletionResult[] lastResult, List<IndexSearchData> indexSearchDataList, GetRagContext ragContext, PrintWriter out) {
        if (lastResult[0] != null && !lastResult[0].getChoices().isEmpty()
                && indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
            IndexSearchData indexData = indexSearchDataList.get(0);
            List<String> imageList = vectorDbService.getImageFiles(indexData);
            List<String> filePaths = ragContext.getFilePaths().stream().distinct().collect(Collectors.toList());
            List<String> filenames = ragContext.getFilenames().stream().distinct().collect(Collectors.toList());
            for (int i = 0; i < lastResult[0].getChoices().size(); i++) {
                ChatMessage message = lastResult[0].getChoices().get(0).getMessage();
                message.setContent("");
                message.setContext(indexData.getText());
                IndexSearchData indexData1 = indexSearchDataList.get(i);
                if (!(indexData1.getFilename() != null && indexData1.getFilename().size() == 1
                        && indexData1.getFilename().get(0).isEmpty())) {
                    message.setFilename(filenames);
                    message.setFilepath(filePaths);
                }
                message.setImageList(imageList);
            }
            out.print("data: " + gson.toJson(lastResult[0]) + "\n\n");
            out.flush();
        }
        out.print("data: " + "[DONE]" + "\n\n");
        out.flush();
    }

    private void embeddings(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        OpenAIEmbeddingRequest request = gson.fromJson(requestToJson(req), OpenAIEmbeddingRequest.class);
        Embeddings embeddings = EmbeddingFactory.getEmbedding(config.getLLM().getEmbedding());
        List<List<Float>> embeddingDataList = embeddings.createEmbedding(request.getInput());
        Map<String, Object> result = new HashMap<>();
        if (embeddingDataList.isEmpty()) {
            result.put("status", "failed");
        } else {
            result.put("status", "success");
            result.put("data", embeddingDataList);
        }
        responsePrint(resp, toJson(result));
    }
}
