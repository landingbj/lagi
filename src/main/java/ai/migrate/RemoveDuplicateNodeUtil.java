package ai.migrate;

import ai.client.UniServiceCall;

public class RemoveDuplicateNodeUtil {
	public static void main(String[] args) {
		String[] tables = { "atomicSymptom", "action", "cause", "atomicSolution", 
				"reference", "severity", "impact", "suppliment", "event", 
				"sqlCode", "sqlStat", "source", "errorNumber", "component", 
				"errorMessage", "errorDesc", "version", "product", };

		for (String table : tables) {
			String result = UniServiceCall.removeDuplicateNode(table);
		}
	}
}
