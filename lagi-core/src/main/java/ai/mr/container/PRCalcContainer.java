/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * SearchContainer.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.mr.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import ai.mr.AiGlobalMR;
import ai.mr.IRContainer;
import ai.mr.IReducer;

public class PRCalcContainer extends ReduceContainer implements IRContainer {
	
	private static boolean _DEBUG_1 = false;
	private static boolean _DEBUG_2 = false;
	private static boolean _DEBUG_3 = false;

	static {
		if (AiGlobalMR._DEBUG_LEVEL >= 3) {
			_DEBUG_3 = true;
		}
		if (AiGlobalMR._DEBUG_LEVEL >= 2) {
			_DEBUG_2 = true;
		}
		if (AiGlobalMR._DEBUG_LEVEL >= 1) {
			_DEBUG_1 = true;
		}
	}
	
	private List<?> result;
	private Map<String, List<?>> mapperResult = new ConcurrentHashMap<>();
	private CountDownLatch latch = new CountDownLatch(0);
	private IReducer reducer = null;
	
	//加载mapper与reducer类
	@Override
	public IRContainer Init() 
	{
		//reducer存放的是reducer的实现类
		this.reducer = this.reducerGroup;
		return this;
	}
	
	//运行处理
	@Override
	public List<?> running() {
		latch = new CountDownLatch(mappersGroup.size());
if(_DEBUG_3) {
		System.out.println(String.format("[DEBUG-3] Initializing (%d) Mappers", latch.getCount()));
}

		//每个Thread里做的工作为：调用mapper的mapping方法，返回结果，再调用此类中的mapperComplete方法，this代表本类实例
		startThread(mappersGroup, this);  
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return result;
		}
		
		List<List<?>> mapResult = new ArrayList<>();
		for(List<?> o : mapperResult.values()) {
			mapResult.add(o);
		}

		try {
			reducer.myReducing(mapResult);
		} catch(Exception e) {
			this.onReducerFail(reducer.getClass().getCanonicalName());
		}
		this.onReducerComplete(reducer.getClass().getCanonicalName());
		result = reducer.getResult();  //去掉优先级低的错误信息

		return result;
	}

	//mapper线程完成的处理
	@Override
	public void onMapperComplete(String mapperName, List<?> list, int priority) {	
		mapperResult.put(mapperName, list);
		latch.countDown();
		
if(_DEBUG_3) {
		System.out.println(String.format("[DEBUG-3] Mapper (%s) Complete: %d/%d remaining", mapperName, latch.getCount(), mappersGroup.size()));
}
	}

	@Override
	public void onMapperComplete(String id)	{
		latch.countDown();
		
if(_DEBUG_3) {
		System.out.println(String.format("[DEBUG-3] Mapper (%s) Complete: %d/%d remaining", id, latch.getCount(), mappersGroup.size()));
}
	}

	//mapper线程失败的处理
	@Override
	public void onMapperFail(String mapperName, Integer priority, Throwable throwable) {
		latch.countDown();
		
if(_DEBUG_3) {
		System.out.println(String.format("[DEBUG-3] Mapper (%s) Failed: %d/%d remaining", mapperName, latch.getCount(), mappersGroup.size()));
}
	}

	//reducer线程完成的处理
	@Override
	public void onReducerComplete(String reducerName) {

		if(_DEBUG_3) {
			System.out.println(String.format("[DEBUG-3] Reducer (%s) Complete", reducerName));
		}
		
	}

	//reducer线程失败的处理
	@Override
	public void onReducerFail(String reducerName) {
		if(_DEBUG_3) {
			System.out.println(String.format("[DEBUG-3] Reducer (%s) Failed", reducerName));
		}
	}

	@Override
	public void run()	{
		running();
	}
}
