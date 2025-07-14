package ai.pnps.exception;

public class ConnectionTimeoutException extends RuntimeException {
    public ConnectionTimeoutException() {
        super("Connection timeout");
    }
}