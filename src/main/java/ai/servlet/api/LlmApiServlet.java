package ai.servlet.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ai.lagi.service.CompletionsService;
import ai.migrate.pojo.Configuration;
import ai.migrate.pojo.IndexSearchData;
import ai.migrate.service.VectorDbService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.BaseServlet;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LlmApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private static Configuration config = LagiGlobal.config;
    private CompletionsService completionsService = new CompletionsService(config);
    private VectorDbService vectorDbService = new VectorDbService(config);

    private Logger logger = LoggerFactory.getLogger(LlmApiServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Type", "application/json;charset=utf-8");
        String url = req.getRequestURI();
        String method = url.substring(url.lastIndexOf("/") + 1);
        if (method.equals("completions")) {
            this.completions(req, resp);
        }
    }

    private void completions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        ChatCompletionRequest chatCompletionRequest = reqBodyToObj(req, ChatCompletionRequest.class);
        List<IndexSearchData> indexSearchDataList;
        if (chatCompletionRequest.getCategory() != null && vectorDbService.vectorStoreEnabled()) {
            indexSearchDataList = vectorDbService.search(chatCompletionRequest);
            if (indexSearchDataList != null && !indexSearchDataList.isEmpty()) {
                addVectorDBContext(chatCompletionRequest, indexSearchDataList);
            }
        }
        if (chatCompletionRequest.getStream() != null && chatCompletionRequest.getStream()) {
            Observable<ChatCompletionResult> observable = completionsService.streamCompletions(chatCompletionRequest);
            resp.setHeader("Content-Type", "text/event-stream;charset=utf-8");
            PrintWriter out = resp.getWriter();
            final ChatCompletionResult[] lastResult = {null};
            observable.subscribe(
                    data -> {
                        lastResult[0] = data;
                        out.print("data: " + gson.toJson(data) + "\n\n");
                        out.flush();
                    },
                    e -> logger.error("", e),
                    () -> {
                        if (lastResult[0] != null && !lastResult[0].getChoices().isEmpty()) {
                            for (int i = 0; i < lastResult[0].getChoices().size(); i++) {
                                ChatMessage message = lastResult[0].getChoices().get(i).getMessage();
                                message.setContext("dsfdksfjsdkf");
                            }
                            out.print("data: " + gson.toJson(lastResult[0]) + "\n\n");
                        }
                        out.print("data: " + "[DONE]" + "\n\n");
                    }
            );
            out.flush();
            out.close();
        } else {
            ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
            responsePrint(resp, toJson(result));
        }
    }

    private void addVectorDBContext(ChatCompletionRequest request, List<IndexSearchData> indexSearchDataList) {
        String lastMessage = ChatCompletionUtil.getLastMessage(request);
        String contextText = indexSearchDataList.get(0).getText();
        String prompt = ChatCompletionUtil.getPrompt(contextText, lastMessage);
        ChatCompletionUtil.setLastMessage(request, prompt);
    }
}
