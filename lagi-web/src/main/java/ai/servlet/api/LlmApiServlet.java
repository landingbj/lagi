package ai.servlet.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ai.dto.ModelPreferenceDto;
import ai.embedding.EmbeddingFactory;
import ai.embedding.Embeddings;
import ai.embedding.pojo.OpenAIEmbeddingRequest;
import ai.llm.utils.CompletionUtil;
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
import ai.utils.MigrateGlobal;
import ai.vector.VectorCacheLoader;
import cn.hutool.json.JSONUtil;
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
        if(preference != null && chatCompletionRequest != null) {
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
            medusaService.getPromptPool().put(PooledPrompt.builder()
                    .promptInput(promptInput).status(PromptCacheConfig.POOL_INITIAL).build());
        }

        List<IndexSearchData> indexSearchDataList;
        String context = null;
        if (chatCompletionRequest.getCategory() != null && vectorDbService.vectorStoreEnabled()) {
            indexSearchDataList = vectorDbService.searchByContext(chatCompletionRequest);
            if (indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
                context = completionsService.getRagContext(indexSearchDataList);
                completionsService.addVectorDBContext(chatCompletionRequest, context);
            }
        } else {
            indexSearchDataList = null;
        }

        if (chatCompletionRequest.getStream() != null && chatCompletionRequest.getStream()) {
            resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
            Observable<ChatCompletionResult> observable = completionsService.streamCompletions(chatCompletionRequest);
            final ChatCompletionResult[] lastResult = {null, null};
            observable.subscribe(
                    data -> {
                        lastResult[0] = data;
                        String msg = gson.toJson(data);
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
                    e -> logger.error("", e),
                    () -> {
                        extracted(lastResult, indexSearchDataList, out);
                        lastResult[0].setChoices(lastResult[1].getChoices());
                    }
            );
            out.flush();
            out.close();
        } else {
            ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
            CompletionUtil.populateContext(result, indexSearchDataList, context);
            responsePrint(resp, toJson(result));
        }
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
                message.setFilename(indexData.getFilename());
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
