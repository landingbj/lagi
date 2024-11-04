package ai.mr.mapper.llm;

import ai.common.pojo.Backend;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.service.CompletionsService;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class UniversalMapper extends BaseMapper implements IMapper {

    @Getter
    private final ILlmAdapter adapter;

    public UniversalMapper(ILlmAdapter adapter) {
        super();
        this.adapter = adapter;
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
        ChatCompletionResult chatCompletionResult = adapter.completions(chatCompletionRequest);
        CompletionsService.unfreezeAdapter(adapter);
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
