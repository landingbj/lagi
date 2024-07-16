package ai.mr.pipeline;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.mr.AiGlobalMR;

public class ThreadedProducerConsumerPipeline<T extends Serializable> implements ProducerConsumerPipeline<T> {

	private static boolean _DEBUG_1 = false;
	private static boolean _DEBUG_2 = false;
	private static boolean _DEBUG_3 = false;
	private static boolean _DEBUG_4 = false;
	private static boolean _DEBUG_5 = false;

	static {
		if (AiGlobalMR._DEBUG_LEVEL >= 5) {
			_DEBUG_5 = true;
		}
		if (AiGlobalMR._DEBUG_LEVEL >= 4) {
			_DEBUG_4 = true;
		}
		if (AiGlobalMR._DEBUG_LEVEL >= 3) {
			_DEBUG_3 = true;
		}
		if (AiGlobalMR._DEBUG_LEVEL >= 2) {
			_DEBUG_2 = true;
		}
		if (AiGlobalMR._DEBUG_LEVEL >= 1) {
			_DEBUG_1 = true;
		}
	}
	
	private TimeUnit unit = AiGlobalMR.POOL_WAIT_UNIT;
	private int waitTime  = AiGlobalMR.PIPELINE_WAIT;
	
	private ExecutorService threadPool;
	private Collection<Producer<? extends T>> producers;
	private Collection<Consumer<? super T>> consumers;
	private Collection<Future<?>> futures;
	private ProducerConsumerErrorHandler producerErrorHandler;
	private ProducerConsumerErrorHandler consumerErrorHandler;
	private int producerThreads;
	private int consumerThreads;
	private AtomicBoolean shutdown;
	private Semaphore producerAheadLimit;
	
	private ConcurrentMap<Consumer<? super T>, BlockingQueue<T>> queues;

	public ThreadedProducerConsumerPipeline(int producerWeight,	int consumerWeight, int threadCount, int producerAhead) {
		super();
		this.producers = new ArrayList<Producer<? extends T>>();
		this.consumers = new ArrayList<Consumer<? super T>>();
		this.futures = new ArrayList<Future<?>>();
		this.queues = new ConcurrentHashMap<Consumer<? super T>, BlockingQueue<T>>();
		this.producerErrorHandler = new StubProducerConsumerErrorHandler();
		this.consumerErrorHandler = new StubProducerConsumerErrorHandler();
		this.producerAheadLimit = new Semaphore(producerAhead);
		initThreadCounts(producerWeight, consumerWeight, threadCount);
	}

	@Override
	public void connect(Producer<? extends T> producer) {
		this.producers.add(producer);
	}

	@Override
	public void connect(Consumer<? super T> consumer) {
		consumers.add(consumer);
		queues.put(consumer, new LinkedBlockingQueue<T>());
	}


	@Override
	public void connectProducer(Producer<? extends T> producer) {
		this.producers.add(producer);
	}

	@Override
	public void connectConsumer(Consumer<? super T> consumer) {
		consumers.add(consumer);
		queues.put(consumer, new LinkedBlockingQueue<T>());
	}

	@Override
	public void registerProducerErrorHandler(ProducerConsumerErrorHandler e) {
		this.producerErrorHandler = e;
	}

	@Override
	public void registerConsumerErrorHandler(ProducerConsumerErrorHandler e) {
		this.consumerErrorHandler = e;
	}

	@Override
	public void start() {
		if(threadPool != null && !threadPool.isTerminated()) {
			return;
		}
		shutdown = new AtomicBoolean(false);
		initializeAll();
		threadPool = Executors.newFixedThreadPool(producerThreads + consumerThreads);
		
		for(int i = 0; i < producerThreads; ++i) {
			Future<?> fut = threadPool.submit(new ProducerRoundRobinRunnable());
			this.futures.add(fut);
		}
		for(int i = 0; i < consumerThreads; ++i) {
			Future<?> fut = threadPool.submit(new ConsumerRoundRobinRunnable());
			this.futures.add(fut);
		}
		
//if(_DEBUG_1) {
//	System.out.println("[DEBUG-1] Started Producer Consumer");
//}
	}

	@Override
	public void stop() throws InterruptedException {
		if(threadPool == null || threadPool.isShutdown() || shutdown.get()) {
			return;
		}
		shutdown.set(true);
		
if(_DEBUG_1) {
		System.out.println("[DEBUG-1] Initiating shutdown");
}
		interruptThreads();
		
if(_DEBUG_1) {
		System.out.println("[DEBUG-1] Interrupted Threads");
}
		threadPool.shutdown();
		while(!threadPool.isTerminated()) {
			threadPool.awaitTermination(100, TimeUnit.MILLISECONDS);
		}
		
if(_DEBUG_1) {
		System.out.println("[DEBUG-1] Cleaning up");
}
		cleanupQueues();
		cleanupAll();
	}

	@Override
	public Map<Consumer<? super T>, Collection<T>> stopNow() {
		if(threadPool == null || threadPool.isShutdown() || shutdown.get()) {
			return new HashMap<Consumer<? super T>, Collection<T>>();
		}
		shutdown.set(true);
		interruptThreads();
		threadPool.shutdownNow();
		Map<Consumer<? super T>, Collection<T>> result = new HashMap<Consumer<? super T>, Collection<T>>();
		
		for(Entry<Consumer<? super T>, BlockingQueue<T>> q : queues.entrySet()) {
			result.put(q.getKey(), new ArrayList<T>(q.getValue()));
		}
		
		cleanupAll();
		
		return result;
	}
	
	private void interruptThreads() {
		for(Future<?> f : this.futures) {
			f.cancel(true);
		}
		this.futures.clear();
	}
	
	private void cleanupAll() {
		
if(_DEBUG_3){
		System.out.println("[DEBUG-3]" + " Coming into cleanupAll() @ThreadedProducerConsumerPipeline");
}
		for(Producer<?> p : this.producers) {
			p.cleanup();
		}
		
if(_DEBUG_4){
		System.out.println("[DEBUG-4]" + " Producer Cleanedup @cleanupAll()");
}		
		for(Consumer<?> c : this.consumers) {
			c.cleanup();
		}
		
if(_DEBUG_4){
		System.out.println("[DEBUG-4]" + " Consumer Cleanedup @cleanupAll()");
}		
	}
	
	private void cleanupQueues() {
		
		// wrap up all queue
		for(Entry<Consumer<? super T>, BlockingQueue<T>> q : queues.entrySet()) {
			T entry = q.getValue().poll();
			int counter = 0;
			int rotation = q.getValue().size() / 10;
			while(entry != null) {
if(_DEBUG_1){
				if(counter++ == rotation) {
					System.out.println(String.format("[DEBUG-1] Cleaning up queue, %d items remaining", q.getValue().size()));
					counter = 0;
				}
}
				try {
					q.getKey().consume(entry);
					entry = q.getValue().poll();
				} catch (Exception e) {
					consumerErrorHandler.handle(e);
				}
			}
		}
	}

	private void initializeAll() {
		for(Producer<?> p : this.producers) {
			p.init();
		}
		
		for(Consumer<?> c : this.consumers) {
			c.init();
		}
	}
	
	private void initThreadCounts(int producerWeight, int consumerWeight, int total) 
	{
		double base = producerWeight + consumerWeight;
		double producerCount = (producerWeight / base) * total;
		
		if(producerCount < 1) 	{
			this.producerThreads = 1;
		}
		else	{
			this.producerThreads = (int) producerCount;
		}
		
		if(total-1 > this.producerThreads)	{
			this.consumerThreads = total - producerThreads;
		}
		else	{
			this.consumerThreads = 1;
		}
	}
	
	private class ProducerRoundRobinRunnable implements Runnable {
		
		@Override
		public void run() {
			while(!shutdown.get()) {
				for(Producer<? extends T> p : new ArrayList<>(ThreadedProducerConsumerPipeline.this.producers)) {
					try {
						Collection<? extends T> obj = p.produce();

						if(obj == null || obj.isEmpty()) {
							unit.sleep(waitTime);
							continue;
						}
						
						producerAheadLimit.acquire(obj.size());
						
						// 每个consumer一个队列, 每个producer的处理结果, 分别放入所有consumer的队列中
						for(BlockingQueue<T> b : queues.values()) {
							b.addAll(obj);
						}
					} catch(InterruptedException e1) {
						break;
					} catch (Exception e) {
						producerErrorHandler.handle(e);
					} catch(Throwable e) {
						e.printStackTrace();
					}
					
					if(shutdown.get()) {
						break;
					}
				}
			}
			cleanupQueues();
		
if(_DEBUG_1){
			System.out.println("[DEBUG-1] Producer thread shutdown complete");
}
		}
	}
	
	private class ConsumerRoundRobinRunnable implements Runnable {

		@Override
		public void run() {
			while(!shutdown.get()) {
				// 逐个consumer取Queue中的请求数据，所以是轮询而非并发的
				for(Consumer<? super T> c : new ArrayList<>(ThreadedProducerConsumerPipeline.this.consumers)) {
					BlockingQueue<T> queue = queues.get(c);

					try {
						T data = queue.take();
						try {
							c.consume(data);
						} catch(InterruptedException interrupt) {
							throw interrupt;
						} catch (Exception e) {
							consumerErrorHandler.handle(e);
						} catch(Throwable e) {
							e.printStackTrace();
						}
					} catch (InterruptedException e) {
						break;
					} catch(Throwable e) {
						e.printStackTrace();
					}
					
					if(shutdown.get()) {
						break;
					}
				}
				producerAheadLimit.release();
			}

			cleanupQueues();
			
if(_DEBUG_1) {
			System.out.println("[DEBUG-1] Consumer thread shutdown complete");
}
		}
		
	}
	
	
}
