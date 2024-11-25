package ai.workflow.container;

import ai.common.exception.RRException;
import ai.mr.AiGlobalMR;
import ai.mr.IRContainer;
import ai.mr.IReducer;
import ai.mr.container.ReduceContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class AgentContainer extends ReduceContainer implements IRContainer {
    private List<?> result;
    private final List<Object> mapperResult = Collections.synchronizedList(new ArrayList<>());
    private CountDownLatch latch = new CountDownLatch(0);
    private IReducer reducer = null;
    private volatile Integer maxPriority = -1;
    private RRException rrException;

    @Override
    public IRContainer Init() {
        this.reducer = this.reducerGroup;
        return this;
    }

    @Override
    public List<?> running() {
        latch = new CountDownLatch(mappersGroup.size());
        startThread(mappersGroup, this);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return result;
        }
        reducer.myReducing(mapperResult);
        result = reducer.getResult();
        return result;
    }

    @Override
    public void onMapperComplete(String mapperName, List<?> list, int priority) {
        mapperResult.add(list);
        double prior = (Double) list.get(AiGlobalMR.M_LIST_RESULT_PRIORITY);
        if (prior >= AiGlobalMR.FAST_DIRECT_PRIORITY) {
//            while (latch.getCount() > 0L) {
                latch.countDown();
//            }
        } else {
            latch.countDown();
        }
    }

    @Override
    public void onMapperComplete(String id) {
        latch.countDown();
    }

    @Override
    public void onMapperFail(String mapperName, Integer priority, Throwable throwable) {
        latch.countDown();
        if (throwable instanceof RRException) {
            if (priority > maxPriority) {
                synchronized (this) {
                    if (priority > maxPriority) {
                        maxPriority = priority;
                        rrException = (RRException) throwable;
                    }
                }
            }
        }
    }

    public RRException getException() {
        return rrException;
    }

    @Override
    public void onReducerComplete(String reducerName) {
    }

    @Override
    public void onReducerFail(String reducerName) {
    }

    @Override
    public void run() {
        running();
    }

    @Override
    public void close() {
        super.asynClose();
    }
}
