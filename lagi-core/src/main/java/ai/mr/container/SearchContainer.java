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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ai.mr.IMapper;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.mr.threads.IRMapperThread;
import ai.mr.threads.MapperThread;

public class SearchContainer extends ReduceContainer implements IRContainer {
	
	Map<String, MapperThread> threadMap = new HashMap();
	List result;

	IReducer reducer = null;
	int threadNum = 0;
	
	//加载mapper与reducer类
	public IRContainer Init() 
	{
		//reducer存放的是reducer的实现类
		this.reducer = this.reducerGroup;
		return this;
	}
	
	//运行处理
	public List running() {
		threadNum = mappersGroup.size(); //线程数等于mapper类的个数，此时为X
		
		//每个Thread里做的工作为：调用mapper的mapping方法，返回结果，再调用此类中的mapperComplete方法，this代表本类实例
		threadMap = startThread(mappersGroup, this);  
		synchronized(this) {
			try {
				if(0 != threadNum) {
					this.wait();
				}			
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		result = reducer.getResult();  //去掉优先级低的错误信息

		return result;
	}
	
	public Map<String, MapperThread> startThread(Map<String, IMapper> mappers, IRContainer container) 
	{
		String mapperName;
		IMapper mapper;
	
		Map<String, MapperThread> threadMap = new HashMap();
		Set<String> c = mappers.keySet();//mapper名称集合
		MapperThread mapperThread;
		
		Iterator<String> iterator = c.iterator();
		while (iterator.hasNext()) {
			mapperName = iterator.next();
			mapper     = mappers.get(mapperName);
			mapperThread = new IRMapperThread(mapperName, mapper, container);
			threadMap.put(mapperName, mapperThread);
			new Thread(mapperThread).start(); // 把每个线程都启动，总共有X个线程
		}
		return threadMap;
	}
	
	//mapper线程完成的处理
	public void onMapperComplete(String mapperName, List<?> list, int priority) 
	{	
		//对错误信息进行汇总，去掉重复的，并按优先级进行排序
		reducer.myReducing(mapperName, list, priority);  
		threadNum --;
		
		if(threadNum == 0) 
		{
			synchronized(this) {
				this.notify();
			}
		}
	}
	
	public void onMapperComplete(String id)	{
		
	}
	
	//mapper线程失败的处理
	public void onMapperFail(String mapperName, Integer priority, Throwable throwable)
	{
		threadNum --;
		if(threadNum == 0) 
		{
			synchronized(this) {
				this.notify();
			}
		}
	}

	//reducer线程完成的处理
	public void onReducerComplete(String reducerName) {
		
	}
	
	//reducer线程失败的处理
	public void onReducerFail(String reducerName) {
		
	}
	
	public void run()	{
		running();
	}
}
