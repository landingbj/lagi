/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * AiServiceCallByApi.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.client.impl;

import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import ai.client.utils.ReqAndResSoapMessage;
import ai.utils.AiGlobal;

public class AiServiceCallByApi {

	public static String[] callWSByApi(String endpoint ,String method ,Object[] params )
	{
		String[] returnStr =null;

		String timestamp = AiGlobal.DUPLICATE_TOKEN_TIME + "=" + new Date().getTime();
		String random    = AiGlobal.DUPLICATE_TOKEN_RANDOM + "=" + new Random().nextInt();

		if(endpoint.indexOf("?") == -1){
			endpoint += "?" + timestamp + "&" + random;
		}
		else	{
			endpoint += "&" + timestamp + "&" + random;
		}

		try {
			List<Object> list = ReqAndResSoapMessage.getEnvelope();
			SOAPBody body  = (SOAPBody)list.get(0);
			SOAPConnection connection = (SOAPConnection)list.get(1);
			SOAPMessage message = (SOAPMessage) list.get(2);

			//创建消息body内容。具体参数的配置可以参照应用集成接口技术规范1.1版本
			SOAPElement saveUrlAs = body.addChildElement(method,"q0");
			if (params!=null)
			{
				for(int i=0; i<params.length; i++){

					String arg = "arg"+i;
					//添加内容
					if (params[i]!=null) {
						if(params[i].getClass().isArray())
						{
							for (Object object : (String[])params[i]) {
								SOAPElement arg0= saveUrlAs.addChildElement(arg);
								arg0.addTextNode((String)object);
							}
						}
						else{
							SOAPElement arg0= saveUrlAs.addChildElement(arg);
							arg0.addTextNode(String.valueOf(params[i]));
						}
					}
				}
			}
			message.saveChanges();

			// 响应消息
			SOAPMessage reply = connection.call(message, endpoint);
			connection.close();
			returnStr=(String[])ReqAndResSoapMessage.responseMessage(reply);
		}
		catch (SOAPException e) {
			e.printStackTrace();
		}
		return returnStr;
	}
}
