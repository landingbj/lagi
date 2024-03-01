/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * MrRegexUtil.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.utils.mr;

import java.util.regex.Matcher;
import java.util.regex.Pattern; //import java.io.*;
import java.util.*;

public class MrRegexUtil {
	// 在input里找第一个和regex相符合的结果
	public static String getFirstMatch(String input, String regex) {
		String result = null;
		Pattern pt = Pattern.compile(regex);
		Matcher mch = pt.matcher(input);
		if (mch.find()) {
			result = mch.group();
		}
		return result;
	}

	// 在input里找所有和regex相符合的结果
	public static List<String> getAllMatch(String input, String regex) {
		List<String> result = new ArrayList<String>();
		Pattern pt = Pattern.compile(regex);
		Matcher mch = pt.matcher(input);
		while (mch.find()) {
			result.add(mch.group());
		}
		return result;
	}

	// 用来提取多组件号，多错误号的内容
	public static Map<String, List<String>> parseCompNum(String input) {
		Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
		List<String> tempList = getAllMatch(input,
				"\\W*[a-zA-Z_]+(\\W*\\d+)+\\W*");
		for (String tempString : tempList) {
			String s1 = getFirstMatch(tempString, "[a-zA-Z_]+");
			List<String> s2 = getAllMatch(tempString, "\\d+");
			if (!resultMap.containsKey(s1)) {
				resultMap.put(s1, s2);
			} else {
				List<String> s3 = resultMap.get(s1);
				s3.addAll(s2);
				resultMap.put(s1, s3);
			}
		}
		return resultMap;
	}

	// 提取input里所有regexOuter，再在每一个regexOuter里提取符合的regexInner
	public static List<List<String>> multiParse(String input,
			String regexOuter, String regexInner) {
		List<List<String>> resultList = new ArrayList<List<String>>();
		List<String> outerList = getAllMatch(input, regexOuter);
		for (String str : outerList) {
			List<String> innerList = getAllMatch(str, regexInner);
			resultList.add(innerList);
		}
		return resultList;
	}

	public static String formatErrorTitle(String errorTitle) {
		String errorTitlePrefixRegex = "\\p{Punct}+.*";
		String commasRegex = "\\p{Punct}+";
		Pattern pt = Pattern.compile(commasRegex);
		Matcher mch = pt.matcher(errorTitle);
		errorTitle = errorTitle.trim();
		if (errorTitle.matches(errorTitlePrefixRegex)) {
			List<String> matchs = new ArrayList<String>();
			while (mch.find()) {
				matchs.add(mch.group());
			}
			for (String match : matchs) {
				System.out.println(match);
			}
			if (matchs.size() == 1) {
				errorTitle = errorTitle.replace(matchs.get(0), "").trim();

			} else if (matchs.size() >= 2
					&& errorTitle.matches("\\p{Punct}+.*\\p{Punct}+")) {
				errorTitle = errorTitle.replaceFirst(matchs.get(0), "").trim();
				errorTitle = errorTitle.replace(matchs.get(matchs.size() - 1),
						"").trim();
			} else if (matchs.size() >= 2
					&& !errorTitle.matches("\\p{Punct}+.*\\p{Punct}+")) {
				errorTitle = errorTitle.replace(matchs.get(0), "").trim();
			}
		} else {
			List<String> matchs = new ArrayList<String>();
			while (mch.find()) {
				matchs.add(mch.group());
			}
			if (matchs.size() != 0 && errorTitle.matches(".*\\p{Punct}+")) {
				errorTitle = errorTitle.replace(matchs.get(matchs.size() - 1),
						"").trim();
			}
		}
		return errorTitle;
	}

}
