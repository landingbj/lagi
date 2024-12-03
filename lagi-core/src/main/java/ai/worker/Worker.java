package ai.worker;


import ai.worker.pojo.WorkData;

public abstract class Worker<T, R> {
    public abstract R work(WorkData<T> data);

    public abstract R call(WorkData<T> data);

    public abstract void notify(WorkData<T> data);

}
