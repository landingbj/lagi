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

package ai.mr.reduce;

import java.util.ArrayList;
import java.util.List;

import ai.mr.AiGlobalMR;
import ai.mr.IReducer;

/**
 * 执行矩阵条带化计算过程中的加法运算。
 * 
 */
public class QueryReducer extends BaseReducer implements IReducer {
	private static boolean _DEBUG_1 = false;
	private static boolean _DEBUG_2 = false;
	private static boolean _DEBUG_3 = false;
	private static boolean _DEBUG_4 = false;
	private static boolean _DEBUG_5 = false;

	static {
		if (AiGlobalMR._DEBUG_LEVEL >= 5) {
			_DEBUG_5 = true;
		}
		if (AiGlobalMR._DEBUG_LEVEL >= 4) {
			_DEBUG_4 = true;
		}
		if (AiGlobalMR._DEBUG_LEVEL >= 3) {
			_DEBUG_3 = true;
		}
		if (AiGlobalMR._DEBUG_LEVEL >= 2) {
			_DEBUG_2 = true;
		}
		if (AiGlobalMR._DEBUG_LEVEL >= 1) {
			_DEBUG_1 = true;
		}
	}

	List<Object> result = new ArrayList<>();;

	@Override
	public void myReducing(List<?> list) {
		if (_DEBUG_3) {
			System.out.println("[DEBUG-3]" + "Coming into Reducing...");
		}

		for (Object mapperResult : list) {
			List<?> mapperList = (List<?>) mapperResult;
			result.addAll(mapperList);
		}

		if (_DEBUG_3) {
			System.out.println("[DEBUG-3]" + "Finishing Reducing...");
		}
	}

	@Override
	public synchronized void myReducing(String mapperName, List<?> list, int priority) {
	}

	@Override
	public List<?> getResult() {
		return result;
	}
}
