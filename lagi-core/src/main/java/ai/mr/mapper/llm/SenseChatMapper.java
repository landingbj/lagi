package ai.mr.mapper.llm;

import ai.common.pojo.Backend;
import ai.llm.adapter.impl.SenseChatAdapter;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.LagiGlobal;
import ai.utils.qa.ChatCompletionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SenseChatMapper extends BaseMapper implements IMapper {
    private static Logger logger = LoggerFactory.getLogger(SenseChatMapper.class);

    protected int priority;
    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();

        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                LagiGlobal.CHAT_COMPLETION_REQUEST);
        Backend backendConfig = (Backend) this.getParameters().get(LagiGlobal.CHAT_COMPLETION_CONFIG);
        SenseChatAdapter senseChatAdapter = new SenseChatAdapter(backendConfig);
        chatCompletionRequest = ChatCompletionUtil.cloneChatCompletionRequest(chatCompletionRequest);
        chatCompletionRequest.setModel(backendConfig.getModel());
        ChatCompletionResult chatCompletionResult = senseChatAdapter.completions(chatCompletionRequest);
        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, getPriority());
        String question = ChatCompletionUtil.getLastMessage(chatCompletionRequest);
        String answer = ChatCompletionUtil.getFirstAnswer(chatCompletionResult);
        logger.info("ErnieMapper question: " + question);
        logger.info("ErnieMapper answer: " + answer);
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
