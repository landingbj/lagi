package ai.workflow.mapper;

import ai.common.pojo.Backend;
import ai.learn.questionAnswer.KShingle;
import ai.llm.adapter.ILlmAdapter;
import ai.llm.service.CompletionsService;
import ai.mr.IMapper;
import ai.mr.mapper.BaseMapper;
import ai.openai.pojo.ChatCompletionRequest;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.LagiGlobal;
import ai.utils.OkHttpUtil;
import ai.utils.qa.ChatCompletionUtil;
import ai.worker.WorkerGlobal;
import com.google.gson.Gson;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RagMapper extends CiticMapper implements IMapper {
    protected int priority;
    private static final Logger logger = LoggerFactory.getLogger(RagMapper.class);

    private final Gson gson = new Gson();

    private final String BAD_CASE = "我并不了解xxx具体的信息,分享更多关于xxx的信息";

    @Override
    public List<?> myMapping() {
        List<Object> result = new ArrayList<>();
        ChatCompletionRequest chatCompletionRequest = (ChatCompletionRequest) this.getParameters().get(
                WorkerGlobal.MAPPER_CHAT_REQUEST);
        String url = (String) this.getParameters().get(WorkerGlobal.MAPPER_RAG_URL);
        String responseJson = null;
        try {
            responseJson = OkHttpUtil.post(url + "/v1/chat/completions", gson.toJson(chatCompletionRequest));
        } catch (IOException e) {
            logger.error("RagMapper.myMapping: OkHttpUtil.post error", e);
        }
        ChatCompletionResult chatCompletionResult = null;
        double calPriority = 0;
        double positive = 0;
        double negative = 0;
        if (responseJson != null) {
            chatCompletionResult = gson.fromJson(responseJson, ChatCompletionResult.class);
            positive = getSimilarity(chatCompletionRequest, chatCompletionResult);
            negative = getBadCaseSimilarity(BAD_CASE, chatCompletionResult);
            calPriority = calculatePriority(positive, negative, getPriority());
        }

        result.add(AiGlobalQA.M_LIST_RESULT_TEXT, chatCompletionResult);
        logger.info("RagMapper.myMapping: positive = " + positive);
        logger.info("RagMapper.myMapping: negative = " + negative);
        logger.info("RagMapper.myMapping: calPriority = " + calPriority);
        result.add(AiGlobalQA.M_LIST_RESULT_PRIORITY, calPriority);
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
