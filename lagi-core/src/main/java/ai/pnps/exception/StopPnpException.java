package ai.pnps.exception;

public class StopPnpException extends RuntimeException {
    public StopPnpException() {
        super("Stop pnp failed");
    }
}