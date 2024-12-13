package ai.worker;


public abstract class Worker<T, R> {
    public abstract R work(T data);

    public abstract R call(T data);

    public abstract void notify(T data);

}
