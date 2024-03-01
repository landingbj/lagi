package ai.mr.pipeline;

import java.io.Serializable;

public interface Consumer<T extends Serializable> {

	public void init();
	
	public void consume(T data) throws Exception;
	
	public void cleanup();
	
}
