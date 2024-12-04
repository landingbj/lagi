package ai.llm.service;

import java.util.*;

import ai.llm.pojo.InstructionEntity;
import ai.manager.LlmInstructionManager;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.openai.pojo.ChatMessage;

public class InstructionService {
    private static final int[] CHUNK_SIZE = {512, 1000, 4000};
    private final CompletionsService completionService = new CompletionsService(LlmInstructionManager.getInstance());

    public List<InstructionEntity> getInstructionList(String content) {
        int chunkSize = CHUNK_SIZE[0];
        List<String> chunkList = splitChunks(content, chunkSize);
        List<InstructionEntity> instructionList = new ArrayList<>();

        for (String chunk : chunkList) {
            String prompt = "以下是背景信息。\n---------------------\n" + chunk + "\n---------------------\n根据以上背景信息提出一个问题，只返回该问题的内容，不要添加多余的文字。注意：问题只返回一个。";

            String question = Objects.requireNonNull(callLLM(prompt)).trim();
            prompt = "以下是背景信息。\n---------------------\n" + chunk + "---------------------\n根据以上背景信息提出一个问题，回答以下问题，只返回该问题的答案，不要添加多余的文字。: " + question + "\n";

            String answer = Objects.requireNonNull(callLLM(prompt)).trim();
            InstructionEntity entity = new InstructionEntity();
            entity.setInstruction(question);
            entity.setInput("");
            entity.setOutput(answer);
            instructionList.add(entity);
        }
        return instructionList;
    }

    private String callLLM(String question) {
        ChatCompletionRequest data = new ChatCompletionRequest();
        data.setMax_tokens(1000);
        data.setTemperature(0.8d);
        ChatMessage message = new ChatMessage();
        message.setContent(question);
        message.setRole("user");
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(message);
        data.setMessages(messages);
        ChatCompletionResult chatCompletionResult = completionService.completions(data);
        if (chatCompletionResult == null || chatCompletionResult.getChoices() == null || chatCompletionResult.getChoices().isEmpty()) {
            return null;
        }
        String result = chatCompletionResult.getChoices().get(0).getMessage().getContent();
        return result;
    }

    private List<String> splitChunks(String text, int size) {
        List<String> ret = new ArrayList<>((text.length() + size - 1) / size);
        for (int start = 0; start < text.length(); start += size) {
            ret.add(text.substring(start, Math.min(text.length(), start + size)).replaceAll("\n", ""));
        }
        return ret;
    }
}
