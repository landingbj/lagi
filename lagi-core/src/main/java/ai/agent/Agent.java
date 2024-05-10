package ai.agent;

import ai.agent.pojo.AgentData;

public abstract class Agent {
    public abstract void connect();

    public abstract void terminate();

    public abstract void start();

    public abstract void stop();

    public abstract void send(AgentData request);

    public abstract AgentData receive();
}
