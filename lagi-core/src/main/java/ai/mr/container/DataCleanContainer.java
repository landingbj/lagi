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

import ai.mr.IRContainer;
import ai.mr.IReducer;

public class DataCleanContainer extends ReduceContainer implements IRContainer {
	
	private List<?> result;
	private Map<String, List<?>> mapperResult = new ConcurrentHashMap<>();
	private CountDownLatch latch = new CountDownLatch(0);
	private IReducer reducer = null;
	
	public DataCleanContainer(int cpuCores) {
		super(cpuCores);
	}
	
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
		reducer.myReducing(mapResult);  
		result = reducer.getResult();  //去掉优先级低的错误信息

		return result;
	}

	//mapper线程完成的处理
	@Override
	public void onMapperComplete(String mapperName, List<?> list, int priority) {	
		mapperResult.put(mapperName, list);
		latch.countDown();
	}

	@Override
	public void onMapperComplete(String id)	{
		latch.countDown();
	}

	//mapper线程失败的处理
	@Override
	public void onMapperFail(String mapperName, Integer priority, Throwable throwable) {
		latch.countDown();
	}

	//reducer线程完成的处理
	@Override
	public void onReducerComplete(String reducerName) {

	}

	//reducer线程失败的处理
	@Override
	public void onReducerFail(String reducerName) {

	}

	@Override
	public void run()	{
		running();
	}
}
