package ai.mr.pipeline;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import ai.dao.Pool;
import ai.mr.pipeline.Producer;

public abstract class DelayedPoolProducer<T extends Serializable, P> implements Producer<T> {

	private int minWaitTime;
	private TimeUnit unit;
	private int maxWaitTime;
	private Pool<P> pool;
	
	public DelayedPoolProducer(int minWaitTime, TimeUnit unit, int maxWaitTime,	Pool<P> pool) {
		super();
		this.minWaitTime = minWaitTime;
		this.unit = unit;
		this.maxWaitTime = maxWaitTime;
		this.pool = pool;
	}

	@Override
	public Collection<T> produce() throws Exception {
		int waitTime = minWaitTime;
		P item = null;
		try {
			while(item == null) {
				item = pool.take();
				if(item == null) {
					unit.sleep(waitTime);
					waitTime = Math.min(waitTime * 2, maxWaitTime);
				}
			}
			return this.delayedProduce(item);
		} catch(InterruptedException e) {
			return null;
		} catch(Exception e) {
			if(item != null) {
				e.printStackTrace();
				pool.returnItem(item);
				return new HashSet<T>();
			} else {
				throw new ExecutionException(e);
			}
		}
	}
	
	protected int getMinWaitTime() {
		return minWaitTime;
	}

	protected TimeUnit getUnit() {
		return unit;
	}

	protected int getMaxWaitTime() {
		return maxWaitTime;
	}

	protected Pool<P> getPool() {
		return pool;
	}

	protected abstract Collection<T> delayedProduce(P phrase) throws Exception;
	
}
