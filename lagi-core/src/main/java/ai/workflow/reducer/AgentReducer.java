package ai.workflow.reducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ai.learn.questionAnswer.KShingle;
import ai.mr.IReducer;
import ai.mr.reduce.BaseReducer;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.qa.ChatCompletionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentReducer extends BaseReducer implements IReducer {
    private static final Logger logger = LoggerFactory.getLogger(AgentReducer.class);
    List<ChatCompletionResult> result = new ArrayList<>();

    @Override
    public void myReducing(List<?> list) {
        Map<ChatCompletionResult, Double> resultMap = new HashMap<>();
        for (Object mapperResult : list) {
            List<?> mapperList = (List<?>) mapperResult;
            ChatCompletionResult chatCompletionResult = (ChatCompletionResult) mapperList.get(AiGlobalQA.M_LIST_RESULT_TEXT);
            Double priority = (Double) mapperList.get(AiGlobalQA.M_LIST_RESULT_PRIORITY);
            if (chatCompletionResult != null) {
                if (resultMap.containsKey(chatCompletionResult)) {
                    if (priority > resultMap.get(chatCompletionResult)) {
                        resultMap.put(chatCompletionResult, priority);
                    }
                } else {
                    resultMap.put(chatCompletionResult, priority);
                }
            }
        }
        ChatCompletionResult textResult = null;
        double highPriority = -1;
        for (Entry<ChatCompletionResult, Double> entry : resultMap.entrySet()) {
            ChatCompletionResult chatCompletionResult = entry.getKey();
            double priority = entry.getValue();
            if (priority > highPriority) {
                textResult = chatCompletionResult;
                highPriority = priority;
            }
            logger.info("AgentReducer: text = {}, priority = {}", ChatCompletionUtil.getFirstAnswer(chatCompletionResult), priority);
        }
        result.add(textResult);
        logger.info("AgentReducer Finished Reducing...");
    }

    @Override
    public synchronized void myReducing(String mapperName, List<?> list, int priority) {
    }

    @Override
    public List<?> getResult() {
        return result;
    }
}
