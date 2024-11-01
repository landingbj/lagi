/*
 * This program is commercial software; you can only redistribute it and/or modify
 * it under the WARRANTY of Beijing Landing Technologies Co. Ltd.
 *
 * You should have received a copy license along with this program;
 * If not, write to Beijing Landing Technologies, service@landingbj.com.
 */

/*
 * MapperThread.java
 * Copyright (C) 2018 Beijing Landing Technologies, China
 */

package ai.mr.threads;

import java.util.List;

import ai.mr.IMapper;
import ai.mr.IRContainer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IRMapperThread extends MapperThread {
	IMapper mapper = null;
	IRContainer container = null;
	String mapperName = null;
	int priority;

	public IRMapperThread(String mapperName, IMapper mapper,
			IRContainer container) {
		super(mapperName, mapper, container);
		this.mapperName = mapperName;
		this.mapper = mapper;
		this.container = container;
		this.priority = mapper.getPriority();
	}

	public void run() {
		List<?> list = null;
		try {
			list = mapper.myMapping();
			container.onMapperComplete(mapperName, list, priority);
		} catch (Exception e) {
			container.onMapperFail(mapperName, priority, e);
			log.error(e.getMessage());
		}
	}

	public String getId() {
		return mapperName;
	}

	public void setId(String mapperName) {
		this.mapperName = mapperName;
	}
}
