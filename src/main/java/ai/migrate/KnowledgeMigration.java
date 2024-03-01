package ai.migrate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;
import ai.client.UniServiceCall;
import ai.utils.AiGlobal;
import ai.utils.CSVUtils;
import ai.utils.StringUtils;

public class KnowledgeMigration {
	private static boolean _DEBUG_1 = false;
	private static boolean _DEBUG_2 = true;

	static {
		if (AiGlobal._DEBUG_LEVEL >= 2) {
			_DEBUG_2 = true;
		}
		if (AiGlobal._DEBUG_LEVEL >= 1) {
			_DEBUG_1 = true;
		}
	}

	private static AiServiceCall wsCall = new AiServiceCall();

	public static void saveDictKeyword(String csvPath, int column) {
		List<String> words = CSVUtils.readCsvColumn(csvPath, column);
		saveDictKeyword(words);
	}

	public static void saveDictKeyword(String csvPath, int column, int batchSize) {
		List<String> words = CSVUtils.readCsvColumn(csvPath, column);
		for (int i = 0;i < words.size();i += batchSize) {
			int end = i + batchSize;
			if (end > words.size()) {
				end = words.size();
			}
			saveDictKeyword(words.subList(i, end));
		}
	}

	public static void saveDictKeyword(List<String> words) {
		Set<String> wordSet = new HashSet<>();
		for (String str : words) {
			str = str.replaceAll("\\[.*?\\]", "").trim();
			if (StringUtils.hasPunctuation(str) || StringUtils.isAllNumbers(str)
					||str.equals("") || str.length() == 1) {
				continue;
			}
			if (StringUtils.isAllHanCharacter(str)) {
				wordSet.add(str);
			}
		}

		String json = JSONObject.toJSONString(wordSet);
		String dictResult = UniServiceCall.addUserDictWords(json);
		String keywordResult = addPhrasesToPool(json);
	}

	public static void saveSentence(String csvPath, int column) {
		List<String> sentences = CSVUtils.readCsvColumn(csvPath, column);
		saveSentence(sentences);
	}

	public static void saveSentence(String csvPath, int column, int batchSize) {
		List<String> sentences = CSVUtils.readCsvColumn(csvPath, column);
		for (int i = 0;i < sentences.size();i += batchSize) {
			int end = i + batchSize;
			if (end > sentences.size()) {
				end = sentences.size();
			}
			saveSentence(sentences.subList(i, end));
		}
	}

	public static void saveSentence(List<String> sentences) {
		Set<String> sentenceSet = new HashSet<>();
		for (String str : sentences) {
			Set<String> strs = new HashSet<>(StringUtils.toShortPhrase(str));
			for (String s : strs) {
				if (StringUtils.isAllHanCharacter(s)) {
					sentenceSet.add(s);
				}
			}
		}
		String json = JSONObject.toJSONString(sentenceSet);
		String result = addSentences(json);
	}

	private static String addPhrasesToPool(String json) {
		Object[] params = { json };
		String returnStr = wsCall.callWS(AiServiceInfo.WSKngUrl, "addPhrasesToPool", params)[0];
		return returnStr;
	}

	private static String addSentences(String json) {
		Object[] params = { json };
		String returnStr = wsCall.callWS(AiServiceInfo.WSKngUrl, "addSentences", params)[0];
		return returnStr;
	}

	public static void main(String[] args) {
		String csvPath = "E:/Workspace/KnowledgeGraph/pkupie_data/data.csv";
		 
		int batchSize = 10000;

//		saveDictKeyword(csvPath, 2);
//		saveDictKeyword(csvPath, 3);
//		saveSentence(csvPath, 1);

		saveDictKeyword(csvPath, 2, batchSize);
		saveDictKeyword(csvPath, 3, batchSize);
		saveSentence(csvPath, 1, batchSize);
		
		csvPath = "E:/Workspace/KnowledgeGraph/7lore_data/7Lore_triple.csv";
		saveDictKeyword(csvPath, 1, batchSize);
		saveDictKeyword(csvPath, 2, batchSize);
		saveSentence(csvPath, 3, batchSize);
		
		csvPath = "E:/Workspace/KnowledgeGraph/ownthink_data/ownthink_v2.csv";
		saveDictKeyword(csvPath, 1, batchSize);
		saveDictKeyword(csvPath, 2, batchSize);
		saveSentence(csvPath, 3, batchSize);
	}
}
