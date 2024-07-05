package ai.mr.pipeline;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface ProducerConsumerPipeline<T extends Serializable> {

	public void connect(Producer<? extends T> producer);
	
	public void connect(Consumer<? super T> consumer);

	public void connectProducer(Producer<? extends T> producer);

	public void connectConsumer(Consumer<? super T> consumer);
	
	public void registerProducerErrorHandler(ProducerConsumerErrorHandler e);
	
	public void registerConsumerErrorHandler(ProducerConsumerErrorHandler e);
	
	public void start();
	
	public void stop() throws InterruptedException;
	
	public Map<Consumer<? super T>, Collection<T>> stopNow();
	
}
