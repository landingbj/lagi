package ai.agent.exception;

public class TerminateAgentException extends RuntimeException {
    public TerminateAgentException() {
        super("Terminate agent failed");
    }
}