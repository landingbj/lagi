package ai.utils;

import ai.client.AiServiceCall;
import ai.client.AiServiceInfo;

public class UiServiceCall {
	private static AiServiceCall packageCall = new AiServiceCall();

	public static String getRelation() {
		Object[] params = {};
		String returnStr = packageCall.callWS(AiServiceInfo.WSUiUrl, "getRelation", params)[0];
		return returnStr;
	}
	
	public static String getNodeAttrGroup() {
		Object[] params = {};
		String returnStr = packageCall.callWS(AiServiceInfo.WSUiUrl, "getNodeAttrGroup", params)[0];
		return returnStr;
	}

	public static String addRelation(String name, String chinese, String desc, String uni_direction, String regex) {
		Object[] params = { name, chinese, desc, uni_direction, regex };
		String returnStr = packageCall.callWS(AiServiceInfo.WSUiUrl, "addRelation", params)[0];
		return returnStr;
	}

	public static String addAspect(String alias, String className, String relation) {
		Object[] params = { alias, className, relation };
		String returnStr = packageCall.callWS(AiServiceInfo.WSUiUrl, "addAspectSimple", params)[0];
		return returnStr;
	}
	
	public static String addNodeTable(String alias, String className, String dimension, String attrGid, String type, String category) {
		Object[] params = { alias, className, dimension, attrGid, type, category };
		String returnStr = packageCall.callWS(AiServiceInfo.WSUiUrl, "addNodeTable", params)[0];
		return returnStr;
	}
	
	public static String getCategories() {
		String returnStr = packageCall.callWS(AiServiceInfo.WSUniUrl, "getCategories", new Object[0])[0];
		return returnStr;
	}
}
