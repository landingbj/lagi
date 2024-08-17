package ai.servlet.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ai.common.ModelService;
import ai.dto.ModelPreferenceDto;
import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingRequest;
import ai.llm.adapter.ILlmAdapter;
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
import ai.medusa.utils.PromptInputUtil;
import ai.migrate.service.VectorDbService;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import ai.utils.LagiGlobal;
import ai.utils.MigrateGlobal;
import ai.utils.SensitiveWordUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.vector.VectorCacheLoader;
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
        }
    }

    private void completions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        PrintWriter out = resp.getWriter();
        HttpSession session = req.getSession();
        ModelPreferenceDto preference = JSONUtil.toBean((String) session.getAttribute("preference"), ModelPreferenceDto.class) ;
        ChatCompletionRequest chatCompletionRequest = reqBodyToObj(req, ChatCompletionRequest.class);
        if(chatCompletionRequest.getModel() == null
                && preference != null
                && preference.getLlm() != null) {
            chatCompletionRequest.setModel(preference.getLlm());
        }

        PromptInput promptInput = medusaService.getPromptInput(chatCompletionRequest);
        ChatCompletionResult chatCompletionResult = medusaService.locate(promptInput);

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
        List<IndexSearchData> indexSearchDataList;
        String context = null;
        if (chatCompletionRequest.getCategory() != null && LagiGlobal.RAG_ENABLE) {
            String lastMessage = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
            String answer = VectorCacheLoader.get2L2(lastMessage);
            if(StrUtil.isNotBlank(answer)) {
                ChatCompletionResult temp = new ChatCompletionResult();
                ChatCompletionChoice choice = new ChatCompletionChoice();
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setContent(answer);
                choice.setMessage(chatMessage);
                temp.setChoices(Lists.newArrayList(choice));
                responsePrint(resp, toJson(temp));
                return;
            }
            indexSearchDataList = vectorDbService.searchByContext(chatCompletionRequest);
            if (indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
                context = completionsService.getRagContext(indexSearchDataList);
                context = CompletionUtil.truncate(context);
                completionsService.addVectorDBContext(chatCompletionRequest, context);
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
        if (chatCompletionRequest.getStream() != null && chatCompletionRequest.getStream()) {
            resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
            streamOutPrint(chatCompletionRequest, indexSearchDataList, out, LlmManager.getInstance().getAdapters().size());
        } else {
            for (int i = 0; i < LlmManager.getInstance().getAdapters().size(); i++) {
                ChatCompletionResult result = null;
                ChatCompletionRequest request = new ChatCompletionRequest();
                BeanUtil.copyProperties(chatCompletionRequest, request);
                ILlmAdapter ragAdapter = completionsService.getRagAdapter(request, indexSearchDataList);
                if(ragAdapter == null) {
                    continue;
                }
                try {
                    result = completionsService.completions(ragAdapter, request);
                } catch (Exception ignored) {
                    ModelService modelService = (ModelService) ragAdapter;
                    CacheManager.put(modelService.getModel(), false);
                    continue;
                }
                if(result == null) {
                    continue;
                }
                responsePrint(resp, toJson(result));
                CompletionUtil.populateContext(result, indexSearchDataList, context);
                break;
            }
        }
    }

    private void streamOutPrint(ChatCompletionRequest chatCompletionRequest, List<IndexSearchData> indexSearchDataList, PrintWriter out, int limit) {
        if(limit <= 0 ) {
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
                    streamOutPrint(chatCompletionRequest, indexSearchDataList, out, finalLimit);
                },
                () -> {
                    if(lastResult[0] == null) {
                        streamOutPrint(chatCompletionRequest, indexSearchDataList, out, finalLimit);
                        return;
                    }
                    extracted(lastResult, indexSearchDataList, out);
                    lastResult[0].setChoices(lastResult[1].getChoices());
                    out.flush();
                    out.close();
                }
        );
    }

    private void extracted(ChatCompletionResult[] lastResult, List<IndexSearchData> indexSearchDataList, PrintWriter out) {
        if (lastResult[0] != null && !lastResult[0].getChoices().isEmpty()
                && indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
            IndexSearchData indexData = indexSearchDataList.get(0);
            List<String> imageList = vectorDbService.getImageFiles(indexData);
            for (int i = 0; i < lastResult[0].getChoices().size(); i++) {
                ChatMessage message = lastResult[0].getChoices().get(i).getMessage();
                message.setContent("");
                message.setContext(indexData.getText());
                if (!(indexData.getFilename() != null && indexData.getFilename().size() == 1
                        && indexData.getFilename().get(0).isEmpty())) {
                    message.setFilename(indexData.getFilename());
                }
                message.setFilepath(indexData.getFilepath());
                message.setImageList(imageList);
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
