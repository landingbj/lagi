package ai.agent.customer.tools;

import ai.utils.ApiInvokeUtil;
import ai.agent.customer.pojo.ToolArg;
import ai.agent.customer.pojo.ToolInfo;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Setter
public class DeepSeekChatTool extends AbstractTool {

    private static final String API_ADDRESS = "https://api.pearktrue.cn/api/deepseek/";
    private String token = "";

    public DeepSeekChatTool() {
        init();
    }

    private void init() {
        name = "deepseek_chat";
        toolInfo = ToolInfo.builder().name("deepseek_chat")
                .description("这是一个基于 DeepSeekChat 的智能问答工具，可以帮助用户进行对话交流")
                .args(Lists.newArrayList(
                        ToolArg.builder().name("messages").type("list").description("对话消息列表").build()
                )).build();
        register(this);
    }

    public DeepSeekChatTool(String token) {
        this.token = token;
        init();
    }

    private String chat(List<Map<String, String>> messages) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", token);

        Map<String, Object> body = new HashMap<>();
        body.put("messages", messages);

        String bodyStr = new Gson().toJson(body);

        String post = ApiInvokeUtil.post(API_ADDRESS, headers, bodyStr, 15, TimeUnit.SECONDS);

        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(post, Map.class);

        if (map == null || !map.get("code").equals(200.0)) {
            return "获取失败";
        }

        return (String) map.get("message");
    }


    @Override
    public String apply(Map<String, Object> args) {
        List<Map<String, String>> messages = (List<Map<String, String>>) args.get("messages");
        return chat(messages);
    }

    public static void main(String[] args) {
        String token = "xxx";
        DeepSeekChatTool deepSeekChatTool = new DeepSeekChatTool(token);
        List<Map<String, String>> messages = Lists.newArrayList();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");
        messages.add(systemMessage);
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", "你好啊？");
        messages.add(userMessage);

        String result = deepSeekChatTool.chat(messages);
        System.out.println(result);
    }
}
