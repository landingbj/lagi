package ai.mr.pipeline;

import java.io.Serializable;
import java.util.Collection;

public interface Producer<T extends Serializable> {

	public void init();
	
	public Collection<T> produce() throws Exception;
	
	public void cleanup();
	
}
