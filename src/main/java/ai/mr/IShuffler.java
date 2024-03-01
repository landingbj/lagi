/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * IShuffler.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.mr;

import java.util.List;
import java.util.Map;

public interface IShuffler {
	public List<?> myShuffling(List<?> oldList, int priority);
	
	public Map<?,?>  getParameters();
	public void setParameters(Map<?,?> parameters);
}
