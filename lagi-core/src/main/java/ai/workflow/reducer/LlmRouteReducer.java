package ai.workflow.reducer;

import ai.mr.IReducer;
import ai.mr.reduce.BaseReducer;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.router.pojo.RouteCompletionResult;
import ai.utils.qa.ChatCompletionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LlmRouteReducer extends BaseReducer implements IReducer {
    private static final Logger logger = LoggerFactory.getLogger(LlmRouteReducer.class);
    List<RouteCompletionResult> result = new ArrayList<>();

    @Override
    public void myReducing(List<?> list) {
        Map<RouteCompletionResult, Double> resultMap = new HashMap<>();
        for (Object mapperResult : list) {
            List<?> mapperList = (List<?>) mapperResult;
            RouteCompletionResult chatCompletionResult = (RouteCompletionResult) mapperList.get(AiGlobalQA.M_LIST_RESULT_TEXT);
            if (chatCompletionResult != null) {
                Double priority = chatCompletionResult.getPriority();
                if (resultMap.containsKey(chatCompletionResult)) {
                    if (priority > resultMap.get(chatCompletionResult)) {
                        resultMap.put(chatCompletionResult, priority);
                    }
                } else {
                    resultMap.put(chatCompletionResult, priority);
                }
            }
        }
        RouteCompletionResult textResult = null;
        double highPriority = -1;
        for (Entry<RouteCompletionResult, Double> entry : resultMap.entrySet()) {
            RouteCompletionResult chatCompletionResult = entry.getKey();
            double priority = entry.getValue();
            if (priority > highPriority) {
                textResult = chatCompletionResult;
                highPriority = priority;
            }
            logger.info("LlmRouteReducer: text = {}, priority = {}",
                    ChatCompletionUtil.getFirstAnswer(chatCompletionResult.getResult()), priority);
        }
        result.add(textResult);
        logger.info("LlmRouteReducer Finished Reducing...");
    }

    @Override
    public synchronized void myReducing(String mapperName, List<?> list, int priority) {
    }

    @Override
    public List<?> getResult() {
        return result;
    }
}
