package ai.agent.customer;

import ai.agent.Agent;
import ai.agent.customer.pojo.ToolInfo;
import ai.config.ContextLoader;
import ai.config.pojo.AgentConfig;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GeneralAgent extends Agent<ChatCompletionRequest, ChatCompletionResult> {
    private final CompletionsService completionsService = new CompletionsService();
    protected List<ToolInfo> toolInfoList;

    public GeneralAgent(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        this.toolInfoList = new ArrayList<>();
    }

    public GeneralAgent() {
        this.toolInfoList = new ArrayList<>();
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        List<ChatMessage> messages = data.getMessages();
        List<ChatMessage> temp  =  new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole("system");
        chatMessage.setContent(agentConfig.getCharacter());
        temp.add(chatMessage);
        temp.addAll(messages);
        data.setMessages(temp);
        return completionsService.completions(data, null);

    }

    @Override
    public void connect() {
    }

    @Override
    public void terminate() {
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void send(ChatCompletionRequest request) {
    }

    @Override
    public ChatCompletionResult receive() {
        return null;
    }

    public static void main(String[] args) {
        ContextLoader.loadContext();

        String reqStr = "{\n" +
                "  \"category\": \"default\",\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \"你帮我制定一份一个月的健身计划表\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"temperature\": 0.8,\n" +
                "  \"max_tokens\": 1024,\n" +
                "  \"stream\": false\n" +
                "}";

        Gson gson = new Gson();
        ChatCompletionRequest request = gson.fromJson(reqStr, ChatCompletionRequest.class);

        AgentConfig agentConfig = new AgentConfig();
        agentConfig.setName("健身教练AI助手");
        agentConfig.setCharacter("# 角色规范\n" +
                "\n" +
                "你是一个健身教练AI助手，主要职责是根据用户的需求制定个性化的健身计划和饮食建议。你擅长科学的训练方法，并根据生理学和营养学为用户提供专业的健身指导。你总是以耐心、热情的态度与用户互动，帮助他们实现健身目标。你需要熟练掌握相关领域的知识，并根据用户需求提供准确、专业、权威的解答。\n" +
                "\n" +
                "# 思考规范\n" +
                "\n" +
                "在回应用户问题时，你需遵循以下思考路径：\n" +
                "1. **确认问题领域**：首先明确用户问题的领域（如健身计划、饮食建议等），确保你拥有相关的专业知识。\n" +
                "2. **综合解答**：结合科学理论、实际案例或其他专业资源，提供全面且精准的解答。\n" +
                "3. **处理不确定问题**：若遇到不确定或无法直接解答的问题，应建议用户咨询相关领域的专家或提供权威资源链接。\n" +
                "4. **确保信息权威性**：在提供任何建议时，务必确保信息准确无误且具备权威性。\n" +
                "5. **引导用户**：对于偏离咨询范围的问题，温和地引导用户回到主题，例如：“我主要专注于提供健身和饮食建议，关于您提到的其他方面的问题，我建议您直接咨询相关领域的专家。”\n" +
                "\n" +
                "# 回复规范\n" +
                "\n" +
                "在与用户交流时，你应遵循以下规范：\n" +
                "1. **语气**：使用专业且权威的语气，让用户感受到你的专业性和可靠性。\n" +
                "2. **回复格式**：回复要清晰、有条理，最好使用列表、序号等形式，便于用户理解。\n" +
                "3. **深入询问**：在解答问题时，主动询问用户更多背景信息，以便提供更加精确的解答。\n" +
                "4. **结尾引导**：每次回答结束后，可以询问用户是否还有其他相关问题，例如：“请问您是否还有其他健身或饮食方面的疑问？”\n" +
                "5. **直接回应**：确保每个回复都直接针对用户的具体问题，避免偏离主题。");

        GeneralAgent generalAgent = new GeneralAgent(agentConfig);

        ChatCompletionResult communicate = generalAgent.communicate(request);
        String jsonStr = JSONUtil.toJsonStr(communicate);
        System.out.println("jsonStr = " + jsonStr);
    }
}
