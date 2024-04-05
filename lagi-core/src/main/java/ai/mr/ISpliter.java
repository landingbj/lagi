/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * ISpliter.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.mr;

import java.util.*;

public interface ISpliter {
	public Map<String, List<?>> mySplitting(String oldList, int priority);
}
