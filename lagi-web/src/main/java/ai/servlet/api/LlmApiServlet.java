package ai.servlet.api;

import ai.common.ModelService;
import ai.common.pojo.Configuration;
import ai.common.pojo.EnhanceChatCompletionRequest;
import ai.common.pojo.IndexSearchData;
import ai.common.pojo.Medusa;
import ai.config.ContextLoader;
import ai.config.pojo.RAGFunction;
import ai.dto.ModelPreferenceDto;
import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingRequest;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.pojo.GetRagContext;
import ai.llm.service.CompletionsService;
import ai.llm.service.LlmRouterDispatcher;
import ai.llm.utils.CacheManager;
import ai.llm.utils.CompletionUtil;
import ai.manager.LlmManager;
import ai.medusa.MedusaService;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.utils.PromptCacheConfig;
import ai.medusa.utils.PromptCacheTrigger;
import ai.medusa.utils.PromptInputUtil;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.response.ChatMessageResponse;
import ai.servlet.BaseServlet;
import ai.sevice.PdfPreviewServlce;
import ai.utils.HttpUtil;
import ai.utils.MigrateGlobal;
import ai.utils.MinimumEditDistance;
import ai.utils.SensitiveWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorCacheLoader;
import ai.vector.VectorDbService;
import ai.worker.meeting.MeetingWorker;
import ai.worker.pojo.AddMeetingRequest;
import ai.worker.pojo.MeetingInfo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LlmApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private static final Configuration config = MigrateGlobal.config;
    private final CompletionsService completionsService = new CompletionsService();
    private final VectorDbService vectorDbService = new VectorDbService(config);
    private final Logger logger = LoggerFactory.getLogger(LlmApiServlet.class);
    private final MedusaService medusaService = new MedusaService();
    private final RAGFunction RAG_CONFIG = ContextLoader.configuration.getStores().getRag();
    private final Medusa MEDUSA_CONFIG = ContextLoader.configuration.getStores().getMedusa();
    private final Integer debugLevel = ContextLoader.configuration.getDebugLevel() == null ? 0 : ContextLoader.configuration.getDebugLevel();
    private final MeetingWorker meetingWorker = new MeetingWorker();

    private final PdfPreviewServlce pdfPreviewServlce = new PdfPreviewServlce();

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
        }

        String jsonResult = null;
        try {
            jsonResult = HttpUtil.httpPost(apiurl, headers, entity, 2 * 1000);
        } catch (Exception e) {
            System.out.println(e);
        }
        responsePrint(resp, jsonResult);
    }

    private void completions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        String contextPath = session.getServletContext().getRealPath("");
        EnhanceChatCompletionRequest chatCompletionRequest = getChatCompletionFromRequest(req, session);
        ChatCompletionResult chatCompletionResult = null;
        List<IndexSearchData> indexSearchDataList = null;
        String SAMPLE_COMPLETION_RESULT_PATTERN = "{\"created\":0,\"choices\":[{\"index\":0,\"message\":{\"content\":\"%s\"}}]}";
        if (Boolean.TRUE.equals(RAG_CONFIG.getEnable()) && Boolean.TRUE.equals(chatCompletionRequest.getRag())) {
            ModelService modelService = (ModelService) LlmRouterDispatcher
                    .getRagAdapter(null).stream().findFirst().orElse(null);
            if (modelService != null && RAG_CONFIG.getPriority() > modelService.getPriority()) {
                indexSearchDataList = vectorDbService.searchByContext(chatCompletionRequest);
                String lastMessage1 = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
                indexSearchDataList = rerank(lastMessage1, indexSearchDataList);
                if (indexSearchDataList.isEmpty()) {
                    String s = String.format(SAMPLE_COMPLETION_RESULT_PATTERN, RAG_CONFIG.getDefaultText());
                    outPrintJson(resp, chatCompletionRequest, s);
                    return;
                }
            }
        }

        if (Boolean.TRUE.equals(MEDUSA_CONFIG.getEnable()) && Boolean.TRUE.equals(chatCompletionRequest.getRag())) {
            ChatCompletionRequest medusaRequest = getCompletionRequest(chatCompletionRequest);
            PromptInput promptInput = medusaService.getPromptInput(medusaRequest);
            chatCompletionResult = medusaService.locate(promptInput);
            if (chatCompletionResult != null) {
                outPrintChatCompletion(resp, chatCompletionRequest, chatCompletionResult);
                logger.info("Cache hit: {}", PromptInputUtil.getNewestPrompt(promptInput));
                return;
            } else {
                medusaService.triggerCachePut(promptInput);
                if (medusaService.getPromptPool() != null) {
                    medusaService.getPromptPool().put(PooledPrompt.builder()
                            .promptInput(promptInput).status(PromptCacheConfig.POOL_INITIAL).build());
                }
            }
        }
        boolean hasTruncate = false;
        GetRagContext context = null;
        if (chatCompletionRequest.getCategory() != null && Boolean.TRUE.equals(RAG_CONFIG.getEnable()) && Boolean.TRUE.equals(chatCompletionRequest.getRag())) {
            String lastMessage = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
            String answer = VectorCacheLoader.get2L2(lastMessage);
            if (StrUtil.isNotBlank(answer)) {
                outPrintJson(resp, chatCompletionRequest, String.format(SAMPLE_COMPLETION_RESULT_PATTERN, answer));
                return;
            }
            if (indexSearchDataList == null) {
                indexSearchDataList = vectorDbService.searchByContext(chatCompletionRequest);
                String lastMessage1 = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
                indexSearchDataList = rerank(lastMessage1, indexSearchDataList);
            }
            if (indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
                context = completionsService.getRagContext(indexSearchDataList, CompletionUtil.MAX_INPUT);
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
            streamOutPrint(chatCompletionRequest, context, indexSearchDataList, out, LlmManager.getInstance().getAdapters().size(), contextPath);
        } else {
            ChatCompletionResult result = completionsService.completions(chatCompletionRequest, indexSearchDataList);
            if (context != null) {
                CompletionUtil.populateContext(result, indexSearchDataList, context);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(toJson(result));
                JsonNode choicesNode = rootNode.path("choices");
                for (JsonNode choiceNode : choicesNode) {
                    JsonNode messageNode = choiceNode.path("message");
                    ((com.fasterxml.jackson.databind.node.ObjectNode) messageNode).putPOJO("contextChunkIds", context.getChunkIds());
                    //((com.fasterxml.jackson.databind.node.ObjectNode) messageNode).putPOJO("chunkData", context);

                    //List<CropRectResponse> cropRectResponses = pdfPreviewServlce.cropRect(context.getChunkIds(), messageNode.path("context").asText(), contextPath);
//                    if (!cropRectResponses.isEmpty()) {
//                        ((com.fasterxml.jackson.databind.node.ObjectNode) messageNode).putPOJO("cropRectResponse", cropRectResponses);
//                    }
                }

                responsePrint(resp, rootNode.toString());
            } else {
                responsePrint(resp, toJson(result));
            }

        }
    }


    private List<IndexSearchData> rerank(String question, List<IndexSearchData> indexSearchDataList) {
        return indexSearchDataList.stream().peek(indexSearchData -> {
            String text = indexSearchData.getText();
            int distance = MinimumEditDistance.calculateMinEditDistance(question, text);
            int len = text.length();
            double ratio = (double) distance / len;
            ratio = (ratio * 0.9 + indexSearchData.getDistance() * 0.1) * indexSearchData.getDistance();
            indexSearchData.setDistance((float) ratio);
        }).sorted((o1, o2) -> Float.compare(o1.getDistance(), o2.getDistance())).collect(Collectors.toList());
    }

    private void outPrintChatCompletion(HttpServletResponse resp, ChatCompletionRequest chatCompletionRequest, ChatCompletionResult chatCompletionResult) throws IOException {
        if (Boolean.TRUE.equals(chatCompletionRequest.getStream())) {
            streamOutPrint(resp, chatCompletionResult);
        } else {
            outPrint(resp, chatCompletionResult);
        }
    }

    private void outPrintJson(HttpServletResponse resp, ChatCompletionRequest chatCompletionRequest, String s) throws IOException {
        if (Boolean.TRUE.equals(chatCompletionRequest.getStream())) {
            streamOutPrint(resp, s);
        } else {
            outPrint(resp, s);
        }
    }


    private void outPrint(HttpServletResponse resp, ChatCompletionResult chatCompletionResult) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        responsePrint(resp, toJson(chatCompletionResult));
    }

    private void outPrint(HttpServletResponse resp, String json) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        responsePrint(resp, json);
    }

    private void streamOutPrint(HttpServletResponse resp, ChatCompletionResult chatCompletionResult) throws IOException {
        resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.print("data: " + toJson(chatCompletionResult) + "\n\n");
        out.print("data: " + "[DONE]" + "\n\n");
        out.flush();
        out.close();
    }

    private void streamOutPrint(HttpServletResponse resp, String json) throws IOException {
        resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
        PrintWriter out = resp.getWriter();
        out.print("data: " + json + "\n\n");
        out.print("data: " + "[DONE]" + "\n\n");
        out.flush();
        out.close();
    }

    private EnhanceChatCompletionRequest getChatCompletionFromRequest(HttpServletRequest req, HttpSession session) throws IOException {
        ModelPreferenceDto preference = JSONUtil.toBean((String) session.getAttribute("preference"), ModelPreferenceDto.class);
        EnhanceChatCompletionRequest chatCompletionRequest = reqBodyToObj(req, EnhanceChatCompletionRequest.class);
        if (debugLevel >= 0) {
            logger.warn("chatCompletionRequest:{}", JSONUtil.toJsonStr(chatCompletionRequest));
        }
        if (chatCompletionRequest.getModel() == null
                && preference != null
                && preference.getLlm() != null) {
            chatCompletionRequest.setModel(preference.getLlm());
        }
        if (chatCompletionRequest.getRag() == null) {
            chatCompletionRequest.setRag(true);
        }
        return chatCompletionRequest;
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

    private void streamOutPrint(ChatCompletionRequest chatCompletionRequest, GetRagContext context, List<IndexSearchData> indexSearchDataList, PrintWriter out, int limit, String contextPath) {
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
                    streamOutPrint(chatCompletionRequest, context, indexSearchDataList, out, finalLimit, contextPath);
                },
                () -> {
                    if (lastResult[0] == null) {
                        streamOutPrint(chatCompletionRequest, context, indexSearchDataList, out, finalLimit, contextPath);
                        return;
                    }
                    extracted(lastResult, indexSearchDataList, context, out, contextPath);
                    lastResult[0].setChoices(lastResult[1].getChoices());

                    out.flush();
                    out.close();
                }
        );
    }


    private void extracted(ChatCompletionResult[] lastResult, List<IndexSearchData> indexSearchDataList, GetRagContext ragContext, PrintWriter out, String contextPath) {
        if (lastResult[0] != null && !lastResult[0].getChoices().isEmpty()
                && indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
            IndexSearchData indexData = indexSearchDataList.get(0);
            List<String> imageList = vectorDbService.getImageFiles(indexData);
            List<String> filePaths = ragContext.getFilePaths().stream().distinct().collect(Collectors.toList());
            List<String> filenames = ragContext.getFilenames().stream().distinct().collect(Collectors.toList());
            List<String> chunkIds = ragContext.getChunkIds().stream().distinct().collect(Collectors.toList());
            for (int i = 0; i < lastResult[0].getChoices().size(); i++) {
                //List<CropRectResponse> cropRectResponses = pdfPreviewServlce.cropRect(ragContext.getChunkIds(), ragContext.getContext(), contextPath);

                ChatMessageResponse message = ChatMessageResponse.builder()
                        .contextChunkIds(ragContext.getChunkIds())
                        .build();
                message.setFilename(filenames);
                message.setFilepath(filePaths);
                message.setContext(ragContext.getContext());
                message.setContextChunkIds(chunkIds);

                message.setContent("");
                message.setImageList(imageList);
                lastResult[0].getChoices().get(i).setMessage(message);
            }
            out.print("data: " + gson.toJson(lastResult[0]) + "\n\n");
        }
        out.print("data: " + "[DONE]" + "\n\n");
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
