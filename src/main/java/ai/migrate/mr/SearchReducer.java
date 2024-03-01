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

package ai.migrate.mr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ai.index.AiGlobalIndex;
import ai.migrate.pojo.TermWord;
import ai.mr.IReducer;
import ai.mr.reduce.BaseReducer;
import ai.qa.AiGlobalQA;

/**
 * 执行矩阵条带化计算过程中的加法运算。
 * 
 */
public class SearchReducer extends BaseReducer implements IReducer {
	List<TermWord> result = new ArrayList<>();;

	@Override
	public void myReducing(List<?> list) {
		for (Object mapperResult : list) {
			List<?> mapperList = (List<?>) mapperResult;
			List<TermWord> termWordList = (List<TermWord>) mapperList.get(AiGlobalQA.M_LIST_RESULT_TEXT);
			result.addAll(termWordList);
		}
	}

	@Override
	public synchronized void myReducing(String mapperName, List<?> list,
			int priority) {
	}

	@Override
	public List<?> getResult() {
		return result;
	}
}
