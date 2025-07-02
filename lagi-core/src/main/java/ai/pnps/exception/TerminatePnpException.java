package ai.pnps.exception;

public class TerminatePnpException extends RuntimeException {
    public TerminatePnpException() {
        super("Terminate pnp failed");
    }
}