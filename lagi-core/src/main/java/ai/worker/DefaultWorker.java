package ai.worker;

import ai.manager.WorkerManager;
import ai.worker.pojo.WorkData;


public class DefaultWorker {

    public DefaultWorker() {

    }
    // pointed worker
    public <T,R> R communicate(String workName, WorkData<T> data) {
        @SuppressWarnings("unchecked")
        Worker<T, R> worker = (Worker<T, R>)WorkerManager.getInstance().get(workName);
        if(worker == null) {
            return null;
        }
        if(!(worker instanceof DefaultAppointWorker)) {
            return null;
        }
        return worker.call(data);
    }

    public <T,R> R communicate(WorkData<T> data) {
        return communicate("appointedWorker", data);
    }

    // Best Worker
    // Pipeline Worker
    // Filter Worker
    // Social Work
    public <T,R> R work(String workerName, WorkData<T> data) {
        @SuppressWarnings("unchecked")
        Worker<T, R> worker =  (Worker<T, R>)WorkerManager.getInstance().get(workerName);
        if(worker == null) {
            return null;
        }
        return worker.work(data);
    }

    public <T,R> R work(WorkData<T> data) {
        return work("BestWorker", data);
    }

    public <T> void  notify(String workName, WorkData<T> data) {
        @SuppressWarnings("unchecked")
        Worker<T, ?> worker = (Worker<T, ?>)WorkerManager.getInstance().get(workName);
        if(worker == null) {
            return ;
        }
        worker.notify(data);
    }

    public <T> void notify(WorkData<T> data) {
        notify("appointedWorker", data);
    }


}
