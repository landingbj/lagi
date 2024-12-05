package ai.prompt;

import ai.config.ContextLoader;
import ai.config.pojo.PromptConfig;
import ai.llm.utils.CompletionUtil;
import ai.openai.pojo.ChatCompletionChoice;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;
import ai.utils.qa.ChatCompletionUtil;
import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        String requestString = String.format("{\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"temperature\":0.5,\"max_tokens\":1024,\"stream\":false}", answer);
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
        PromptFactory promptFactory = new PromptFactory();
        promptFactory.loadContext();
        log.info("{}",promptFactory.promptConfig);
        promptFactory.promptConfig.getPrompt().setEnable(false);
        promptFactory.savePromptConfig();
    }
}
