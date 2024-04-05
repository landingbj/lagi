/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * RepDao_Mapper.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.mr.mapper.llm;

import ai.llm.adapter.impl.GPTAdapter;
import ai.learn.questionAnswer.QuestionAnswerUtil;
import ai.learning.pojo.IndexSearchData;
import ai.learning.service.ChatCompletionService;
import ai.learning.service.IndexSearchService;
import ai.learning.service.OpenAIService;
import ai.common.pojo.Backend;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.*;
import ai.qa.AiGlobalQA;
import ai.utils.LagiGlobal;
import ai.utils.StringUtils;
import ai.utils.qa.ChatCompletionUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GPTMapper extends BaseMapper implements IMapper {
    private Gson gson = new Gson();

    private static boolean _DEBUG_1 = false;
    private static boolean _DEBUG_2 = false;
    private static boolean _DEBUG_3 = false;
    private static boolean _DEBUG_4 = false;
    private static boolean _DEBUG_5 = false;

    static {
        if (AiGlobalQA._DEBUG_LEVEL >= 5) {
            _DEBUG_5 = true;
        }
        if (AiGlobalQA._DEBUG_LEVEL >= 4) {
            _DEBUG_4 = true;
        }
        if (AiGlobalQA._DEBUG_LEVEL >= 3) {
            _DEBUG_3 = true;
        }
        if (AiGlobalQA._DEBUG_LEVEL >= 2) {
            _DEBUG_2 = true;
        }
        if (AiGlobalQA._DEBUG_LEVEL >= 1) {
            _DEBUG_1 = true;
        }
    }

    protected int priority;
    private OpenAIService openAIService = new OpenAIService();

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();

        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                LagiGlobal.CHAT_COMPLETION_REQUEST);
        Backend backendConfig = (Backend) this.getParameters().get(LagiGlobal.CHAT_COMPLETION_CONFIG);

        GPTAdapter gptAdapter = new GPTAdapter(backendConfig);

        chatCompletionRequest = ChatCompletionUtil.cloneChatCompletionRequest(chatCompletionRequest);
        chatCompletionRequest.setModel(backendConfig.getModel());
        
        ChatCompletionResult chatCompletionResult = gptAdapter.completions(chatCompletionRequest);

        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, getPriority());

        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        return result;
    }

    private String handleOpenAI(String question, String category, String tagStr, String vectorDbWhere,
            String extendPrompt) {
        if (QuestionAnswerUtil.isChat(question)) {
            return handleChatCompletion(question, category, tagStr, vectorDbWhere, extendPrompt);
        }
        return handleCompletion(question, category, tagStr, vectorDbWhere, extendPrompt);
    }

    private String handleCompletion(String question, String category, String tagStr, String vectorDbWhere,
            String extendPrompt) {
        String prompt = question;
        if (tagStr != null) {
            String data = IndexSearchService.search(question, category, vectorDbWhere, extendPrompt).get(0).getText();
            prompt = getPrompt(data, question, tagStr);
        }

        try {
            return openAIService.handleCompletion(prompt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String handleChatCompletion(String question, String category, String tagStr, String vectorDbWhere,
            String extendPrompt) {
        List<ChatMessage> messages = gson.fromJson(question, new TypeToken<List<ChatMessage>>() {
        }.getType());

        String lastMessage = messages.get(messages.size() - 1).getContent();

        List<IndexSearchData> dataList = null;

        dataList = IndexSearchService.search(lastMessage, category, vectorDbWhere, extendPrompt);

        List<ChatRequestWithContext> chatRequestWithContextList = new ArrayList<>();
        if (dataList == null) {
            ChatRequestWithContext chatRequestWithContext = new ChatRequestWithContext();
            messages.get(messages.size() - 1).setContent(lastMessage + ";");
            chatRequestWithContext.setMessages(messages);
            chatRequestWithContextList.add(chatRequestWithContext);
        } else {
            for (IndexSearchData data : dataList) {
                List<ChatMessage> messageList = new ArrayList<>();
                messageList.addAll(messages.subList(0, messages.size() - 1));
                ChatMessage lastChatMessage = new ChatMessage();
                lastChatMessage.setRole("user");
                lastChatMessage.setContent(getPrompt(data.getText(), lastMessage, tagStr));
                messageList.add(lastChatMessage);
                ChatRequestWithContext chatRequestWithContext = new ChatRequestWithContext();
                chatRequestWithContext.setMessages(messageList);
                chatRequestWithContext.setIndexSearchData(data);
                chatRequestWithContextList.add(chatRequestWithContext);
            }
        }
        String result = null;
        try {
            List<ChatResponseWithContext> responseList = ChatCompletionService
                    .concurrentHandleCompletion(chatRequestWithContextList);
            if (responseList.size() > 0) {
                result = gson.toJson(responseList);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String getPrompt(String contextText, String lastMessage, String tagStr) {
        String prompt = "以下是背景信息。\n---------------------\n" + contextText
                + "---------------------\n根据上下文信息而非先前知识，回答这个问题: " + lastMessage + ";\n";

        // if (tagStr.trim().length() > 0) {
        // String tagPrompt = ";回答需要满足如下风格要求：\n";
        // if (contextText == null) {
        // prompt = lastMessage + tagPrompt + tagStr;
        // } else {
        // prompt = prompt + tagPrompt + tagStr;
        // }
        // }
        return prompt;
    }

    private double chineseRatio(String question) {
        double result = 1d;
        int count = 0;
        char[] ch = question.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (StringUtils.isChinese(c)) {
                count++;
            }
        }
        result = count * 1.0 / ch.length;
        return result;
    }

    private boolean containsWord(String[] words, String str) {
        String tmpStr = str.toLowerCase();
        for (String word : words) {
            if (tmpStr.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }
}
