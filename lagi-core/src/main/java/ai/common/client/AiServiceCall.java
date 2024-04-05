/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * AiServiceCall.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.common.client;

import ai.common.client.impl.AiServiceCallByApi;
import ai.common.client.impl.AiServiceCallByLocal;
import ai.common.client.impl.AiServiceCallByMsg;
import ai.utils.AiGlobal;

public class AiServiceCall {

	int type = AiGlobal.AI_SERVICE_TYPE;

	/**
	 *
	 * @param urlOrClassName
	 *            远程webservice发布端口或者是本地调用类名
	 * @param method
	 *            方法名
	 * @param params
	 *            调用方法的参数
	 * @return
	 */
	public String[] callWS(String urlOrClassName, String method, Object[] params) {

		switch (type)
		{
			case AiGlobal.AI_SERVICE_BY_API:
				return AiServiceCallByApi.callWSByApi(urlOrClassName, method, params);
			case AiGlobal.AI_SERVICE_BY_MSG:
				return AiServiceCallByMsg.callWSByMsg(urlOrClassName, method, params);
			case AiGlobal.AI_SERVICE_BY_LOCAL:
				return AiServiceCallByLocal.callWSByLocal(urlOrClassName, method, params);
			default:
				return new String[] { AiGlobal.CALL_METHOD_NOT_FOUND };
		}
	}

}
