/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * IContainer.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.mr;

import java.util.List;
import java.util.Map;

public interface IRContainer extends IContainer	{

	public void onMapperComplete(String id, List<?> list, int priority);

	public List<?> running();

	public IRContainer Init();

	public IMapper registMapper(String mapperName, int concurrNum, Map<String, ?> parameters);
	
	public IShuffler registShuffler(String shufflerName, int concurrNum, Map<String, ?> parameters);
	
	public IReducer registReducer(String reducerName, int concurrNum, Map<String, ?> parameters);
	
	public void setMapperPriority(String mapperName, int priority);
	
	public void registerReducer(IReducer reducer);
}
