package ai.migrate.service;

import ai.common.pojo.Response;
import ai.config.pojo.AgentConfig;
import ai.dto.DeductExpensesRequest;
import ai.dto.FeeRequiredAgentRequest;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.servlet.dto.LagiAgentExpenseListResponse;
import ai.servlet.dto.LagiAgentListResponse;
import ai.servlet.dto.LagiAgentResponse;
import ai.utils.MigrateGlobal;
import ai.utils.OkHttpUtil;
import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentService {
    private final Gson gson = new Gson();
    private static final String SAAS_BASE_URL = MigrateGlobal.SAAS_BASE_URL;

    public Response addLagiAgent(AgentConfig agentConfig) throws IOException {
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/agent/addLagiAgent", gson.toJson(agentConfig));
        Response response = gson.fromJson(resultJson, Response.class);
        return response;
    }

    public Response updateLagiAgent(AgentConfig agentConfig) throws IOException {
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/agent/updateLagiAgent", gson.toJson(agentConfig));
        Response response = gson.fromJson(resultJson, Response.class);
        return response;
    }

    public Response deleteLagiAgentById(List<Integer> ids) throws IOException {
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/agent/deleteLagiAgentById", gson.toJson(ids));
        Response response = gson.fromJson(resultJson, Response.class);
        return response;
    }

    public LagiAgentListResponse getLagiAgentList(String lagiUserId, int pageNumber, int pageSize, String publishStatus) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (lagiUserId != null) {
            params.put("lagiUserId", lagiUserId);
        }
        if (publishStatus != null) {
            params.put("publishStatus", publishStatus);
        }
        params.put("pageNumber", String.valueOf(pageNumber));
        params.put("pageSize", String.valueOf(pageSize));
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/agent/getLagiAgentList", params);
        LagiAgentListResponse lagiAgentResponse = gson.fromJson(resultJson, LagiAgentListResponse.class);
        return lagiAgentResponse;
    }

    public LagiAgentResponse getLagiAgent(String lagiUserId, String agentId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("lagiUserId", lagiUserId);
        params.put("agentId", agentId);
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/agent/getLagiAgent", params);
        LagiAgentResponse lagiAgentResponse = gson.fromJson(resultJson, LagiAgentResponse.class);
        return lagiAgentResponse;
    }

    public LagiAgentExpenseListResponse getPaidAgentByUser(String lagiUserId, String pageNumber, String pageSize) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("lagiUserId", lagiUserId);
        if (pageNumber != null) params.put("pageNumber", pageNumber);
        if (pageSize != null) params.put("pageSize", pageSize);
        String resultJson = OkHttpUtil.get(SAAS_BASE_URL + "/agent/getPaidAgentByUser", params);
        LagiAgentExpenseListResponse lagiAgentResponse = gson.fromJson(resultJson, LagiAgentExpenseListResponse.class);
        return lagiAgentResponse;
    }

    public Response deductExpenses(DeductExpensesRequest deductExpensesRequest) throws IOException {
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/agent/deductExpenses", gson.toJson(deductExpensesRequest));
        Response lagiAgentResponse = gson.fromJson(resultJson, Response.class);
        return lagiAgentResponse;
    }

    public LagiAgentListResponse getFeeRequiredAgent(FeeRequiredAgentRequest feeRequiredAgentRequest) throws IOException {
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/agent/getFeeRequiredAgent", gson.toJson(feeRequiredAgentRequest));
        LagiAgentListResponse lagiAgentResponse = gson.fromJson(resultJson, LagiAgentListResponse.class);
        return lagiAgentResponse;
    }

    public Response createLagiAgent(AgentConfig agentConfig) throws IOException {
        String prompt = generatePrompt(agentConfig.getDescribe(),agentConfig.getName());
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setTemperature(0.8);
        chatCompletionRequest.setMax_tokens(1024);
        ChatMessage message = new ChatMessage();
        message.setRole("system");
        message.setContent(prompt);
        chatCompletionRequest.setMessages(Lists.newArrayList(message));
        chatCompletionRequest.setStream(false);
        CompletionsService completionsService = new CompletionsService();
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        agentConfig.setCharacter(result.getChoices().get(0).getMessage().getContent());
        agentConfig.setDriver("ai.agent.customer.GeneralAgent");
        agentConfig.setToken(IdUtil.simpleUUID());
        agentConfig.setAppId(IdUtil.simpleUUID());
        String resultJson = OkHttpUtil.post(SAAS_BASE_URL + "/agent/addLagiAgent", gson.toJson(agentConfig));
        Response response = gson.fromJson(resultJson, Response.class);
        return response;
    }

    public static String generatePrompt(String describe, String name) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("智能体名称：").append(name).append("\n\n");
        prompt.append("简介：").append(describe).append("\n\n");
        prompt.append("根据上述简介，生成以下内容：\n\n");
        prompt.append("#角色规范\n");
        prompt.append("你是一个").append(name).append("，主要职责是").append(describe).append("。你需熟练掌握相关领域的知识，并根据用户需求提供准确、专业、权威的解答。\n\n");
        prompt.append("#思考规范\n");
        prompt.append("在回应用户问题时，你需遵循以下思考路径：\n");
        prompt.append("1. **确认问题领域**：首先明确用户问题的领域，确保你拥有相关的专业知识。\n");
        prompt.append("2. **综合解答**：结合法律条文、相关案例分析或其他专业资源，提供全面且精准的解答。\n");
        prompt.append("3. **处理不确定问题**：若遇到不确定或无法直接解答的问题，应建议用户咨询相关领域的专家或提供权威资源链接。\n");
        prompt.append("4. **确保信息权威性**：在提供任何建议时，务必确保信息准确无误且具备权威性。\n");
        prompt.append("5. **引导用户**：对于偏离咨询范围的问题，温和地引导用户回到主题，例如：“我主要专注于提供法律咨询，关于您提到的非法律问题，我建议您直接咨询相关领域的专家。”\n\n");
        prompt.append("#回复规范\n");
        prompt.append("在与用户交流时，你应遵循以下规范：\n");
        prompt.append("1. **语气**：使用专业且权威的语气，让用户感受到你的专业性和可靠性。\n");
        prompt.append("2. **回复格式**：回复要清晰、有条理，最好使用列表、序号等形式，便于用户理解。\n");
        prompt.append("3. **深入询问**：在解答问题时，主动询问用户更多背景信息，以便提供更加精确的解答。\n");
        prompt.append("4. **结尾引导**：每次回答结束后，可以询问用户是否还有其他相关问题，例如：“请问您是否还有其他法律问题需要咨询？”\n");
        prompt.append("5. **直接回应**：确保每个回复都直接针对用户的具体问题，避免偏离主题。\n");
        prompt.append("\n根据上述结构，生成相应的角色规范、思考规范和回复规范。请确保输出内容详细、结构清晰，并且符合智能体的角色特征。\n");
        return prompt.toString();
    }


}

