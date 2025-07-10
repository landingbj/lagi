package ai.agent.customer;

import ai.agent.Agent;
import ai.agent.customer.pojo.Thoughts;
import ai.agent.customer.pojo.ToolInfo;
import ai.agent.customer.prompt.Prompt;
import ai.agent.customer.tools.AbstractTool;
import ai.agent.customer.tools.ToolManager;
import ai.agent.customer.tools.ToolUtils;
import ai.config.ContextLoader;
import ai.config.pojo.AgentConfig;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.*;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.reactivex.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FunctioncallCustomerAgent extends Agent<ChatCompletionRequest, ChatCompletionResult> {
    private final Integer maxTryTimes = 3;
    private final CompletionsService completionsService = new CompletionsService();
    private final Gson gson = new Gson();
    protected List<ToolInfo> toolInfoList;

    public FunctioncallCustomerAgent(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        toolInfoList = new ArrayList<>();
    }

    public FunctioncallCustomerAgent() {
        toolInfoList = new ArrayList<>();
    }

    public FunctioncallCustomerAgent(List<ToolInfo> toolInfoList) {
        this.toolInfoList = toolInfoList;
    }

    private ChatCompletionResult callLLm(String prompt, List<List<String>> history, String userMsg) {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.setModel(ContextLoader.configuration.getAgentGeneralConfiguration().getModel());
        List<ChatMessage> chatMessages = new ArrayList<>();
        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setContent(prompt);
        systemMessage.setRole("system");
        chatMessages.add(systemMessage);
        for (int i = 0; i < history.size(); i++) {
            ChatMessage userMessage = new ChatMessage();
            userMessage.setRole("user");
            userMessage.setContent(history.get(i).get(0));

            ChatMessage assistantMessage = new ChatMessage();
            assistantMessage.setRole("assistant");
            assistantMessage.setContent(history.get(i).get(1));

            chatMessages.add(userMessage);
            chatMessages.add(assistantMessage);
        }
        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(userMsg);
        chatMessages.add(userMessage);
        request.setMax_tokens(2048);
        request.setTemperature(0);
        request.setMessages(chatMessages);
        request.setStream(false);
        System.out.println("request = " + gson.toJson(request));
        return completionsService.completions(request);
    }

    private String genPrompt(String question, String agent_scratch) {
        return Prompt.genPrompt(ToolUtils.genToolPrompt(toolInfoList), question, agent_scratch);
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        int count = maxTryTimes;
        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        BeanUtil.copyProperties(data, chatCompletionRequest);
        chatCompletionRequest.setStream(false);

        List<Tool> tools = toolInfoList.stream().map(toolInfo -> {
            Tool openaiTool = new Tool();
            openaiTool.setType("function");
            Function function = new Function();
            function.setName(toolInfo.getName());
            function.setDescription(toolInfo.getDescription());
            Parameters parameters = new Parameters();
            Map<String, Property> properties = toolInfo.getArgs().stream().collect(Collectors.toMap(toolArg -> toolArg.getName(), toolArg -> {
                Property property = new Property();
                property.setDescription(toolArg.getDescription());
                property.setType(toolArg.getType());
                return property;
            }));
            parameters.setProperties(properties);
            function.setParameters(parameters);
            openaiTool.setFunction(function);
            return openaiTool;
        }).collect(Collectors.toList());

        chatCompletionRequest.setTools(tools);
        String model = ContextLoader.configuration.getAgentGeneralConfiguration().getModel();
        chatCompletionRequest.setModel(model);
        ChatCompletionResult result = completionsService.completions(chatCompletionRequest);
        ChatMessage assistantMessage = result.getChoices().get(0).getMessage();
        List<ToolCall> functionCalls = assistantMessage.getTool_calls();
        List<ChatMessage> chatMessages = chatCompletionRequest.getMessages();
        chatMessages.add(assistantMessage);
        List<String>  imageUrl = null;
        List<String> fileUrl = null;
        while (functionCalls != null && !functionCalls.isEmpty() && count > 0) {
            List<ToolCall> loopFunctionCalls =  new ArrayList<>(functionCalls);
            List<ToolCall> nextFunctionCalls =  new ArrayList<>();
            for (ToolCall functionCall: loopFunctionCalls) {

                String name = functionCall.getFunction().getName();
                AbstractTool tool = ToolManager.getInstance().getTool(name);
                Map<String, Object> args = gson.fromJson(functionCall.getFunction().getArguments(), new TypeToken<Map<String, Object>>() {
                }.getType());
                if("finish".equals(name)) {
                    imageUrl = (List<String>) args.get("imageUrl");
                    fileUrl = (List<String>) args.get("fileUrl");
                }
                String apply = tool.apply(args);
                ChatMessage toolChatMessage = new ChatMessage();
                toolChatMessage.setRole("tool");
                toolChatMessage.setTool_call_id(functionCall.getId());
                toolChatMessage.setContent(apply);
                chatMessages.add(toolChatMessage);
                ChatCompletionResult temp = completionsService.completions(chatCompletionRequest);
                assistantMessage = temp.getChoices().get(0).getMessage();
                chatMessages.add(assistantMessage);
                if (assistantMessage.getTool_calls() != null) {
                    nextFunctionCalls.addAll(assistantMessage.getTool_calls());
                }
                if(temp.getChoices().get(0).getMessage().getContent() != null) {
                    assistantMessage.setImageList(imageUrl);
                    assistantMessage.setFilepath(fileUrl);
                    result.getChoices().get(0).setMessage(assistantMessage);
                }
            }
            functionCalls = nextFunctionCalls;
            count--;
        }
        return result;
    }

    @Override
    public Observable<ChatCompletionResult> stream(ChatCompletionRequest data) {
        throw new UnsupportedOperationException("streaming is not supported");
    }

    @Override
    public boolean canStream() {
        return false;
    }

    private String parseThoughts(Thoughts thoughts) {
        return StrUtil.format("plan: {}\n reasoning:{}\n criticism: {}\n observation:{}",
                thoughts.getPlain(),
                thoughts.getReasoning(),
                thoughts.getCriticism(),
                thoughts.getSpeak());
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


}
