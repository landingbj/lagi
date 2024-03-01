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

package ai.migrate.mr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.mr.container.ReduceContainer;

public class SearchContainer extends ReduceContainer implements IRContainer {
	private List<?> result;
	private List<Object> mapperResult = Collections
			.synchronizedList(new ArrayList<>());
	private CountDownLatch latch = new CountDownLatch(0);
	private IReducer reducer = null;

	// 加载mapper与reducer类
	@Override
	public IRContainer Init() {
		// reducer存放的是reducer的实现类
		this.reducer = this.reducerGroup;
		return this;
	}

	// 运行处理
	@Override
	public List<?> running() {
		latch = new CountDownLatch(mappersGroup.size());

		// 每个Thread里做的工作为：调用mapper的mapping方法，返回结果，再调用此类中的mapperComplete方法，this代表本类实例
		startThread(mappersGroup, this);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return result;
		}
		reducer.myReducing(mapperResult);
		result = reducer.getResult(); // 去掉优先级低的错误信息

		return result;
	}

	// mapper线程完成的处理
	@Override
	public void onMapperComplete(String mapperName, List<?> list, int priority) {
		mapperResult.add(list);
		latch.countDown();
	}

	@Override
	public void onMapperComplete(String id) {
		latch.countDown();
	}

	// mapper线程失败的处理
	@Override
	public void onMapperFail(String mapperName) {
		latch.countDown();
	}

	// reducer线程完成的处理
	@Override
	public void onReducerComplete(String reducerName) {

	}

	// reducer线程失败的处理
	@Override
	public void onReducerFail(String reducerName) {

	}

	@Override
	public void run() {
		running();
	}

	@Override
	public void close() {
		super.asynClose();
	}
}
