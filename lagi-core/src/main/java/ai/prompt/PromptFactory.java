package ai.prompt;

import ai.config.ContextLoader;
import ai.config.pojo.PromptConfig;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PromptFactory {
    String prompt = "input your prompt here %s";
    private PromptConfig promptConfig;
    private static final Logger log = LoggerFactory.getLogger(PromptFactory.class);
    private static final PromptFactory INSTANCE = new PromptFactory();
    public static PromptFactory getInstance() {
        return INSTANCE;
    }
    private Gson gson = new Gson();
    private PromptFactory() {
        loadContext();
    }

    public ChatCompletionRequest loadPrompt(ChatCompletionRequest request) {
        loadContext();
        ChatMessage message = request.getMessages().get(request.getMessages().size() - 1);
        String prompt = promptConfig.getPrompt().getRoles().stream()
                .findFirst()
                .map(PromptConfig.Role::getPrompt)
                .orElse("%s");
        message.setContent(String.format(prompt, message.getContent()));
        return request;
    }

    public ChatCompletionRequest loadPrompt(ChatCompletionResult result) {
        loadContext();
        String answer = ((ChatCompletionChoice)result.getChoices().get(result.getChoices().size() - 1)).getMessage().getContent();
        String prompt = promptConfig.getPrompt().getRoles().stream()
                .findFirst()
                .map(item-> {
                    return StringUtils.isNotBlank(item.getPrompt()) ? item.getPrompt().replaceAll("%s", answer) : "%s";
                })
                .orElse("%s");
        String requestString = String.format("{\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"temperature\":0.001,\"max_tokens\":4096,\"stream\":false}", prompt);
        log.info("promptFactory : {}", requestString);
        return gson.fromJson(requestString, ChatCompletionRequest.class);
    }



    public ChatCompletionRequest loadPrompt(ChatCompletionRequest request,String roleName) {
        loadContext();
        ChatMessage message = request.getMessages().get(request.getMessages().size() - 1);
        String prompt = promptConfig.getPrompt().getRoles().stream()
                .filter(role -> role.getName().equals(roleName))
                .findFirst()
                .map(PromptConfig.Role::getPrompt)
                .orElse("%s");
        message.setContent(String.format(prompt, message.getContent()));
        return request;
    }
    private void loadContext() {
        try {

//            InputStream resourceAsStream = Files.newInputStream(Paths.get("lagi-web/src/main/resources/prompt_template.yml"));
            InputStream resourceAsStream = ContextLoader.class.getResourceAsStream("/prompt_template.yml");
            ObjectMapper mapper = new YAMLMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            promptConfig = mapper.readValue(resourceAsStream, PromptConfig.class);
            log.info("{}",promptConfig);

        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }
    public PromptConfig getPromptConfig() {
        return promptConfig;
    }


    public void savePromptConfig() {
        try {
            ObjectMapper mapper = new YAMLMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            // 更新prompt_template.yml文件
            String configFilePath = getClass().getClassLoader().getResource("/prompt_template.yml").getPath();
            mapper.writeValue(new File(configFilePath), promptConfig);
        } catch (IOException e) {
            log.warn(e.getMessage());
        }
    }
    public static void main(String[] args) {
        String pattern = "你是一个中信金融方面的业务人员，请将问题和回答进行包装。 \\n问题：\\n###%s###\\n回答：\\n###%s###\\n要求: 如果是日常回答，则按提供的原文提供，如果是金融、股票或者汇率的问题，则以专业的方式回答。 请直接输出结果";

        String msg = pattern.replaceAll("%s", "你好吗？");
        System.out.println(msg);
    }
}
