package ai.agent.exception;

public class StopAgentException extends RuntimeException {
    public StopAgentException() {
        super("Stop agent failed");
    }
}