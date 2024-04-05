package ai.mr.pipeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import ai.mr.AiGlobalMR;

public abstract class ConnectedProducerConsumerPipeline<C extends Serializable, P extends Serializable> implements Producer<P>, Consumer<C> 
{
	private static boolean _DEBUG_1 = false;
	private static boolean _DEBUG_2 = false;
	private static boolean _DEBUG_3 = false;
	
	static 	{
		if(AiGlobalMR._DEBUG_LEVEL >= 3)	{
			_DEBUG_3 = true;
		}
		if(AiGlobalMR._DEBUG_LEVEL >= 2)	{
			_DEBUG_2 = true;
		}
		if(AiGlobalMR._DEBUG_LEVEL >= 1)	{
			_DEBUG_1 = true;
		}
	}	

	private List<C> items;
	private int limit;
	
	public ConnectedProducerConsumerPipeline(int limitSize) {
		items = new ArrayList<>();
		this.limit = limitSize;
	}
	
	@Override
	public synchronized void consume(C data) throws Exception {
		while(items.size() >= limit) {
			this.wait();
		}
		
		// 将上家处理好的数据，放入待处理队列中
		this.items.add(data);
	}
	
	@Override
	public synchronized Collection<P> produce() throws Exception {
		if(this.items.isEmpty()) {
			return new HashSet<>();
		}
if(_DEBUG_3){		
		System.out.println("[DEBUG-3]" + " items@ConnectedProducer : " + this.items.size());
}		
		C item = this.items.remove(0);
		// 唤醒其他的处理线程
		this.notifyAll();
		
		// 具体的处理逻辑由其绑定的子类完成
		return produce(item);
	}

	
	protected abstract Collection<P> produce(C item) throws Exception;
	
}
