package ai.agent.customer;

import ai.agent.Agent;
import ai.agent.customer.pojo.*;
import ai.agent.customer.prompt.Prompt;
import ai.agent.customer.tools.*;
import ai.config.ContextLoader;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomerAgent extends Agent<ChatCompletionRequest, ChatCompletionResult> {
    private final Integer maxTryTimes = 3;
    private final CompletionsService  completionsService= new CompletionsService();
    private final Gson gson = new Gson();
    protected List<ToolInfo> toolInfoList;

    public CustomerAgent() {
        toolInfoList = new ArrayList<>();
    }

    public CustomerAgent(List<ToolInfo> toolInfoList) {
        this.toolInfoList = toolInfoList;
        if(toolInfoList == null) {
            return;
        }
        System.out.println("toolInfoList = " + toolInfoList);
    }
    private ChatCompletionResult callLLm(String prompt, List<List<String>> history, String userMsg) {
        ChatCompletionRequest request = new ChatCompletionRequest();
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
        request.setMax_tokens(1024);
        request.setTemperature(0);
        request.setMessages(chatMessages);
        System.out.println("request = " + gson.toJson(request));
        return completionsService.completions(request);
    }

    private String genPrompt(String question, String agent_scratch) {
        return Prompt.genPrompt(ToolUtils.genToolPrompt(toolInfoList), question, agent_scratch);
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        int count = maxTryTimes;
        String finalAnswer = null;
        String imageUrl = null;
        String fileUrl = null;
        String question = data.getMessages().get(data.getMessages().size() - 1).getContent();
        StringBuilder agent_scratch = new StringBuilder();
        String user_msg = "决定用哪个工具若认为任务已完成或工具调用失败直接调用finish工具";
        String assistant_msg = "";
        List<List<String>> history = new ArrayList<>();
        while (count-- > 0){
            String prompt = genPrompt(question, agent_scratch.toString());
            long start = System.currentTimeMillis();
            System.out.println("开始调用大模型");
            ChatCompletionResult result = callLLm(prompt, history, user_msg);
            System.out.println("结束调用大模型, 耗时：" + (System.currentTimeMillis()  - start));
            String answer = result.getChoices().get(0).getMessage().getContent();
            System.out.println("调用结果：" + answer);
            ResponseTemplate responseTemplate;
            try {
                responseTemplate = gson.fromJson(answer, ResponseTemplate.class);
            } catch (Exception e) {
                agent_scratch.append("\nobservation: 返回的结果不符合要求的json格式");
                continue;
            }
            Action action = responseTemplate.getAction();
            if("finish".equals(action.getName())) {
                finalAnswer = (String) action.getArgs().get("result");
                imageUrl = (String) action.getArgs().get("imageUrl");
                fileUrl = (String) action.getArgs().get("fileUrl");
                break;
            }
            String observation = responseTemplate.getThoughts().getSpeak();
            Map<String, Object> args = action.getArgs();
            String call_result=null;
            try {
                AbstractTool func = ToolManager.getInstance().getTool(action.getName());
                call_result = func.apply(args);
            } catch (Exception e) {

            }
            assistant_msg = parseThoughts(responseTemplate.getThoughts(), call_result);
            agent_scratch.append(StrUtil.format("\nobservation: {} \nexecute action results: {}",observation, call_result == null ? "查询失败": "查询成功"));
            history.add(Lists.newArrayList(user_msg, assistant_msg));
        }
        if(finalAnswer == null) {
            return null;
        }
        String format = StrUtil.format("{\"created\":0,\"choices\":[{\"index\":0,\"message\":{\"content\":\"{}\"}}]}", finalAnswer);
        ChatCompletionResult chatCompletionResult = gson.fromJson(format, ChatCompletionResult.class);
        ChatMessage message = chatCompletionResult.getChoices().get(0).getMessage();
        if(StrUtil.isNotBlank(fileUrl)) {
            message.setFilepath(Lists.newArrayList(fileUrl));
        }
        if(StrUtil.isNotBlank(imageUrl)) {
            message.setImageList(Lists.newArrayList(imageUrl));
        }
        return chatCompletionResult;
    }

    private String parseThoughts(Thoughts thoughts, String actionResult) {
        return StrUtil.format("plan: {}\nreasoning:{}\ncriticism: {}\nobservation:{}\nobservation :{}",
                thoughts.getPlain(),
                thoughts.getReasoning(),
                thoughts.getCriticism(),
                thoughts.getSpeak(), actionResult == null? "调用失败": "调用成功结果如下:"+actionResult);
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
                "      \"content\": \"武汉的天气如何油价是多少\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"temperature\": 0.8,\n" +
                "  \"max_tokens\": 1024,\n" +
                "  \"stream\": false\n" +
                "}";
        Gson gson = new Gson();
        WeatherSearchTool weatherSearchTool = new WeatherSearchTool();
        weatherSearchTool.setToken("xxx");
        FinishTool finishTool = new FinishTool();
        OilPriceSearchTool oilPriceSearchTool = new OilPriceSearchTool("xxx");
        List<ToolInfo> toolInfos = new ArrayList<>();
        toolInfos.add(weatherSearchTool.getToolInfo());
        toolInfos.add(oilPriceSearchTool.getToolInfo());
        toolInfos.add(finishTool.getToolInfo());
        ChatCompletionRequest request = gson.fromJson(reqStr, ChatCompletionRequest.class);
        CustomerAgent customerAgent = new CustomerAgent(toolInfos);
        ChatCompletionResult communicate = customerAgent.communicate(request);
        String jsonStr = JSONUtil.toJsonStr(communicate);
        System.out.println("jsonStr = " + jsonStr);
    }


}
