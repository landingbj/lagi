package ai.agent;


import ai.config.pojo.AgentConfig;
import lombok.Getter;

@Getter
public abstract class Agent<T, R>{

    protected AgentConfig agentConfig;

    public abstract void connect();

    public abstract void terminate();

    public abstract void start();

    public abstract void stop();

    public abstract void send(T request);

    public abstract R receive();

    public abstract R communicate(T data);
}
