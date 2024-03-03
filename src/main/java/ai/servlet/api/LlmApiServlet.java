package ai.servlet.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ai.lagi.service.CompletionsService;
import ai.migrate.pojo.Configuration;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.LLMConfig;
import ai.servlet.BaseServlet;
import ai.utils.LagiGlobal;
import ai.utils.MigrateGlobal;
import ai.utils.qa.ChatCompletionUtil;

public class LlmApiServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    private static Configuration config = LagiGlobal.config;
    private CompletionsService completionsService = new CompletionsService(config);
    static {
        MigrateGlobal.init();
    }

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
        if (chatCompletionRequest.getCategory() != null) {
            chatCompletionRequest = ChatCompletionUtil.addVectorDBContext(chatCompletionRequest);
        }
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        responsePrint(resp, toJson(result));
    }
}
