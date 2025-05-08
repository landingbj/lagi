package ai.agent.mcp;

import ai.agent.Agent;
import ai.config.pojo.AgentConfig;
import ai.llm.pojo.EnhanceChatCompletionRequest;
import ai.llm.service.CompletionsService;
import ai.manager.McpManager;
import ai.mcps.SyncMcpClient;
import ai.mcps.spec.McpSchema;
import ai.openai.pojo.*;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class McpReACTAgent extends Agent<ChatCompletionRequest, ChatCompletionResult> {


    private static final Logger log = LoggerFactory.getLogger(McpReACTAgent.class);

    private final CompletionsService completionsService;

    private Gson gson = new Gson();

    private List<String> mcpNames = new ArrayList<>();

    private static final int MAX_LOOP_COUNT = 20;

    public McpReACTAgent(AgentConfig agentConfig) {
        this.agentConfig = agentConfig;
        this.completionsService = new CompletionsService();
        String mcps = agentConfig.getMcps();
        if (mcps == null) {
            throw new RuntimeException("mcp name is null");
        }
        String[] split = mcps.split(",");
        this.mcpNames.addAll(Arrays.asList(split));
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
            "    \"action\": \"Final Answer\",\n" +
            "    \"action_input\": string \\ You should put what you want to return to use here\n" +
            "}\n" +
            "```\n" +
            "\n" +
            "USER'S INPUT\n" +
            "--------------------\n" +
            "Here is the user's input (remember to respond with a markdown code snippet of a json blob with a single action, and NOTHING else):\n" +
            "\n" +
            "{input}\n" +
            "AGENT SCRATCHPAD\n" +
            "{agent_scratchpad}";

    @Override
    public ChatCompletionResult receive() {
        return null;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Builder
    static class Action  {
        private String action;
        @SerializedName("action_input")
        private String actionInput;
    }

    @Override
    public ChatCompletionResult communicate(ChatCompletionRequest data) {
        EnhanceChatCompletionRequest chatCompletionRequest = new EnhanceChatCompletionRequest();
        BeanUtil.copyProperties(data, chatCompletionRequest);
        chatCompletionRequest.setStream(false);
        List<SyncMcpClient> mcpClients = this.mcpNames.stream().map(name -> McpManager.getInstance().getNewMcpClient(name))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<Tool> tools = new ArrayList<>();
        Map<String, SyncMcpClient> toolNameSyncMcpClientHashMap = new HashMap<>();
        for (SyncMcpClient mcpClient : mcpClients) {
            try {
                mcpClient.initialize();
                McpSchema.ListToolsResult listToolsResult = mcpClient.listTools();
                List<Tool> tools1 = convert2FunctionCallTools(listToolsResult);
                if(listToolsResult.getNextCursor() != null) {
                    while (listToolsResult.getNextCursor() != null) {
                        listToolsResult = mcpClient.listTools(listToolsResult.getNextCursor());
                        List<Tool> temp = convert2FunctionCallTools(listToolsResult);
                        tools1.addAll(temp);
                    }
                }
                for (Tool tool : tools1) {
                    toolNameSyncMcpClientHashMap.put(tool.getFunction().getName(), mcpClient);
                }
                tools.addAll(tools1);
            } catch (Exception e) {
                log.error("get mcpClient error ", e);
            }
        }

        // limit 10 history
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
        promptVariable.put("tools", gson.toJson(tools));
        promptVariable.put("input", input);
        String toolNames = tools.stream().map(tool -> tool.getFunction().getName()).collect(Collectors.joining(", "));
        promptVariable.put("tool_names", toolNames);
        promptVariable.put("agent_scratchpad", agentScratchpad);
        userMessage.setContent(StrUtil.format(HUMAN_PROMPT_TEMPLATE, promptVariable));
        int count = MAX_LOOP_COUNT;
        ChatCompletionResult result = null;
        while (count-- > 0) {
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
            if("final answer".equals(lowerCase)) {
                assistantMessage.setContent(action.getActionInput());
                break;
            }
            SyncMcpClient syncMcpClient = toolNameSyncMcpClientHashMap.get(actionName);
            McpSchema.CallToolRequest callToolRequest = new McpSchema.CallToolRequest();
            callToolRequest.setName(actionName);
            Map<String, Object> arg = gson.fromJson(action.getActionInput(), new TypeToken<Map<String, Object>>(){}.getType());
            callToolRequest.setArguments(arg);
            McpSchema.CallToolResult callToolResult = syncMcpClient.callTool(callToolRequest);
            String scratchpad = promptVariable.get("agent_scratchpad") + convertAction2ScraptPad(action, callToolResult);
            promptVariable.put("agent_scratchpad", scratchpad);
            userMessage.setContent(StrUtil.format(HUMAN_PROMPT_TEMPLATE, promptVariable));
        }



        mcpClients.forEach(mcpClient -> {
            try {
                mcpClient.close();
            } catch (Exception ignored) {
            }
        });

        return result;
    }

    private String convertAction2ScraptPad(Action action, McpSchema.CallToolResult callToolResult) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Action:");
        stringBuilder.append(action.getAction());
        stringBuilder.append("\n");
        stringBuilder.append("Observation: ");
        stringBuilder.append(gson.toJson(callToolResult.getContent().get(0)));
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    public static String extractJson(String input) {
        Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }


    public McpSchema.CallToolRequest convertFunctionToolCall2McpToolCall(ToolCall toolCall) {
        Gson gson = new Gson();
        McpSchema.CallToolRequest callToolRequest = new McpSchema.CallToolRequest();
        callToolRequest.setName(toolCall.getFunction().getName());
        Map<String, Object> o = gson.fromJson(toolCall.getFunction().getArguments(), new TypeToken<Map<String, Object>>() {
        }.getType());
        callToolRequest.setArguments(o);
        return callToolRequest;
    }

    public List<Tool> convert2FunctionCallTools(McpSchema.ListToolsResult listToolsResult) {
        List<Tool> res = new ArrayList<>();
        List<McpSchema.Tool> tools = listToolsResult.getTools();
        for (McpSchema.Tool tool : tools) {
            String name = tool.getName();
            String description = tool.getDescription();
            McpSchema.JsonSchema inputSchema = tool.getInputSchema();
            Tool functionCallTool = new Tool();
            functionCallTool.setType(inputSchema.getType());
            Function function = new Function();
            function.setName(name);
            function.setDescription(description);
            Parameters parameters = new Parameters();
            BeanUtil.copyProperties(inputSchema, parameters);
            function.setParameters(parameters);
            functionCallTool.setFunction(function);
            res.add(functionCallTool);
        }
        return res;
    }

}
