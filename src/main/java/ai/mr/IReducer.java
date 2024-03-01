/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * IReducer.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.mr;

import java.util.List;
import java.util.Map;

public interface IReducer {

	public void myReducing(String id, List<?> list, int priority);
	public void myReducing(List<?> list);
	
	public Map<?,?>  getParameters();
	public void setParameters(Map<?,?> parameters);

	public List<?> getResult();
}
