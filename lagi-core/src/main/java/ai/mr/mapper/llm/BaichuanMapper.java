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

import ai.common.pojo.Backend;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.adapter.impl.BaichuanAdapter;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;

import java.util.ArrayList;
import java.util.List;

public class BaichuanMapper extends BaseMapper implements IMapper {

    protected int priority;

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();

        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                LagiGlobal.CHAT_COMPLETION_REQUEST);
        Backend backendConfig = (Backend) this.getParameters().get(LagiGlobal.CHAT_COMPLETION_CONFIG);

        ILlmAdapter adapter = new BaichuanAdapter(backendConfig);
        chatCompletionRequest = ChatCompletionUtil.cloneChatCompletionRequest(chatCompletionRequest);
        chatCompletionRequest.setModel(backendConfig.getModel());
        ChatCompletionResult chatCompletionResult = adapter.completions(chatCompletionRequest);

        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, getPriority());
        return result;
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
