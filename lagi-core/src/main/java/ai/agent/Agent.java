package ai.agent;


import ai.config.pojo.AgentConfig;
import io.reactivex.Observable;
import lombok.Data;

@Data
public abstract class Agent<T, R> implements Cloneable{

    protected AgentConfig agentConfig;

    public abstract void connect();

    public abstract void terminate();

    public abstract void start();

    public abstract void stop();

    public abstract void send(T request);

    public abstract R receive();

    public abstract R communicate(T data);

    public abstract Observable<R> stream(T data);

    public abstract boolean canStream();

    @Override
    public Agent<T, R> clone() throws CloneNotSupportedException {
        return (Agent<T, R>) super.clone();
    }
}
