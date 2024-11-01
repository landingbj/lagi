package ai.mr.container;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;

import ai.mr.AiGlobalMR;
import ai.mr.IContainer;
import ai.mr.IMapper;

public class QueryContainer extends ExecutorsContainer implements IContainer {

	private static boolean _DEBUG_1 = false;
	private static boolean _DEBUG_2 = false;
	private static boolean _DEBUG_3 = false;

	static {
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

	private CountDownLatch latch;

	public QueryContainer() {
	}

	// 运行处理
	@Override
	public void run() {
		try {
			if (latch != null) {
				latch.await();
			}
			latch = new CountDownLatch(mappersGroup.size());
if(_DEBUG_3){
			System.out.println("[DEBUG-3]Parallel Threads : " + this.size());
}
			super.startThread(mappersGroup, this);
			latch.await();
			mappersGroup.clear();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// mapper线程完成的处理
	@Override
	public void onMapperComplete(String mapperName) {
if(_DEBUG_3){
			// Clear parameters
			IMapper im = mappersGroup.get(mapperName);
			im.setParameters(null);
}
		// Count down
		latch.countDown();
	}

	// mapper线程失败的处理
	@Override
	public void onMapperFail(String mapperName, Integer priority, Throwable throwable) {
		Iterator<Entry<String, IMapper>> it = mappersGroup.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, IMapper> entry = it.next();
			if (entry.getKey().equals(mapperName)) {
if(_DEBUG_3){
				entry.getValue().setParameters(null);
}
				it.remove();
			}
		}
		latch.countDown();
	}

	// reducer线程完成的处理
	@Override
	public void onReducerComplete(String reducerName) {

	}

	// reducer线程失败的处理
	@Override
	public void onReducerFail(String reducerName) {

	}

	@Override
	public long size() {
		if (latch != null)
			return this.latch.getCount();
		else
			return 0;
	}
}
