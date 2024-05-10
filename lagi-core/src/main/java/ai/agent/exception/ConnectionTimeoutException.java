package ai.agent.exception;

public class ConnectionTimeoutException extends RuntimeException {
    public ConnectionTimeoutException() {
        super("Connection timeout");
    }
}