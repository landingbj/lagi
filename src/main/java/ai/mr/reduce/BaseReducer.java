/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * BaseReducer.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.mr.reduce;

import java.util.Map;

//import mr.IReducer;

public class BaseReducer {
	private Map<?, ?> data;

	public Map<?, ?> getParameters() {
		return this.data;
	}

	public void setParameters(Map<?, ?> data) {
		this.data = data;
	}

}
