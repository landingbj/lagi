package ai.mr.mapper;

import java.util.Map;

public class BaseMapper {

	protected Map<String, ?> data;
	protected int priority;

	public Map<String, ?> getParameters() {
		return data;
	}

	public void setParameters(Map<String, ?> data) {
		this.data = data;
	}
	
	public void setPriority(int priority)	{
		this.priority = priority;
	}
	
	public int getPriority()	{
		return priority;
	}
	

}
