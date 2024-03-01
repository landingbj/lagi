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

import java.util.ArrayList;
import java.util.List;

import weixin.tools.TulingThread;
import ai.migrate.pojo.Backend;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.qa.QaSessionUtil;
import ai.utils.LagiGlobal;
import ai.utils.StringUtils;
import ai.utils.qa.ChatCompletionUtil;

public class TulingMapper extends BaseMapper implements IMapper {

    private static boolean _DEBUG_1 = false;
    private static boolean _DEBUG_2 = false;
    private static boolean _DEBUG_3 = false;
    private static boolean _DEBUG_4 = false;
    private static boolean _DEBUG_5 = false;

    private static String TURING_SENTENCE;

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
        StringBuilder sb = new StringBuilder();
        for (String s : AiGlobalQA.TURING_SENTENCES) {
            sb.append(s).append(",");
        }
        TURING_SENTENCE = sb.toString();
    }

    protected int priority;

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();

        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                LagiGlobal.CHAT_COMPLETION_REQUEST);
        Backend backendConfig = (Backend) this.getParameters().get(LagiGlobal.CHAT_COMPLETION_CONFIG);

        chatCompletionRequest = ChatCompletionUtil.cloneChatCompletionRequest(chatCompletionRequest);
        chatCompletionRequest.setModel(backendConfig.getModel());

        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);

        String textResult = handleTuling(question);

        ChatCompletionResult chatCompletionResult = ChatCompletionUtil.toChatCompletionResult(textResult, backendConfig.getModel());

        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, getPriority());
        return result;
    }

    /*
     * @see mr.IMapper#setPriority(int)
     */
    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /*
     * @see mr.IMapper#getPriority()
     */
    @Override
    public int getPriority() {
        return priority;
    }

    private String handleSession(String sessionId, String reqMessage) {
        String result = reqMessage;

        if (sessionId != null) {
            String lastAnswer = QaSessionUtil.getTulingAnswer(sessionId);

            if (lastAnswer != null && lastAnswer.length() > 0) {
                String lastChar = lastAnswer.substring(lastAnswer.length() - 1, lastAnswer.length());
                if (StringUtils.hasPunctuation(lastChar)) {
                    lastAnswer = lastAnswer.substring(0, lastAnswer.length() - 1) + "。";
                } else {
                    lastAnswer = lastAnswer + "。";
                }

                result += lastAnswer;
            }
        }

        return result;
    }

    private String handleTuling(String reqMessage) {
        String result = null;
        String respMessage = TulingThread.lookupKeywords(reqMessage);
        if (respMessage != null)
            result = respMessage;

        return result;
    }
}
