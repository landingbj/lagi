package ai.workflow.reducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ai.llm.pojo.ChatCompletionResultWithSource;
import ai.mr.IReducer;
import ai.mr.reduce.BaseReducer;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.router.pojo.RouteAgentResult;
import ai.utils.qa.ChatCompletionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentReducer extends BaseReducer implements IReducer {
    private static final Logger logger = LoggerFactory.getLogger(AgentReducer.class);
    List<RouteAgentResult> result = new ArrayList<>();

    @Override
    public void myReducing(List<?> list) {
        Map<RouteAgentResult, Double> resultMap = new HashMap<>();
        for (Object mapperResult : list) {
            List<?> mapperList = (List<?>) mapperResult;
            RouteAgentResult chatCompletionResult = (RouteAgentResult) mapperList.get(AiGlobalQA.M_LIST_RESULT_TEXT);
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
        RouteAgentResult textResult = null;
        double highPriority = -1;
        for (Entry<RouteAgentResult, Double> entry : resultMap.entrySet()) {
            RouteAgentResult chatCompletionResult = entry.getKey();
            double priority = entry.getValue();
            if (priority > highPriority) {
                textResult = chatCompletionResult;
                highPriority = priority;
            }
            logger.info("AgentReducer {}-{}:  text = {}, priority = {}", getSource(chatCompletionResult), getSourceId(chatCompletionResult),   ChatCompletionUtil.getFirstAnswer(chatCompletionResult.getResult().get(0)), priority);
        }
        result.add(textResult);
        logger.info("AgentReducer Finished Reducing...");
    }

    private String getSource(RouteAgentResult chatCompletionResult) {
        List<ChatCompletionResult> results = chatCompletionResult.getResult();
        if(results != null && !results.isEmpty()) {
            ChatCompletionResult chatCompletionResult1 = results.get(0);
            if(chatCompletionResult1 instanceof ChatCompletionResultWithSource) {
                return ((ChatCompletionResultWithSource)chatCompletionResult1).getSource();
            }
        }
        return "unknown";
    }

    private Integer getSourceId(RouteAgentResult chatCompletionResult) {
        List<ChatCompletionResult> results = chatCompletionResult.getResult();
        if(results != null && !results.isEmpty()) {
            ChatCompletionResult chatCompletionResult1 = results.get(0);
            if(chatCompletionResult1 instanceof ChatCompletionResultWithSource) {
                return ((ChatCompletionResultWithSource)chatCompletionResult1).getSourceId();
            }
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public synchronized void myReducing(String mapperName, List<?> list, int priority) {
    }

    @Override
    public List<?> getResult() {
        return result;
    }
}
