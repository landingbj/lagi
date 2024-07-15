package ai.mr.reducer.llm;

/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * SearchReducer.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ai.llm.utils.CompletionUtil;
import ai.mr.IReducer;
import ai.mr.reduce.BaseReducer;
import ai.openai.pojo.ChatCompletionResult;
import ai.qa.AiGlobalQA;
import ai.utils.qa.ChatCompletionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 执行矩阵条带化计算过程中的加法运算。
 */
public class QaReducer extends BaseReducer implements IReducer {
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
    private static final Logger logger = LoggerFactory.getLogger(QaReducer.class);
    List<ChatCompletionResult> result = new ArrayList<>();

    @Override
    public void myReducing(List<?> list) {
        Map<ChatCompletionResult, Integer> resultMap = new HashMap<>();

        for (Object mapperResult : list) {
            List<?> mapperList = (List<?>) mapperResult;
            ChatCompletionResult text = (ChatCompletionResult) mapperList.get(AiGlobalQA.M_LIST_RESULT_TEXT);
            int priority = (Integer) mapperList.get(AiGlobalQA.M_LIST_RESULT_PRIORITY);

            if (text != null) {
                if (resultMap.containsKey(text)) {
                    if (priority > resultMap.get(text)) {
                        resultMap.put(text, priority);
                    }
                } else {
                    resultMap.put(text, priority);
                }
            }
        }
        ChatCompletionResult textResult = null;
        int highPriority = Integer.MIN_VALUE;

        for (Entry<ChatCompletionResult, Integer> entry : resultMap.entrySet()) {
            ChatCompletionResult text = entry.getKey();
            int priority = entry.getValue();
            if (priority > highPriority) {
                textResult = text;
                highPriority = priority;
            }
        }

        result.add(textResult);
        logger.info("QaReducer Finished Reducing...");
    }

    @Override
    public synchronized void myReducing(String mapperName, List<?> list, int priority) {
    }

    @Override
    public List<?> getResult() {
        return result;
    }
}
