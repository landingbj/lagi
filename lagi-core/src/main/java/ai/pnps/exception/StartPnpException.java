package ai.pnps.exception;

public class StartPnpException extends RuntimeException {
    public StartPnpException() {
        super("Start pnp failed");
    }
}