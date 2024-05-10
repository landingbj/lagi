package ai.agent.exception;

public class StartAgentException extends RuntimeException {
    public StartAgentException() {
        super("Start agent failed");
    }
}