package ai.mr;

import java.util.Map;

public interface IContainer extends AutoCloseable {

	public void onMapperComplete(String id);

	public void onMapperFail(String id, Integer priority, Throwable throwable);

	public void onReducerComplete(String id);

	public void onReducerFail(String id);

	public void registMapper(String mapperName, Map<String, ?> parameters);
	
	public void registerMapper(IMapper mapper);

	public long size();
	
	public void run();
	
	public void close();
}
