package ai.servlet.api;

import ai.agent.Agent;
import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.common.pojo.Configuration;
import ai.common.pojo.IndexSearchData;
import ai.common.pojo.Medusa;
import ai.common.pojo.Response;
import ai.config.ContextLoader;
import ai.config.pojo.AgentConfig;
import ai.config.pojo.RAGFunction;
import ai.dto.DeductExpensesRequest;
import ai.dto.ModelPreferenceDto;
import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingRequest;
import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.llm.pojo.EnhanceChatCompletionRequest;
import ai.llm.pojo.GetRagContext;
import ai.llm.schedule.QueueSchedule;
import ai.llm.service.CompletionsService;
import ai.llm.service.LlmRouterDispatcher;
import ai.llm.utils.CompletionUtil;
import ai.medusa.MedusaService;
import ai.medusa.pojo.PooledPrompt;
import ai.medusa.pojo.PromptInput;
import ai.medusa.utils.PromptCacheConfig;
import ai.medusa.utils.PromptCacheTrigger;
import ai.medusa.utils.PromptInputUtil;
import ai.migrate.service.AgentService;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.response.ChatMessageResponse;
import ai.router.pojo.LLmRequest;
import ai.servlet.BaseServlet;
import ai.servlet.dto.LagiAgentListResponse;
import ai.servlet.dto.LagiAgentResponse;
import ai.utils.ClientIpAddressUtil;
import ai.utils.MigrateGlobal;
import ai.utils.SensitiveWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorCacheLoader;
import ai.vector.VectorDbService;
import ai.worker.DefaultWorker;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
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
import java.lang.reflect.Constructor;
import java.util.*;
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
    private final Boolean enableQueueHandle = ContextLoader.configuration.getFunctions().getPolicy().getEnableQueueHandle();
    private final QueueSchedule queueSchedule = enableQueueHandle ? new QueueSchedule() : null;
    private final DefaultWorker defaultWorker = new DefaultWorker();
    private AgentService agentService = new AgentService();

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
        } else if(method.equals("go")) {
            this.go(req, resp);
        }
    }

    private void go(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        resp.setContentType("application/json;charset=utf-8");
        LLmRequest lLmRequest = reqBodyToObj(req, LLmRequest.class);
        List<Agent<ChatCompletionRequest, ChatCompletionResult>> agents = new ArrayList<>();
        if(lLmRequest.getAgentId() == null) {
            LagiAgentListResponse lagiAgentList = agentService.getLagiAgentList(null, 1, 1000, "true");
             agents = convert2AgentList(lagiAgentList.getData());
        } else {
            LagiAgentResponse lagiAgent = agentService.getLagiAgent(null, String.valueOf(lLmRequest.getAgentId()));
            agents = convert2AgentList(Lists.newArrayList(lagiAgent.getData()));
            if(agents != null && agents.size() == 1) {
                Agent<ChatCompletionRequest, ChatCompletionResult> agent = agents.get(0);
                DeductExpensesRequest deductExpensesRequest = new DeductExpensesRequest();
                deductExpensesRequest.setAgentId(lLmRequest.getAgentId());
                deductExpensesRequest.setUserId(lLmRequest.getUserId());
                Response response = agentService.deductExpenses(deductExpensesRequest);
                if(!"success".equals(response.getStatus())) {
                    String format = StrUtil.format("{\"source\":\"{}\",  \"created\":0,\"choices\":[{\"index\":0,\"message\":{\"content\":\"您在{}账户余额不足\"}}]}", agent.getAgentConfig().getName(), agent.getAgentConfig().getName());
                    responsePrint(resp, toJson(format));
                }
            }
        }
        ChatCompletionResult work = defaultWorker.work(lLmRequest.getWorker(), agents, lLmRequest);
        if(Boolean.FALSE.equals(lLmRequest.getStream())) {
            responsePrint(resp, toJson(work));
            return;
        }
        if(work != null) {
            String firstAnswer = ChatCompletionUtil.getFirstAnswer(work);
            convert2streamAndOutput(firstAnswer, resp, work);
        }
    }

    private static List<Agent<ChatCompletionRequest, ChatCompletionResult>> convert2AgentList(List<AgentConfig> agentConfigs) {
        Map<String, Constructor<?>> agentMap = new HashMap<>();
        return agentConfigs.stream().map(agentConfig -> {
            String driver = agentConfig.getDriver();
            Agent<ChatCompletionRequest, ChatCompletionResult> agent = null;
            if(!agentMap.containsKey(driver)) {
                try {
                    Class<?> aClass = Class.forName(driver);
                    Constructor<?> constructor = aClass.getConstructor(AgentConfig.class);
                    agentMap.put(driver, constructor);
                    agent = (Agent<ChatCompletionRequest, ChatCompletionResult>) constructor.newInstance(agentConfig);
                } catch (Exception ignored) {
                }
            } else {
                Constructor<?> constructor = agentMap.get(driver);
                try {
                    agent = (Agent<ChatCompletionRequest, ChatCompletionResult>) constructor.newInstance(agentConfig);
                } catch (Exception ignored) {
                }
            }
            return agent;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    private ChatCompletionResultWithSource convertResponse(String source,  String response) {
        String format = StrUtil.format("{\"source\":\"{}\",  \"created\":0,\"choices\":[{\"index\":0,\"message\":{\"content\":\"{}\"}}]}", source, response);
        return JSONUtil.toBean(format, ChatCompletionResultWithSource.class);
    }

    private void convert2streamAndOutput(String firstAnswer, HttpServletResponse resp, ChatCompletionResult chatCompletionResult) throws IOException {
        resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
        PrintWriter out = resp.getWriter();
        int length = 5;
        String source = "";
        if(chatCompletionResult instanceof ChatCompletionResultWithSource) {
            source = ((ChatCompletionResultWithSource) chatCompletionResult).getSource();
        }
        for (int i = 0; i < firstAnswer.length(); i += length) {
            int end = Math.min(i + length, firstAnswer.length());
            try {
                String substring = firstAnswer.substring(i, end);

                ChatCompletionResult result = convertResponse(source, substring);
                ChatCompletionResult filter = SensitiveWordUtil.filter(result);
                String msg = gson.toJson(filter);
                out.print("data: " + msg + "\n\n");
                out.flush();
            } catch (Exception e) {
            }
            try {
                Thread.sleep(200);
            } catch (Exception ignored) {
                logger.error("produce stream", ignored);
            }
        }
        chatCompletionResult.getChoices().get(0).getMessage().setContent("");
        out.print("data: " + gson.toJson(chatCompletionResult) + "\n\n");
        out.flush();
        out.print("data: " + "[DONE]" + "\n\n");
        out.flush();
        out.close();
    }


    private void completions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        ChatCompletionRequest chatCompletionRequest = setCustomerModel(req, session);
        ChatCompletionResult chatCompletionResult = null;

        List<IndexSearchData> indexSearchDataList = null;
        String SAMPLE_COMPLETION_RESULT_PATTERN = "{\"created\":0,\"choices\":[{\"index\":0,\"message\":{\"content\":\"%s\"}}]}";

        if (Boolean.TRUE.equals(RAG_CONFIG.getEnable())) {
            ModelService modelService = (ModelService) LlmRouterDispatcher
                    .getRagAdapter(null).stream().findFirst().orElse(null);
            if(modelService != null  && RAG_CONFIG.getPriority() > modelService.getPriority()) {
                indexSearchDataList = vectorDbService.searchByContext(chatCompletionRequest);
                if(indexSearchDataList.isEmpty()) {
                    String s = String.format(SAMPLE_COMPLETION_RESULT_PATTERN, RAG_CONFIG.getDefaultText());
                    outPrintJson(resp, chatCompletionRequest, s);
                    return ;
                }
            }
        }

        if(Boolean.TRUE.equals(MEDUSA_CONFIG.getEnable())) {
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
        if (chatCompletionRequest.getCategory() != null && Boolean.TRUE.equals(RAG_CONFIG.getEnable())) {
            String lastMessage = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
            String answer = VectorCacheLoader.get2L2(lastMessage);
            if(StrUtil.isNotBlank(answer)) {
                outPrintJson(resp,  chatCompletionRequest,String.format(SAMPLE_COMPLETION_RESULT_PATTERN, answer));
                return;
            }
            if(indexSearchDataList == null) {
                indexSearchDataList = vectorDbService.searchByContext(chatCompletionRequest);
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
        if(!hasTruncate) {
            List<ChatMessage> chatMessages = CompletionUtil.truncateChatMessages(chatCompletionRequest.getMessages());
            chatCompletionRequest.setMessages(chatMessages);
        }
        EnhanceChatCompletionRequest enhance = EnhanceChatCompletionRequest.builder()
                .ip(ClientIpAddressUtil.getClientIpAddress(req))
                .build();
        BeanUtil.copyProperties(chatCompletionRequest, enhance);
        chatCompletionRequest = enhance;
        if (chatCompletionRequest.getStream() != null && chatCompletionRequest.getStream()) {
            try {
                Observable<ChatCompletionResult> result;
                if(enableQueueHandle) {
                    result = queueSchedule.streamSchedule(chatCompletionRequest, indexSearchDataList);
                } else {
                    result = completionsService.streamCompletions(chatCompletionRequest, indexSearchDataList);
                }
                resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
                streamOutPrint(result, context, indexSearchDataList, out);
            } catch (RRException e) {
                resp.setStatus(e.getCode());
                responsePrint(resp, e.getMsg());
            }

        } else {
            try {
                ChatCompletionResult result;
                if(enableQueueHandle) {
                    result = queueSchedule.schedule(chatCompletionRequest, indexSearchDataList);
                } else {
                    result = completionsService.completions(chatCompletionRequest, indexSearchDataList);
                }
                if (context != null) {
                    CompletionUtil.populateContext(result, indexSearchDataList, context.getContext());
                    addChunkIds(result, context);
                }
                responsePrint(resp, toJson(result));
            } catch (RRException e) {
                resp.setStatus(e.getCode());
                responsePrint(resp, e.getMsg());
            }
        }
    }

    private void addChunkIds(ChatCompletionResult result, GetRagContext context) {
        result.getChoices().forEach(choice -> {
            ChatMessage message = choice.getMessage();
            ChatMessageResponse build = ChatMessageResponse.builder().contextChunkIds(context.getChunkIds()).build();
            BeanUtil.copyProperties(message, build);
            choice.setMessage(build);
        });
    }


    private void outPrintChatCompletion(HttpServletResponse resp, ChatCompletionRequest chatCompletionRequest, ChatCompletionResult chatCompletionResult) throws IOException {
        if(Boolean.TRUE.equals(chatCompletionRequest.getStream())) {
            streamOutPrint(resp, chatCompletionResult);
        } else {
            outPrint(resp, chatCompletionResult);
        }
    }

    private void outPrintJson(HttpServletResponse resp, ChatCompletionRequest chatCompletionRequest, String s) throws IOException {
        if(Boolean.TRUE.equals(chatCompletionRequest.getStream())) {
            streamOutPrint(resp, s);
        } else {
            outPrint(resp, s);
        }
    }


    private void outPrint(HttpServletResponse resp,  ChatCompletionResult chatCompletionResult) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        responsePrint(resp, toJson(chatCompletionResult));
    }

    private void outPrint(HttpServletResponse resp,  String json) throws IOException {
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


    private ChatCompletionRequest setCustomerModel(HttpServletRequest req, HttpSession session) throws IOException {
        ModelPreferenceDto preference = JSONUtil.toBean((String) session.getAttribute("preference"), ModelPreferenceDto.class) ;
        ChatCompletionRequest chatCompletionRequest = reqBodyToObj(req, ChatCompletionRequest.class);
        if(chatCompletionRequest.getModel() == null
                && preference != null
                && preference.getLlm() != null) {
            chatCompletionRequest.setModel(preference.getLlm());
        }
        return chatCompletionRequest;
    }

    private static ChatCompletionRequest getCompletionRequest(ChatCompletionRequest chatCompletionRequest) {
        List<Integer> integers = PromptCacheTrigger.analyzeChatBoundariesForIntent(chatCompletionRequest);
        ChatCompletionRequest medusaRequest = null;
        if(!integers.isEmpty()) {
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

    private void streamOutPrint(Observable<ChatCompletionResult> observable, GetRagContext context, List<IndexSearchData> indexSearchDataList, PrintWriter out) {
        final ChatCompletionResult[] lastResult = {null, null};
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
                },
                () -> {
                    if(lastResult[0] == null) {
                        return;
                    }
                    extracted(lastResult,indexSearchDataList,context, out);
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
            List<String> chunkIds = ragContext.getChunkIds().stream().distinct().collect(Collectors.toList());
            for (int i = 0; i < lastResult[0].getChoices().size(); i++) {
//                ChatMessage message = lastResult[0].getChoices().get(0).getMessage();
                ChatMessageResponse message = ChatMessageResponse.builder()
                        .contextChunkIds(ragContext.getChunkIds())
                        .build();
//                message.setContext(ragContext.getContext());
                IndexSearchData indexData1 = indexSearchDataList.get(i);
                    if (!(indexData1.getFilename() != null && indexData1.getFilename().size() == 1
                            && indexData1.getFilename().get(0).isEmpty())) {
                    message.setFilename(filenames);
                    message.setFilepath(filePaths);
                    message.setContextChunkIds(chunkIds);
                }
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
