package ai.mr.threads;

import ai.mr.IContainer;
import ai.mr.IMapper;

public class MapperThread implements Runnable {

	IMapper mapper 		 = null;
	IContainer container = null;
	String mapperName    = null;
	int    priority      = 0;

	public MapperThread(String mapperName, IMapper mapper, IContainer container) {
		this.mapperName = mapperName;
		this.mapper = mapper;
		this.container = container;
	}

	public void run() {
		try {
			mapper.myMapping();
		} 
		catch (Exception e) {
			container.onMapperFail(mapperName, null, e);
			e.printStackTrace();
		}
		
		container.onMapperComplete(mapperName);
	}
	
	public String getId() {
		return mapperName;
	}

	public void setId(String mapperName) {
		this.mapperName = mapperName;
	}
}
