/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * AiServiceCallByMsg.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.client.impl;

import java.io.InputStreamReader;
import java.util.Date;
import java.util.Random;

import ai.client.utils.SendAndAnalysisWS;
import ai.client.utils.WSReadSoap;
import ai.utils.AiGlobal;

/**
 * @author GeorgeDai
 *
 */
public class AiServiceCallByMsg {

	static String soapStr; //soap消息的字符串

	//通过拼接soap消息调用
	public static String[] callWSByMsg(String endpoint ,String methodName ,Object[] params )
	{
		String timestamp = AiGlobal.DUPLICATE_TOKEN_TIME + "=" + new Date().getTime();
		String random    = AiGlobal.DUPLICATE_TOKEN_RANDOM + "=" + new Random().nextInt();

		if(endpoint.indexOf("?") == -1){
			endpoint += "?" + timestamp + "&" + random;
		}
		else	{
			endpoint += "&" + timestamp + "&" + random;
		}

		WSReadSoap readSoap = new WSReadSoap();
		soapStr = readSoap.getImgSoap(methodName, params);

		InputStreamReader in = SendAndAnalysisWS.sendSoap(endpoint, soapStr);
		String[] returnStr = SendAndAnalysisWS.getReturnSoap(in,methodName);

		return returnStr;
	}
}
