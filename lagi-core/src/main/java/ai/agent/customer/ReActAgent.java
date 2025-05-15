package ai.agent.customer;

import ai.agent.customer.pojo.ToolInfo;
import ai.agent.customer.tools.AbstractTool;
import ai.agent.customer.tools.ToolManager;
import ai.config.ContextLoader;
import ai.config.pojo.AgentConfig;
import ai.llm.service.CompletionsService;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ReActAgent extends CustomerAgent {
    private final Integer maxTryTimes = 3;
    private final CompletionsService completionsService = new CompletionsService();
    private final Gson gson = new Gson();
    protected List<ToolInfo> toolInfoList;

    private final String SYSTEM_PROMPT = "Assistant is a large language model trained by OpenAI.\n" +
            "\n" +
            "Assistant is designed to be able to assist with a wide range of tasks, from answering simple questions to providing in-depth explanations and discussions on a wide range of topics. As a language model, Assistant is able to generate human-like text based on the input it receives, allowing it to engage in natural-sounding conversations and provide responses that are coherent and relevant to the topic at hand.\n" +
            "\n" +
            "Assistant is constantly learning and improving, and its capabilities are constantly evolving. It is able to process and understand large amounts of text, and can use this knowledge to provide accurate and informative responses to a wide range of questions. Additionally, Assistant is able to generate its own text based on the input it receives, allowing it to engage in discussions and provide explanations and descriptions on a wide range of topics.\n" +
            "\n" +
            "Overall, Assistant is a powerful system that can help with a wide range of tasks and provide valuable insights and information on a wide range of topics. Whether you need help with a specific question or just want to have a conversation about a particular topic, Assistant is here to assist.";

    private final String HUMAN_PROMPT_TEMPLATE = "TOOLS\n" +
            "------\n" +
            "Assistant can ask the user to use tools to look up information that may be helpful in answering the users original question. The tools the human can use are:\n" +
            "\n" +
            "{tools}\n" +
            "\n" +
            "RESPONSE FORMAT INSTRUCTIONS\n" +
            "----------------------------\n" +
            "\n" +
            "When responding to me, please output a response in one of two formats:\n" +
            "\n" +
            "**Option 1:**\n" +
            "Use this if you want the human to use a tool.\n" +
            "Markdown code snippet formatted in the following schema:\n" +
            "\n" +
            "```json\n" +
            "{\n" +
            "    \"action\": string, \\ The action to take. Must be one of {tool_names}\n" +
            "    \"action_input\": string \\ The input to the action\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "**Option #2:**\n" +
            "Use this if you want to respond directly to the human. Markdown code snippet formatted in the following schema:\n" +
            "\n" +
            "```json\n" +
            "{\n" +
            "    \"action\": \"finish\",\n" +
            "    \"action_result\": {\n" +
            "             \"result\": string,\\ You should put what you want to return to use here\n" +
            "             \"imageUrl\": [string],\\ You should put the picture url into this list \n" +
            "             \"fileUrl\": [string],\\ You should put the file url into this list \n" +
            "     }\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "AGENT SCRATCHPAD\n" +
            "--------------------\n" +
            "Here is the recorded information when the agent executes tasks.\n" +
            "{agent_scratchpad}\n" +
            "\n"+
            "USER'S INPUT\n" +
            "--------------------\n" +
            "Here is the user's input (remember to respond with a markdown code snippet of a json blob with a single action, and NOTHING else):\n" +
            "\n" +
            "{input}\n";

    public ReActAgent(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        toolInfoList = new ArrayList<>();
    }

    public ReActAgent() {
        toolInfoList = new ArrayList<>();
    }

    public ReActAgent(List<ToolInfo> toolInfoList) {
        this.toolInfoList = toolInfoList;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    static
    class Action {
        private String action;
        private String action_input;
        private Map<String, Object> action_result;
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest chatCompletionRequest) {
        // limit 10 history
        chatCompletionRequest.setModel(ContextLoader.configuration.getAgentGeneralConfiguration().getModel());
        List<ChatMessage> chatMessages = chatCompletionRequest.getMessages();
        chatMessages = chatMessages.stream().filter(chatMessage -> !chatMessage.getRole().equals("system")).collect(Collectors.toList());
        if(chatMessages.size() > 11) {
            chatMessages = chatMessages.subList(chatMessages.size() - 11, chatMessages.size());
        }
        List<ChatMessage> all =  new ArrayList<>();
        all.add(ChatMessage.builder().role("system").content(SYSTEM_PROMPT).build());
        all.addAll(chatMessages);
        chatCompletionRequest.setMessages(all);
        chatMessages = chatCompletionRequest.getMessages();

        ChatMessage userMessage = chatMessages.get(chatMessages.size() - 1);
        String input = userMessage.getContent();
        String agentScratchpad = "";
        HashMap<String, Object> promptVariable = new HashMap<>();
        promptVariable.put("tools", gson.toJson(toolInfoList));
        promptVariable.put("input", input);
        String toolNames = toolInfoList.stream().map(ToolInfo::getName).collect(Collectors.joining(", "));
        promptVariable.put("tool_names", toolNames);
        promptVariable.put("agent_scratchpad", agentScratchpad);
        userMessage.setContent(StrUtil.format(HUMAN_PROMPT_TEMPLATE, promptVariable));
        int count = 0;
        ChatCompletionResult result = null;
        while (count++ < maxTryTimes) {
            log.info("智能机 {}-{} 运行第{}轮 请求参数:\n {}", agentConfig.getId(), agentConfig.getName(), count, gson.toJson(chatCompletionRequest));
            result = completionsService.completions(chatCompletionRequest);
            if(result == null) {
                break;
            }
            ChatMessage assistantMessage = result.getChoices().get(0).getMessage();
            if(assistantMessage == null) {
                break;
            }
            String action_json = extractJson(assistantMessage.getContent());
            Action action = gson.fromJson(action_json, Action.class);
            if(action == null) {
                continue;
            }
            String actionName = action.getAction();
            if(actionName == null) {
                continue;
            }
            String lowerCase = actionName.toLowerCase();
            if("finish".equals(lowerCase)) {
                Map<String, Object> actionResult = action.getAction_result();
                String msg =  (String) actionResult.get("result");
                msg = StrUtil.isBlank(msg) ? "任务完成" : msg;
                assistantMessage.setContent(msg);
                assistantMessage.setImageList((List<String>) actionResult.get("imageUrl"));
                assistantMessage.setFilepath((List<String>) actionResult.get("fileUrl"));
                break;
            }
            String call_result = null;
            try {
                if (toolNames.contains(action.getAction())) {
                    AbstractTool func = ToolManager.getInstance().getTool(action.getAction());
                    Map<String, Object> args = gson.fromJson(action.getAction_input(), new TypeToken<Map<String, Object>>() {
                    }.getType());

                    call_result = func.apply(args);
                }
            } catch (Exception e) {

            }
            String scratchpad = promptVariable.get("agent_scratchpad") + convertAction2ScratchPad(action, call_result);
            promptVariable.put("agent_scratchpad", scratchpad);
            userMessage.setContent(StrUtil.format(HUMAN_PROMPT_TEMPLATE, promptVariable));
        }
        return result;
    }

    private String convertAction2ScratchPad(Action action, String callToolResult) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Action:\n");
        stringBuilder.append(action.getAction());
        stringBuilder.append("\n");
        stringBuilder.append("Observation:\n");
        stringBuilder.append(callToolResult);
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }


}
